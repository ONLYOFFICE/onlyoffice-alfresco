package com.parashift.onlyoffice.scripts;

import com.parashift.onlyoffice.util.*;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.extensions.webscripts.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/*
   Copyright (c) Ascensio System SIA 2023. All rights reserved.
   http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.editor-api.post")
public class EditorApi extends AbstractWebScript {

    @Autowired
    NodeService nodeService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    ContentService contentService;

    @Autowired
    MimetypeService mimetypeService;

    @Autowired
    FavouritesService favouritesService;

    @Autowired
    Util util;

    @Autowired
    JwtManager jwtManager;

    @Autowired
    ConvertManager converterService;

    @Autowired
    MessageService mesService;

    @Autowired
    UrlManager urlManager;

    @Autowired
    RequestManager requestManager;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
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
                docxToDocxf(request, response);
                break;
            default:
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "API Not Found");
        }
    }

    private void insert(WebScriptRequest request, WebScriptResponse response) throws IOException {
        try {
            JSONObject requestData = new JSONObject(request.getContent().getContent());
            JSONArray nodes = requestData.getJSONArray("nodes");
            List<Object> responseJson = new ArrayList<>();

            for (int i = 0; i < nodes.length(); i++) {
                JSONObject data = new JSONObject();

                NodeRef node = new NodeRef(nodes.getString(i));

                if (permissionService.hasPermission(node, PermissionService.READ) == AccessStatus.ALLOWED) {
                    String fileType = util.getExtension(node);

                    if (requestData.has("command")) {
                        data.put("c", requestData.get("command"));
                    }
                    data.put("fileType", fileType);
                    data.put("url", urlManager.getContentUrl(node));
                    if (jwtManager.jwtEnabled()) {
                        try {
                            data.put("token", jwtManager.createToken(data));
                        } catch (Exception e) {
                            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Token creation error", e);
                        }
                    }

                    responseJson.add(data);
                }
            }

            response.setContentType("application/json");
            response.getWriter().write(responseJson.toString());
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from request", e);
        }
    }

    private void docxToDocxf(WebScriptRequest request, WebScriptResponse response) throws IOException {
        try {
            JSONObject requestData = new JSONObject(request.getContent().getContent());
            JSONArray docxNode = requestData.getJSONArray("nodes");
            String folder = requestData.getString("parentNode");

            if (docxNode.length() == 0) {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Selected docx template not found");
            }

            NodeRef folderNode = new NodeRef(folder);

            if (permissionService.hasPermission(folderNode, PermissionService.CREATE_CHILDREN) != AccessStatus.ALLOWED) {
                throw new WebScriptException(Status.STATUS_FORBIDDEN, "User don't have the permissions to create child node");
            }

            NodeRef node = new NodeRef(docxNode.getString(0));
            JSONObject data = new JSONObject();

            if (permissionService.hasPermission(node, PermissionService.READ) == AccessStatus.ALLOWED) {
                String fileType = util.getExtension(node);
                if (!mimetypeService.getMimetype(fileType).equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Selected file is not docx extension");
                }

                try {
                    String downloadUrl = converterService.convert(util.getKey(node), fileType, "docxf", urlManager.getContentUrl(node), mesService.getLocale().toLanguageTag());
                    String docTitle = util.getTitleWithoutExtension(node);
                    String newNode = createNode(folderNode, docTitle, "docxf", downloadUrl);
                    data.put("nodeRef", newNode);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not convert docx file to docxf", e);
                }
            }

            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from request", e);
        }
    }

    private void saveAs(WebScriptRequest request, WebScriptResponse response) throws IOException {
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

            if (permissionService.hasPermission(folderNode, PermissionService.CREATE_CHILDREN) != AccessStatus.ALLOWED) {
                throw new WebScriptException(Status.STATUS_FORBIDDEN, "User don't have the permissions to create child node");
            }

            createNode(folderNode, title, ext, url);
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from request", e);
        }
    }

    private String createNode(NodeRef folderNode, String title, final String ext, String url) throws IOException {
        String fileName = util.getCorrectName(folderNode, title, ext);

        final NodeRef nodeRef = nodeService.createNode(
                folderNode,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileName),
                ContentModel.TYPE_CONTENT,
                Collections.<QName, Serializable> singletonMap(ContentModel.PROP_NAME, fileName)).getChildRef();

        requestManager.executeRequestToDocumentServer(url, new RequestManager.Callback<Void>() {
            public Void doWork(HttpEntity httpEntity) throws IOException {
                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(mimetypeService.getMimetype(ext));
                writer.putContent(httpEntity.getContent());
                return null;
            }
        });

        util.ensureVersioningEnabled(nodeRef);
        util.postActivity(nodeRef, true);

        return nodeRef.toString();
    }

    private void favorite(WebScriptRequest request, WebScriptResponse response) throws IOException {
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
}

