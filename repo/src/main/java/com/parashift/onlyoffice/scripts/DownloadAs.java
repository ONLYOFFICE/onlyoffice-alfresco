package com.parashift.onlyoffice.scripts;

import com.parashift.onlyoffice.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.DownloadModel;
import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.zip.Deflater;

/*
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.download-as.post")
public class DownloadAs extends AbstractWebScript {

    private static final String MIMETYPE_ZIP = "application/zip";
    private static final String TEMP_FILE_PREFIX = "alf";
    private static final String ZIP_EXTENSION = ".zip";
    private final static String CONTENT_DOWNLOAD_API_URL = "slingshot/node/content/{0}/{1}/{2}/{3}";

    @Autowired
    ConvertManager converterService;

    @Autowired
    Util util;

    @Autowired
    PermissionService permissionService;

    @Autowired
    NodeService nodeService;

    @Autowired
    ContentService contentService;

    @Autowired
    DownloadStorage downloadStorage;

    @Autowired
    MimetypeService mimetypeService;

    @Autowired
    DownloadStatusUpdateService updateService;

    @Autowired
    MessageService mesService;

    @Autowired
    UrlManager urlManager;

    @Autowired
    RequestManager requestManager;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        try {
            String contentURL = null;
            JSONArray requestDataJson = new JSONArray(request.getContent().getContent());
            String region = mesService.getLocale().toLanguageTag();

            if (requestDataJson.length() == 1) {
                JSONObject data = requestDataJson.getJSONObject(0);

                NodeRef node = new NodeRef(data.getString("nodeRef"));
                final String outputType = data.getString("outputType");

                if (permissionService.hasPermission(node, PermissionService.READ) != AccessStatus.ALLOWED) {
                    throw new AccessDeniedException("Access denied. You do not have the appropriate permissions to perform this operation. NodeRef= " + node.toString());
                }

                String currentExt = util.getExtension(node);

                if (currentExt.equals(outputType)) {
                    String docTitle = util.getTitle(node);

                    contentURL = getDownloadAPIUrl(node, docTitle);
                } else {
                    String downloadUrl = converterService.convert(util.getKey(node), currentExt, outputType, urlManager.getContentUrl(node), region);
                    final String docTitle = util.getTitleWithoutExtension(node) + "." + outputType;

                    contentURL = requestManager.executeRequestToDocumentServer(downloadUrl, new RequestManager.Callback<String>() {
                        public String doWork(HttpEntity httpEntity) throws IOException {
                            byte[] bytes = EntityUtils.toByteArray(httpEntity);
                            InputStream inputStream = new ByteArrayInputStream(bytes);

                            return createDownloadNode(docTitle, mimetypeService.getMimetype(outputType), inputStream, bytes.length, 1);
                        }
                    });
                }
            } else {
                File zip = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, ZIP_EXTENSION);
                long totalSize = 0;

                try (FileOutputStream stream = new FileOutputStream(zip);
                     ZipArchiveOutputStream out = new ZipArchiveOutputStream(stream)) {

                    out.setEncoding("UTF-8");
                    out.setMethod(ZipArchiveOutputStream.DEFLATED);
                    out.setLevel(Deflater.BEST_COMPRESSION);

                    for (int i = 0; i < requestDataJson.length(); i++) {
                        JSONObject data = requestDataJson.getJSONObject(i);

                        NodeRef node = new NodeRef(data.getString("nodeRef"));
                        String outputType = data.getString("outputType");

                        if (permissionService.hasPermission(node, PermissionService.READ) != AccessStatus.ALLOWED) {
                            throw new AccessDeniedException("Access denied. You do not have the appropriate permissions to perform this operation. NodeRef= " + node.toString());
                        }

                        String currentExt = util.getExtension(node);

                        if (currentExt.equals(outputType)) {
                            String docTitle = util.getTitle(node);
                            ContentReader reader = contentService.getReader(node, ContentModel.PROP_CONTENT);
                            try (InputStream inputStream = reader.getContentInputStream()) {
                                out.putArchiveEntry(new ZipArchiveEntry(docTitle));
                                totalSize = totalSize + IOUtils.copyLarge(inputStream, out);
                            }
                        } else {
                            String downloadUrl = converterService.convert(util.getKey(node), currentExt, outputType, urlManager.getContentUrl(node), region);
                            String docTitle = util.getTitleWithoutExtension(node) + "." + outputType;

                            out.putArchiveEntry(new ZipArchiveEntry(docTitle));

                            totalSize += requestManager.executeRequestToDocumentServer(downloadUrl, new RequestManager.Callback<Long>() {
                                public Long doWork(HttpEntity httpEntity) throws IOException {
                                    return IOUtils.copyLarge(httpEntity.getContent(), out);
                                }
                            });
                        }

                        out.closeArchiveEntry();
                    }
                }

                try (FileInputStream inputStream = new FileInputStream(zip)) {
                    contentURL = createDownloadNode("Archive.zip", MIMETYPE_ZIP, inputStream, totalSize, requestDataJson.length());
                }
            }

            JSONObject responseJson = new JSONObject();
            responseJson.put("downloadUrl", contentURL);

            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");
            response.getWriter().write(responseJson.toString(3));

        } catch(Exception e){
            throw new WebScriptException(e.getMessage(), e);
        }
    }

    private String createDownloadNode (String title, String mimeType, InputStream inputStream, long totalSize, long totalFiles) {
        NodeRef downloadNode = nodeService.createNode(
                downloadStorage.getOrCreateDowloadContainer(),
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                DownloadModel.TYPE_DOWNLOAD,
                Collections.<QName, Serializable> singletonMap(ContentModel.PROP_NAME, title)).getChildRef();

        Map<QName, Serializable> aspectProperties = new HashMap<>(2);
        aspectProperties.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);
        aspectProperties.put(ContentModel.PROP_IS_CONTENT_INDEXED, Boolean.FALSE);
        nodeService.addAspect(downloadNode, ContentModel.ASPECT_INDEX_CONTROL, aspectProperties);

        ContentWriter writer = contentService.getWriter(downloadNode, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimeType);
        writer.putContent(inputStream);

        DownloadStatus status = new DownloadStatus(DownloadStatus.Status.DONE, totalSize, totalSize, totalFiles, totalFiles);
        int sequenceNumber = downloadStorage.getSequenceNumber(downloadNode) + 1;
        updateService.update(downloadNode, status, sequenceNumber);

        return getDownloadAPIUrl(downloadNode, title);
    }

    private String getDownloadAPIUrl (NodeRef nodeRef, String title) {
        String contentURL = MessageFormat.format(
                CONTENT_DOWNLOAD_API_URL, new Object[]{
                        nodeRef.getStoreRef().getProtocol(),
                        nodeRef.getStoreRef().getIdentifier(),
                        nodeRef.getId(),
                        URLEncoder.encode(title)});

        return contentURL;
    }
}
