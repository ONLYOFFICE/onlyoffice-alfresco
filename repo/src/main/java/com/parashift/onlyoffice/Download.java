package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import org.springframework.stereotype.Component;
import org.springframework.extensions.surf.util.URLEncoder;

import java.io.*;
import java.util.Map;

/*
    Copyright (c) Ascensio System SIA 2021. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.download.get")
public class Download extends AbstractWebScript {

    @Autowired
    ContentService contentService;

    @Autowired
    NodeService nodeService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    JwtManager jwtManager;

    @Autowired
    MimetypeService mimetypeService;

    @Autowired
    Util util;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        if (request.getParameter("nodeRef") != null) {
            String zipParam = request.getParameter("wjc");
            if (jwtManager.jwtEnabled() && zipParam == null) {
                String jwth = jwtManager.getJwtHeader();
                String header = request.getHeader(jwth);
                String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : header;

                if (token == null || token == "") {
                    throw new SecurityException("Expected JWT");
                }

                if (!jwtManager.verify(token)) {
                    throw new SecurityException("JWT verification failed");
                }
            }

            NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));

            if (permissionService.hasPermission(nodeRef, PermissionService.READ) != AccessStatus.ALLOWED) {
                throw new AccessDeniedException("Access denied. You do not have the appropriate permissions to perform this operation");
            }
            if (zipParam != null) {
                NodeRef parentVersionNode = nodeService.getParentAssocs(nodeRef).get(0).getParentRef();
                if (permissionService.hasPermission(parentVersionNode, PermissionService.READ) != AccessStatus.ALLOWED) {
                    throw new AccessDeniedException("Access denied. You do not have the appropriate permissions to perform this operation");
                }
            }
            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

            String name = (String) properties.get(ContentModel.PROP_NAME);
            String docExt = name.substring(name.lastIndexOf(".") + 1).trim().toLowerCase();

            String editorUrl = util.getEditorUrl();
            if (editorUrl.endsWith("/")) {
                editorUrl = editorUrl.substring(0, editorUrl.length() - 1);
            }

            response.setHeader("Access-Control-Allow-Origin", editorUrl);
            response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,HEAD");
            response.setHeader("Content-Length", String.valueOf(reader.getSize()));
            response.setHeader("Content-Type", mimetypeService.getMimetype(docExt));
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8\'\'" + URLEncoder.encode(name));

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
        } else {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not find required 'nodeRef' parameter");
        }
    }
}
