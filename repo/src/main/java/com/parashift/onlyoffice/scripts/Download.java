package com.parashift.onlyoffice.scripts;

import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.security.JwtManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.parashift.onlyoffice.sdk.manager.url.UrlManager;
import com.parashift.onlyoffice.util.HistoryManager;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import org.springframework.stereotype.Component;
import org.springframework.extensions.surf.util.URLEncoder;

import java.io.*;

/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.download.get")
public class Download extends AbstractWebScript {

    @Autowired
    private ContentService contentService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private JwtManager jwtManager;

    @Autowired
    private MimetypeService mimetypeService;

    @Autowired
    private HistoryManager historyManager;

    @Autowired
    private UrlManager urlManager;

    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private DocumentManager documentManager;

    @Autowired
    private ContentStreamer contentStreamer;

    @Override
    public void execute(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
        String nodeRefString = request.getParameter("nodeRef");
        String type = request.getServiceMatch().getTemplateVars().get("type");

        if (type == null || type.isEmpty()) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not find required 'type' parameter!");
        }

        if (nodeRefString == null || nodeRefString.isEmpty()) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not find required 'nodeRef' parameter!");
        }

        if (settingsManager.isSecurityEnabled() ) {
            String jwth = settingsManager.getSecurityHeader();
            String header = request.getHeader(jwth);
            String authorizationPrefix = settingsManager.getSecurityPrefix();
            String token = (header != null && header.startsWith(authorizationPrefix))
                    ? header.substring(authorizationPrefix.length()) : header;

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
            throw new AccessDeniedException("Access denied. You do not have the appropriate permissions" +
                    "to perform this operation");
        }

        switch (type) {
            case "file":
                break;
            case "diff":
                nodeRef = historyManager.getHistoryNodeByVersionNode(nodeRef, "diff.zip");

                if (nodeRef == null) {
                    throw new WebScriptException(
                            Status.STATUS_NOT_FOUND,
                            "Not found diff.zip for version: " + nodeRefString
                    );
                }

                String editorUrl = urlManager.getDocumentServerUrl();
                if (editorUrl.endsWith("/")) {
                    editorUrl = editorUrl.substring(0, editorUrl.length() - 1);
                }

                response.setHeader("Access-Control-Allow-Origin", editorUrl);
                break;
            default:
                throw new WebScriptException(404, "Unknown parameter 'type': '" + type + "'!");
        }

        String title = documentManager.getDocumentName(nodeRef.toString());

        contentStreamer.streamContent(request, response, nodeRef, ContentModel.PROP_CONTENT, true, title,null);
    }
}
