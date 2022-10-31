package com.parashift.onlyoffice.scripts;

import com.parashift.onlyoffice.util.HistoryManager;
import com.parashift.onlyoffice.util.JwtManager;
import com.parashift.onlyoffice.util.UrlManager;
import com.parashift.onlyoffice.util.Util;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import org.springframework.stereotype.Component;
import org.springframework.extensions.surf.util.URLEncoder;

import java.io.*;

/*
    Copyright (c) Ascensio System SIA 2022. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.download.get")
public class Download extends AbstractWebScript {

    @Autowired
    ContentService contentService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    JwtManager jwtManager;

    @Autowired
    MimetypeService mimetypeService;

    @Autowired
    HistoryManager historyManager;

    @Autowired
    Util util;

    @Autowired
    UrlManager urlManager;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        String nodeRefString = request.getParameter("nodeRef");
        String type = request.getServiceMatch().getTemplateVars().get("type");

        if (type == null || type.isEmpty()) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not find required 'type' parameter!");
        }

        if (nodeRefString == null || nodeRefString.isEmpty()) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not find required 'nodeRef' parameter!");
        }

        if (jwtManager.jwtEnabled() ) {
            String jwth = jwtManager.getJwtHeader();
            String header = request.getHeader(jwth);
            String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : header;

            if (token == null || token == "") {
                throw new SecurityException("Expected JWT");
            }

            try {
                String payload = jwtManager.verify(token);
            } catch (Exception e) {
                throw new SecurityException("JWT verification failed!");
            }
        }

        NodeRef nodeRef = new NodeRef(nodeRefString);

        if (permissionService.hasPermission(nodeRef, PermissionService.READ) != AccessStatus.ALLOWED) {
            throw new AccessDeniedException("Access denied. You do not have the appropriate permissions to perform this operation");
        }

        switch (type) {
            case "file":
                break;
            case "diff":
                nodeRef = historyManager.getHistoryNodeByVersionNode(nodeRef, "diff.zip");

                if (nodeRef == null) {
                    throw new WebScriptException(Status.STATUS_NOT_FOUND, "Not found diff.zip for version: " + nodeRefString);
                }

                String editorUrl = urlManager.getEditorUrl();
                if (editorUrl.endsWith("/")) {
                    editorUrl = editorUrl.substring(0, editorUrl.length() - 1);
                }

                response.setHeader("Access-Control-Allow-Origin", editorUrl);
                break;
            default:
                throw new WebScriptException(404, "Unknown parameter 'type': '" + type + "'!");
        }

        String title = util.getTitle(nodeRef);
        String fileType = util.getExtension(nodeRef);
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

        response.setHeader("Content-Length", String.valueOf(reader.getSize()));
        response.setHeader("Content-Type", mimetypeService.getMimetype(fileType));
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8\'\'" + URLEncoder.encode(title));

        Writer writer = response.getWriter();
        BufferedInputStream inputStream = null;

        try {
            InputStream fileInputStream = reader.getContentInputStream();
            inputStream = new BufferedInputStream(fileInputStream);
            int readBytes = 0;
            while ((readBytes = inputStream.read()) != -1) {
                writer.write(readBytes);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            inputStream.close();
        }
    }
}
