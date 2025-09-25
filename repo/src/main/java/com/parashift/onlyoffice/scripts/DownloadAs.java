/*
    Copyright (c) Ascensio System SIA 2025. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.scripts;

import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.model.convertservice.ConvertRequest;
import com.onlyoffice.model.convertservice.ConvertResponse;
import com.onlyoffice.service.convert.ConvertService;
import com.parashift.onlyoffice.sdk.client.DocumentServerClient;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.DownloadModel;
import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;


@Component(value = "webscript.onlyoffice.download-as.post")
public class DownloadAs extends AbstractWebScript {

    private static final String MIMETYPE_ZIP = "application/zip";
    private static final String TEMP_FILE_PREFIX = "alf";
    private static final String ZIP_EXTENSION = ".zip";
    private static final String CONTENT_DOWNLOAD_API_URL = "slingshot/node/content/{0}/{1}/{2}/{3}";

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private DownloadStorage downloadStorage;

    @Autowired
    private MimetypeService mimetypeService;

    @Autowired
    private DownloadStatusUpdateService updateService;

    @Autowired
    private MessageService mesService;

    @Autowired
    private DocumentServerClient documentServerClient;

    @Autowired
    private ConvertService convertService;

    @Autowired
    private DocumentManager documentManager;

    @Override
    public void execute(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
        try {
            String contentURL = null;
            JSONArray requestDataJson = new JSONArray(request.getContent().getContent());
            String region = mesService.getLocale().toLanguageTag();

            if (requestDataJson.length() == 1) {
                JSONObject data = requestDataJson.getJSONObject(0);

                NodeRef node = new NodeRef(data.getString("nodeRef"));
                final String outputType = data.getString("outputType");

                if (permissionService.hasPermission(node, PermissionService.READ) != AccessStatus.ALLOWED) {
                    throw new AccessDeniedException("Access denied. You do not have the appropriate permissions"
                            + "to perform this operation. NodeRef= " + node.toString());
                }

                String docTitle = documentManager.getDocumentName(node.toString());
                String currentExt = documentManager.getExtension(docTitle);

                if (currentExt.equals(outputType)) {
                    contentURL = getDownloadAPIUrl(node, docTitle);
                } else {
                    ConvertResponse convertResponse = convertNodeToOutputType(node, outputType, region);
                    final String newTitle = documentManager.getBaseName(docTitle) + "." + convertResponse.getFileType();

                    NodeRef downloadNode = createDownloadNode(newTitle);

                    ContentWriter writer = contentService.getWriter(downloadNode, ContentModel.PROP_CONTENT, true);
                    writer.setMimetype(mimetypeService.getMimetype(convertResponse.getFileType()));

                    int totalSize = documentServerClient.getFile(
                            convertResponse.getFileUrl(),
                            writer.getContentOutputStream()
                    );

                    contentURL = createDownloadRegistry(
                            downloadNode,
                            newTitle,
                            totalSize,
                            1
                    );
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
                            throw new AccessDeniedException("Access denied. You do not have the appropriate "
                                    + "permissions to perform this operation. NodeRef= " + node.toString());
                        }

                        String docTitle = documentManager.getDocumentName(node.toString());
                        String currentExt = documentManager.getExtension(docTitle);

                        if (currentExt.equals(outputType)) {
                            ContentReader reader = contentService.getReader(node, ContentModel.PROP_CONTENT);
                            try (InputStream inputStream = reader.getContentInputStream()) {
                                out.putArchiveEntry(new ZipArchiveEntry(docTitle));
                                totalSize = totalSize + IOUtils.copyLarge(inputStream, out);
                            }
                        } else {
                            ConvertResponse convertResponse = convertNodeToOutputType(node, outputType, region);
                            String newTitle = MessageFormat.format(
                                    "{0}.{1}",
                                    documentManager.getBaseName(docTitle),
                                    convertResponse.getFileType()
                            );

                            out.putArchiveEntry(new ZipArchiveEntry(newTitle));

                            totalSize += documentServerClient.getFileWithoutCloseOutputStream(
                                    convertResponse.getFileUrl(),
                                    out
                            );
                        }

                        out.closeArchiveEntry();
                    }
                }

                NodeRef downloadNode = createDownloadNode("Archive.zip");

                try (FileInputStream inputStream = new FileInputStream(zip)) {
                    ContentWriter writer = contentService.getWriter(downloadNode, ContentModel.PROP_CONTENT, true);
                    writer.setMimetype(mimetypeService.getMimetype(MIMETYPE_ZIP));
                    writer.putContent(inputStream);

                    contentURL = createDownloadRegistry(
                            downloadNode,
                            "Archive.zip",
                            totalSize,
                            requestDataJson.length()
                    );
                }
            }

            JSONObject responseJson = new JSONObject();
            responseJson.put("downloadUrl", contentURL);

            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");
            response.getWriter().write(responseJson.toString());

        } catch (Exception e) {
            throw new WebScriptException(e.getMessage(), e);
        }
    }

    private ConvertResponse convertNodeToOutputType(final NodeRef node, final String outputType, final String region)
            throws Exception {
        ConvertRequest convertRequest = ConvertRequest.builder()
                .outputtype(outputType)
                .region(region)
                .build();

        ConvertResponse convertResponse = convertService.processConvert(
                convertRequest,
                node.toString()
        );

        if (convertResponse.getError() != null
                && convertResponse.getError().equals(ConvertResponse.Error.TOKEN)) {
            throw new SecurityException();
        }

        if (convertResponse.getEndConvert() == null || !convertResponse.getEndConvert()
                || convertResponse.getFileUrl() == null || convertResponse.getFileUrl().isEmpty()) {
            throw new Exception("'endConvert' is false or 'fileUrl' is empty");
        }

        return convertResponse;
    }

    private NodeRef createDownloadNode(final String title) {
        NodeRef downloadNode = nodeService.createNode(
                downloadStorage.getOrCreateDowloadContainer(),
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                DownloadModel.TYPE_DOWNLOAD,
                Collections.<QName, Serializable>singletonMap(ContentModel.PROP_NAME, title)).getChildRef();

        Map<QName, Serializable> aspectProperties = new HashMap<>(2);
        aspectProperties.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);
        aspectProperties.put(ContentModel.PROP_IS_CONTENT_INDEXED, Boolean.FALSE);
        nodeService.addAspect(downloadNode, ContentModel.ASPECT_INDEX_CONTROL, aspectProperties);

        return downloadNode;
    }

    private String createDownloadRegistry(final NodeRef downloadNode, final String title, final long totalSize,
                                          final long totalFiles) {
        DownloadStatus status = new DownloadStatus(
                DownloadStatus.Status.DONE,
                totalSize,
                totalSize,
                totalFiles,
                totalFiles
        );

        int sequenceNumber = downloadStorage.getSequenceNumber(downloadNode) + 1;
        updateService.update(downloadNode, status, sequenceNumber);

        return getDownloadAPIUrl(downloadNode, title);
    }

    private String getDownloadAPIUrl(final NodeRef nodeRef, final String title) {
        String contentURL = MessageFormat.format(
                CONTENT_DOWNLOAD_API_URL, new Object[]{
                        nodeRef.getStoreRef().getProtocol(),
                        nodeRef.getStoreRef().getIdentifier(),
                        nodeRef.getId(),
                        URLEncoder.encode(title)});

        return contentURL;
    }
}
