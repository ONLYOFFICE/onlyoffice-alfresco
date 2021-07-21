package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cetra on 20/10/15.
 * Sends Alfresco Share the necessaries to build up what information is needed for the OnlyOffice server
 */
 /*
    Copyright (c) Ascensio System SIA 2021. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.prepare.get")
public class Prepare extends AbstractWebScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    NodeService nodeService;

    @Autowired
    ContentService contentService;

    @Autowired
    MessageService mesService;

    @Autowired
    MimetypeService mimetypeService;

    @Autowired
    ConfigManager configManager;

    @Autowired
    PermissionService permissionService;

    @Autowired
    Util util;

    @Autowired
    UtilDocConfig utilDocConfig;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        mesService.registerResourceBundle("alfresco/messages/prepare");

        if (request.getParameter("nodeRef") != null) {
            boolean isReadOnly = request.getParameter("readonly") != null;

            String newFileMime = request.getParameter("new");
            NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));

            if (newFileMime != null && !newFileMime.isEmpty()) {
                logger.debug("Creating new node");
                if (permissionService.hasPermission(nodeRef, PermissionService.CREATE_CHILDREN) == AccessStatus.ALLOWED) {
                    String ext = mimetypeService.getExtension(newFileMime);
                    String baseName = mesService.getMessage("onlyoffice.newdoc-filename-" + ext);

                    String newName = util.getCorrectName(nodeRef, baseName, ext);

                    Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                    props.put(ContentModel.PROP_NAME, newName);

                    nodeRef = this.nodeService.createNode(nodeRef, ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newName), ContentModel.TYPE_CONTENT, props)
                            .getChildRef();

                    ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setMimetype(newFileMime);

                    String pathLocale = Util.PathLocale.get(mesService.getLocale().toLanguageTag());

                    if (pathLocale == null) {
                        pathLocale = Util.PathLocale.get(mesService.getLocale().getLanguage());

                        if (pathLocale == null) pathLocale = Util.PathLocale.get("en");
                    }

                    InputStream in = getClass().getResourceAsStream("/newdocs/" + pathLocale + "/new." + ext);

                    writer.putContent(in);

                    util.ensureVersioningEnabled(nodeRef);
                } else {
                    throw new SecurityException("User don't have the permissions to create child node");
                }
            }

            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");
            try {
                JSONObject responseJson = new JSONObject();

                if (permissionService.hasPermission(nodeRef, PermissionService.READ) != AccessStatus.ALLOWED) {
                    responseJson.put("error", "User have no read access");
                    response.setStatus(403);
                    response.getWriter().write(responseJson.toString(3));
                    return;
                }

                Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
                String docTitle = (String) properties.get(ContentModel.PROP_NAME);
                String docExt = docTitle.substring(docTitle.lastIndexOf(".") + 1).trim().toLowerCase();
                String documentType = util.getDocType(docExt);

                if (documentType == null) {
                    responseJson.put("error", "File type is not supported");
                    response.setStatus(500);
                    response.getWriter().write(responseJson.toString(3));
                    return;
                }

                String previewParam = request.getParameter("preview");
                Boolean preview = previewParam != null && previewParam.equals("true");

                if (preview) {
                    if (((String)configManager.getOrDefault("webpreview", "")).equals("true")) {
                        responseJson.put("previewEnabled", true);
                    } else {
                        responseJson.put("previewEnabled", false);
                        response.getWriter().write(responseJson.toString(3));
                        return;
                    }
                }

                String username = AuthenticationUtil.getFullyAuthenticatedUser();

                JSONObject configJson = utilDocConfig.getConfigJson(nodeRef, null, username, documentType, docTitle,
                        docExt, preview, isReadOnly);
                responseJson.put("config", configJson);
                responseJson.put("onlyofficeUrl", util.getEditorUrl());
                responseJson.put("mime", mimetypeService.getMimetype(docExt));
                responseJson.put("demo", configManager.demoActive());
                responseJson.put("documentType", documentType);

                logger.debug("Sending JSON prepare object");
                logger.debug(responseJson.toString(3));

                response.getWriter().write(responseJson.toString(3));
            } catch (JSONException ex) {
                throw new WebScriptException("Unable to serialize JSON: " + ex.getMessage());
            } catch (Exception ex) {
                throw new WebScriptException("Unable to create JWT token: " + ex.getMessage());
            }
        }
    }
}

