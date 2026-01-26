/*
    Copyright (c) Ascensio System SIA 2026. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.scripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.client.DocumentServerClient;
import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.security.JwtManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.model.convertservice.ConvertRequest;
import com.onlyoffice.model.convertservice.ConvertResponse;
import com.onlyoffice.model.convertservice.convertrequest.PDF;
import com.onlyoffice.model.documenteditor.config.document.ReferenceData;
import com.onlyoffice.service.convert.ConvertService;
import com.parashift.onlyoffice.model.dto.ReferenceDataRequest;
import com.parashift.onlyoffice.model.dto.ReferenceDataResponse;
import com.parashift.onlyoffice.sdk.manager.url.UrlManager;
import com.parashift.onlyoffice.util.Util;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component(value = "webscript.onlyoffice.editor-api.post")
public class EditorApi extends AbstractWebScript {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private MimetypeService mimetypeService;

    @Autowired
    private FavouritesService favouritesService;

    @Autowired
    private Util util;

    @Autowired
    private JwtManager jwtManager;

    @Autowired
    private MessageService mesService;

    @Autowired
    private UrlManager urlManager;

    @Autowired
    private DocumentServerClient documentServerClient;

    @Autowired
    private ConvertService convertService;

    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private DocumentManager documentManager;

    @Autowired
    private FileFolderService fileFolderService;

    @Autowired
    private Repository repositoryHelper;

    @Autowired
    private NamespaceService namespaceService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
        Map<String, String> templateVars = request.getServiceMatch().getTemplateVars();
        String type = templateVars.get("type");
        switch (type.toLowerCase()) {
            case "insert":
                insert(request, response);
                break;
            case "save-as":
                saveAs(request, response);
                break;
            case "favorite":
                favorite(request, response);
                break;
            case "from-docx":
                docxToPdfForm(request, response);
                break;
            case "reference-data":
                referenceData(request, response);
                break;
            default:
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "API Not Found");
        }
    }

    private void insert(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
        try {
            JSONObject requestData = new JSONObject(request.getContent().getContent());
            JSONArray nodes = requestData.getJSONArray("nodes");
            List<Map<String, Object>> responseJson = new ArrayList<>();

            for (int i = 0; i < nodes.length(); i++) {
                Map<String, Object> data = new HashMap<>();

                NodeRef node = new NodeRef(nodes.getString(i));

                if (permissionService.hasPermission(node, PermissionService.READ) == AccessStatus.ALLOWED) {
                    String fileName = documentManager.getDocumentName(node.toString());
                    String fileType = documentManager.getExtension(fileName);

                    if (!requestData.get("command").equals(null)) {
                        data.put("c", requestData.getString("command"));
                    }
                    data.put("fileType", fileType);
                    data.put("url", urlManager.getFileUrl(node.toString()));
                    if (settingsManager.isSecurityEnabled()) {
                        try {
                            data.put("token", jwtManager.createToken(data));
                        } catch (Exception e) {
                            throw new WebScriptException(
                                    Status.STATUS_INTERNAL_SERVER_ERROR,
                                    "Token creation error",
                                    e
                            );
                        }
                    }

                    responseJson.add(data);
                }
            }

            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(responseJson));
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from request", e);
        }
    }

    private void docxToPdfForm(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
        try {
            JSONObject requestData = new JSONObject(request.getContent().getContent());
            JSONArray docxNode = requestData.getJSONArray("nodes");
            String folder = requestData.getString("parentNode");

            if (docxNode.length() == 0) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Selected docx template not found");
            }

            NodeRef folderNode = new NodeRef(folder);

            if (permissionService.hasPermission(folderNode, PermissionService.CREATE_CHILDREN)
                    != AccessStatus.ALLOWED) {
                throw new WebScriptException(
                        Status.STATUS_FORBIDDEN,
                        "User don't have the permissions to create child node"
                );
            }

            NodeRef node = new NodeRef(docxNode.getString(0));
            JSONObject data = new JSONObject();

            if (permissionService.hasPermission(node, PermissionService.READ) == AccessStatus.ALLOWED) {
                String fileName = documentManager.getDocumentName(node.toString());
                String fileType = documentManager.getExtension(fileName);
                if (!mimetypeService.getMimetype(fileType)
                        .equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Selected file is not docx extension");
                }

                try {
                    ConvertRequest convertRequest = ConvertRequest.builder()
                            .outputtype("pdf")
                            .pdf(new PDF(true))
                            .region(mesService.getLocale().toLanguageTag())
                            .build();

                    ConvertResponse convertResponse = convertService.processConvert(convertRequest, node.toString());

                    if (convertResponse.getError() != null
                            && convertResponse.getError().equals(ConvertResponse.Error.TOKEN)) {
                        throw new SecurityException();
                    }

                    if (convertResponse.getEndConvert() == null || !convertResponse.getEndConvert()
                            || convertResponse.getFileUrl() == null || convertResponse.getFileUrl().isEmpty()) {
                        throw new Exception("'endConvert' is false or 'fileUrl' is empty");
                    }

                    String downloadUrl = convertResponse.getFileUrl();
                    String docTitle = documentManager.getBaseName(fileName);
                    String newNode = createNode(folderNode, docTitle, "pdf", downloadUrl);
                    data.put("nodeRef", newNode);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not convert docx file to pdf", e);
                }
            }

            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from request", e);
        }
    }

    private void saveAs(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
        try {
            JSONObject requestData = new JSONObject(request.getContent().getContent());

            String title = requestData.getString("title");
            String ext = requestData.getString("ext");
            String url = requestData.getString("url");
            String saveNode = requestData.getString("saveNode");

            if (title.isEmpty() || ext.isEmpty() || url.isEmpty() || saveNode.isEmpty()) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Required query parameters not found");
            }

            NodeRef folderNode = new NodeRef(saveNode);

            if (permissionService.hasPermission(folderNode, PermissionService.CREATE_CHILDREN)
                    != AccessStatus.ALLOWED) {
                throw new WebScriptException(
                        Status.STATUS_FORBIDDEN,
                        "User don't have the permissions to create child node"
                );
            }

            createNode(folderNode, title, ext, url);
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from request", e);
        }
    }

    private String createNode(final NodeRef folderNode, final String title, final String ext, final String url) {
        String fileName = util.getCorrectName(folderNode, title, ext);

        final NodeRef nodeRef = nodeService.createNode(
                folderNode,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileName),
                ContentModel.TYPE_CONTENT,
                Collections.<QName, Serializable>singletonMap(ContentModel.PROP_NAME, fileName)).getChildRef();

        try {
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(mimetypeService.getMimetype(ext));

            documentServerClient.getFile(url, writer.getContentOutputStream());
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        }

        util.ensureVersioningEnabled(nodeRef);
        util.postActivity(nodeRef, true);

        return nodeRef.toString();
    }

    private void favorite(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
        if (request.getParameter("nodeRef") != null) {
            NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));
            String username = AuthenticationUtil.getFullyAuthenticatedUser();

            if (favouritesService.isFavourite(username, nodeRef)) {
                favouritesService.removeFavourite(username, nodeRef);
            } else {
                favouritesService.addFavourite(username, nodeRef);
            }
        } else {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Required query parameters not found");
        }
    }

    private void referenceData(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
        ReferenceDataRequest referenceDataRequest = objectMapper.readValue(
                request.getContent().getContent(),
                ReferenceDataRequest.class
        );

        NodeRef nodeRef = null;
        ReferenceData referenceData = referenceDataRequest.getReferenceData();
        if (referenceData != null) {
            String currentInstanceId = util.getCurrentInstanceId();
            String instanceId = referenceData.getInstanceId();

            if (instanceId == null || instanceId.isEmpty()) {
                instanceId = currentInstanceId;
                referenceData.setInstanceId(instanceId);
            }

            if (instanceId.equals(currentInstanceId)) {
                nodeRef = new NodeRef(referenceData.getFileKey());
            }
        }

        if (nodeRef == null || !nodeService.exists(nodeRef)) {
            String path = referenceDataRequest.getPath();

            if (path != null && !path.isEmpty()) {
                nodeRef = findNodeByPath(path);
            }
        }

        if (nodeRef == null || !nodeService.exists(nodeRef)) {
            String link = referenceDataRequest.getLink();
            if (link != null && link.startsWith(urlManager.getShareUrl())) {
                try {
                    URIBuilder uri = new URIBuilder(link);
                    NameValuePair nodeRefQueryParam = uri.getFirstQueryParam("nodeRef");
                    String nodeRefFromQueryParam = Optional.ofNullable(nodeRefQueryParam.getValue()).orElse("");

                    if (!nodeRefFromQueryParam.isEmpty()) {
                        nodeRef = new NodeRef(nodeRefFromQueryParam);
                    }
                } catch (URISyntaxException e) {
                    //Do nothing if link could not be parsed as a URI reference
                }
            }
        }

        if (nodeRef == null
                || !nodeService.exists(nodeRef)
                || permissionService.hasPermission(nodeRef, PermissionService.READ) != AccessStatus.ALLOWED) {
            throw new WebScriptException(
                    Status.STATUS_NOT_FOUND,
                    "Node with given parameters not found or you don't have permission to view this node."
            );
        }

        if (referenceData == null) {
            referenceData = ReferenceData.builder()
                    .fileKey(nodeRef.toString())
                    .instanceId(util.getCurrentInstanceId())
                    .build();
        }

        String documentName = documentManager.getDocumentName(nodeRef.toString());
        String extension = documentManager.getExtension(documentName);
        String path = MessageFormat.format("{0}/{1}", getPath(nodeRef), documentName);
        String documentKey = documentManager.getDocumentKey(nodeRef.toString(), false);
        String fileUrl = urlManager.getFileUrl(nodeRef.toString());
        String editorUrl = urlManager.getEditorUrl(nodeRef);

        ReferenceDataResponse referenceDataResponse = ReferenceDataResponse.builder()
                .fileType(extension)
                .path(path)
                .key(documentKey)
                .url(fileUrl)
                .link(editorUrl)
                .referenceData(referenceData)
                .build();

        if (settingsManager.isSecurityEnabled()) {
            referenceDataResponse.setToken(jwtManager.createToken(referenceDataResponse));
        }

        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(referenceDataResponse));
    }

    private String getPath(final NodeRef nodeRef) {
        Path path = nodeService.getPath(nodeRef);

        path = path.subPath(2, path.size() - 1);

        return path.toDisplayPath(nodeService, permissionService);
    }

    private NodeRef findNodeByPath(final String path) {
        NodeRef currentNode = repositoryHelper.getCompanyHome();
        String[] pathSegments = path.split("/");

        for (String segment : pathSegments) {
            if (!segment.isEmpty()) {
                currentNode = fileFolderService.searchSimple(currentNode, segment);
                if (currentNode == null) {
                    break;
                }
            }
        }

        return currentNode;
    }
}

