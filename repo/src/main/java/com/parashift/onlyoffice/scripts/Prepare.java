package com.parashift.onlyoffice.scripts;

import com.parashift.onlyoffice.util.ConfigManager;
import com.parashift.onlyoffice.util.UrlManager;
import com.parashift.onlyoffice.util.Util;
import com.parashift.onlyoffice.util.UtilDocConfig;
import com.parashift.onlyoffice.constants.Type;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
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
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
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
    UrlManager urlManager;

    @Autowired
    UtilDocConfig utilDocConfig;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        mesService.registerResourceBundle("alfresco/messages/prepare");
        JSONObject responseJson = new JSONObject();

        try {
            if (request.getParameter("nodeRef") == null) {
                String newFileMime = request.getParameter("new");
                String parentNodeRefString = request.getParameter("parentNodeRef");

                if (newFileMime == null || newFileMime.isEmpty() || parentNodeRefString == null || parentNodeRefString.isEmpty()) {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Required query parameters not found");
                }

                logger.debug("Creating new node");
                NodeRef parentNodeRef = new NodeRef(parentNodeRefString);

                if (permissionService.hasPermission(parentNodeRef, PermissionService.CREATE_CHILDREN) != AccessStatus.ALLOWED) {
                    throw new SecurityException("User don't have the permissions to create child node");
                }

                String ext = mimetypeService.getExtension(newFileMime);
                String baseName = mesService.getMessage("onlyoffice.newdoc-filename-" + ext);

                String newName = util.getCorrectName(parentNodeRef, baseName, ext);

                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, newName);

                NodeRef nodeRef = this.nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newName), ContentModel.TYPE_CONTENT, props)
                        .getChildRef();

                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(newFileMime);

                String pathLocale = Util.PathLocale.get(mesService.getLocale().toLanguageTag());

                if (pathLocale == null) {
                    pathLocale = Util.PathLocale.get(mesService.getLocale().getLanguage());

                    if (pathLocale == null) pathLocale = Util.PathLocale.get("en");
                }

                InputStream in;
                if (request.getParameter("templateNodeRef") != null) {
                    NodeRef templateNodeRef = new NodeRef(request.getParameter("templateNodeRef"));
                    in = contentService.getReader(templateNodeRef, ContentModel.PROP_CONTENT).getContentInputStream();
                } else {
                    in = getClass().getResourceAsStream("/newdocs/" + pathLocale + "/new." + ext);
                }
                writer.putContent(in);
                util.ensureVersioningEnabled(nodeRef);
                util.postActivity(nodeRef, true);

                responseJson.put("nodeRef", nodeRef);
            } else {
                NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));
                boolean isReadOnly = request.getParameter("readonly") != null;

                if (permissionService.hasPermission(nodeRef, PermissionService.READ) != AccessStatus.ALLOWED) {
                    responseJson.put("error", "User have no read access");
                    response.setStatus(403);
                    response.getWriter().write(responseJson.toString(3));
                    return;
                }

                String docTitle = util.getTitle(nodeRef);
                String docExt = util.getExtension(nodeRef);
                String documentType = util.getDocType(docExt);
                if (docExt.equals("docxf") || docExt.equals("oform")) {
                    documentType = Type.WORD.name().toLowerCase();
                }

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
                responseJson.put("editorConfig", configJson);
                responseJson.put("onlyofficeUrl", urlManager.getEditorUrl());
                responseJson.put("mime", mimetypeService.getMimetype(docExt));
                responseJson.put("folderNode", util.getParentNodeRef(nodeRef));
                responseJson.put("demo", configManager.demoActive());
                responseJson.put("historyInfoUrl", urlManager.getHistoryInfoUrl(nodeRef));
                responseJson.put("historyDataUrl", urlManager.getHistoryDataUrl(nodeRef));
                responseJson.put("favorite", urlManager.getFavoriteUrl(nodeRef));
                responseJson.put("canManagePermissions", permissionService.hasPermission(nodeRef, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

                logger.debug("Sending JSON prepare object");
                logger.debug(responseJson.toString(3));
            }

            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");
            response.getWriter().write(responseJson.toString(3));
        } catch (JSONException ex) {
            throw new WebScriptException("Unable to serialize JSON: " + ex.getMessage());
        }
    }
}

