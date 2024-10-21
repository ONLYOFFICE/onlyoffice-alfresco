/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/
/**
 * Created by cetra on 20/10/15.
 * Sends Alfresco Share the necessaries to build up what information is needed for the OnlyOffice server
 */

package com.parashift.onlyoffice.scripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.model.documenteditor.Config;
import com.onlyoffice.model.documenteditor.config.document.DocumentType;
import com.onlyoffice.model.documenteditor.config.document.Type;
import com.onlyoffice.model.documenteditor.config.editorconfig.Mode;
import com.onlyoffice.service.documenteditor.config.ConfigService;
import com.parashift.onlyoffice.sdk.manager.url.UrlManager;
import com.parashift.onlyoffice.util.Util;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


@Component(value = "webscript.onlyoffice.prepare.get")
public class Prepare extends AbstractWebScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("checkOutCheckInService")
    private CheckOutCheckInService cociService;

    @Autowired
    private OwnableService ownableService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private MessageService mesService;

    @Autowired
    private MimetypeService mimetypeService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private Util util;

    @Autowired
    private UrlManager urlManager;

    @Autowired
    private ConfigService configService;

    @Autowired
    private DocumentManager documentManager;

    @Autowired
    private SettingsManager settingsManager;

    @Override
    public void execute(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
        mesService.registerResourceBundle("alfresco/messages/prepare");
        JSONObject responseJson = new JSONObject();

        try {
            if (request.getParameter("nodeRef") == null) {
                String newFileMime = request.getParameter("new");
                String parentNodeRefString = request.getParameter("parentNodeRef");

                if (newFileMime == null
                        || newFileMime.isEmpty()
                        || parentNodeRefString == null
                        || parentNodeRefString.isEmpty()) {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Required query parameters not found");
                }

                logger.debug("Creating new node");
                NodeRef parentNodeRef = new NodeRef(parentNodeRefString);

                if (permissionService.hasPermission(parentNodeRef, PermissionService.CREATE_CHILDREN)
                        != AccessStatus.ALLOWED) {
                    throw new SecurityException("User don't have the permissions to create child node");
                }

                String ext = mimetypeService.getExtension(newFileMime);
                String baseName = mesService.getMessage("onlyoffice.newdoc-filename-" + ext);

                String newName = util.getCorrectName(parentNodeRef, baseName, ext);

                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, newName);

                NodeRef nodeRef = this.nodeService.createNode(
                        parentNodeRef,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newName),
                        ContentModel.TYPE_CONTENT,
                        props
                ).getChildRef();

                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(newFileMime);

                InputStream in = documentManager.getNewBlankFile(ext, mesService.getLocale());

                writer.putContent(in);
                util.ensureVersioningEnabled(nodeRef);
                util.postActivity(nodeRef, true);

                responseJson.put("nodeRef", nodeRef);
            } else {
                NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));
                boolean readonly = request.getParameter("readonly") != null
                        && request.getParameter("readonly").equals("1");

                if (permissionService.hasPermission(nodeRef, PermissionService.READ) != AccessStatus.ALLOWED) {
                    responseJson.put("error", "User have no read access");
                    response.setStatus(Status.STATUS_FORBIDDEN);
                    response.getWriter().write(responseJson.toString());
                    return;
                }

                String fileName = documentManager.getDocumentName(nodeRef.toString());
                String fileExtension = documentManager.getExtension(fileName);
                DocumentType documentType = documentManager.getDocumentType(fileName);

                if (documentType == null) {
                    responseJson.put("error", "File type is not supported");
                    response.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
                    response.getWriter().write(responseJson.toString());
                    return;
                }

                String previewParam = request.getParameter("preview");
                Boolean preview = previewParam != null && previewParam.equals("true");

                com.onlyoffice.model.documenteditor.config.document.Type type = Type.DESKTOP;
                Mode mode = readonly ? Mode.VIEW : Mode.EDIT;
                responseJson.put("previewEnabled", false);

                if (preview) {
                    if (settingsManager.getSettingBoolean("webpreview", false)) {
                        type = Type.EMBEDDED;
                        mode = Mode.VIEW;
                        responseJson.put("previewEnabled", true);
                    } else {
                        response.getWriter().write(responseJson.toString());
                        return;
                    }
                }

                if ((documentManager.isEditable(fileName) || documentManager.isFillable(fileName))
                        && permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.ALLOWED
                        && mode.equals(Mode.EDIT)) {
                    if (!cociService.isCheckedOut(nodeRef)) {
                        util.ensureVersioningEnabled(nodeRef);
                        NodeRef copyRef = cociService.checkout(nodeRef);
                        ownableService.setOwner(copyRef, ownableService.getOwner(nodeRef));
                        nodeService.setProperty(
                                copyRef,
                                Util.EDITING_KEY_ASPECT,
                                documentManager.getDocumentKey(nodeRef.toString(), false)
                        );
                        nodeService.setProperty(copyRef, Util.EDITING_HASH_ASPECT, util.generateHash());
                    }
                }

                Config config = configService.createConfig(
                        request.getParameter("nodeRef"),
                        mode,
                        type
                );

                config.getEditorConfig().setLang(mesService.getLocale().toLanguageTag());

                ObjectMapper mapper = new ObjectMapper();

                responseJson.put("editorConfig", new JSONObject(mapper.writeValueAsString(config)));
                responseJson.put("onlyofficeUrl", urlManager.getDocumentServerUrl() + "/");
                responseJson.put("mime", mimetypeService.getMimetype(fileExtension));
                responseJson.put("folderNode", util.getParentNodeRef(nodeRef));
                responseJson.put("demo", settingsManager.isDemoActive());
                responseJson.put("historyInfoUrl", urlManager.getHistoryInfoUrl(nodeRef));
                responseJson.put("historyDataUrl", urlManager.getHistoryDataUrl(nodeRef));
                responseJson.put("favorite", urlManager.getFavoriteUrl(nodeRef));
                responseJson.put("canManagePermissions", permissionService.hasPermission(
                        nodeRef, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

                logger.debug("Sending JSON prepare object");
                logger.debug(responseJson.toString());
            }

            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");
            response.getWriter().write(responseJson.toString());
        } catch (JSONException ex) {
            throw new WebScriptException("Unable to serialize JSON: " + ex.getMessage());
        }
    }
}

