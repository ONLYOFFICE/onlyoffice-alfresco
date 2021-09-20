package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

/*
    Copyright (c) Ascensio System SIA 2021. All rights reserved.
    http://www.onlyoffice.com
*/

@Component(value = "webscript.onlyoffice.save-as.post")
public class SaveAs extends AbstractWebScript {

    @Autowired
    NodeService nodeService;

    @Autowired
    Util util;

    @Autowired
    ContentService contentService;

    @Autowired
    MimetypeService mimetypeService;

    @Autowired
    PermissionService permissionService;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
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

            String fileName = util.getCorrectName(folderNode, title, ext);

            NodeRef nodeRef = nodeService.createNode(
                    folderNode,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileName),
                    ContentModel.TYPE_CONTENT,
                    Collections.<QName, Serializable> singletonMap(ContentModel.PROP_NAME, fileName)).getChildRef();

            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(mimetypeService.getMimetype(ext));

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            try (InputStream in = connection.getInputStream()) {
                writer.putContent(in);
                util.ensureVersioningEnabled(nodeRef);
            } finally {
                connection.disconnect();
            }
        } catch (JSONException e) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from request", e);
        }
    }
}
