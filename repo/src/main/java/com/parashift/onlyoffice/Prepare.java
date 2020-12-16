package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by cetra on 20/10/15.
 * Sends Alfresco Share the necessaries to build up what information is needed for the OnlyOffice server
 */
 /*
    Copyright (c) Ascensio System SIA 2020. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.prepare.get")
public class Prepare extends AbstractWebScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("checkOutCheckInService")
    CheckOutCheckInService cociService;

    @Autowired
    NodeService nodeService;

    @Autowired
    ContentService contentService;

    @Autowired
    MimetypeService mimetypeService;

    @Autowired
    MessageService mesService;

    @Autowired
    PersonService personService;

    @Autowired
    ConfigManager configManager;

    @Autowired
    VersionService versionService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    JwtManager jwtManager;

    @Autowired
    Util util;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        mesService.registerResourceBundle("alfresco/messages/prepare");
        if (request.getParameter("nodeRef") != null) {
            boolean isReadOnly = request.getParameter("readonly") != null;

            String newFileMime = request.getParameter("new");
            NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));

            if (newFileMime != null && !newFileMime.isEmpty()) {
                logger.debug("Creating new node");

                String ext = mimetypeService.getExtension(newFileMime);

                String baseName = mesService.getMessage("onlyoffice.newdoc-filename-" + ext);
                String newName = baseName + "." + ext;

                NodeRef node = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, newName);
                if (node != null) {
                    Integer i = 0;
                    do {
                        i++;
                        newName = baseName + " (" +  Integer.toString(i) + ")." + ext;
                        node = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, newName);
                    } while (node != null);
                }

                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, newName);

                nodeRef = this.nodeService.createNode(nodeRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newName), ContentModel.TYPE_CONTENT, props)
                    .getChildRef();

                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(newFileMime);

                String tag = mesService.getLocale().toLanguageTag().substring(0, 2);
                if ("de en es fr it ru".indexOf(tag) == -1) tag = "en";

                InputStream in = getClass().getResourceAsStream("/newdocs/" + tag + "/new." + ext);

                writer.putContent(in);

                util.ensureVersioningEnabled(nodeRef);
            }

            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

            String previewParam = request.getParameter("preview");
            Boolean preview = previewParam != null && previewParam.equals("true");

            String docTitle = (String) properties.get(ContentModel.PROP_NAME);
            String docExt = docTitle.substring(docTitle.lastIndexOf(".") + 1).trim().toLowerCase();

            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");
            try {
                JSONObject responseJson = new JSONObject();
                JSONObject configJson = new JSONObject();
                JSONObject documentObject = new JSONObject();
                JSONObject editorConfigObject = new JSONObject();
                JSONObject userObject = new JSONObject();
                JSONObject permObject = new JSONObject();
                JSONObject customizationObject = new JSONObject();

                if (permissionService.hasPermission(nodeRef, PermissionService.READ) != AccessStatus.ALLOWED) {
                    responseJson.put("error", "User have no read access");
                    response.setStatus(403);
                    response.getWriter().write(responseJson.toString(3));
                    return;
                }

                if (getDocType(docExt) == null) {
                    responseJson.put("error", "File type is not supported");
                    response.setStatus(500);
                    response.getWriter().write(responseJson.toString(3));
                    return;
                }

                String mimeType = mimetypeService.getMimetype(docExt);

                responseJson.put("config", configJson);
                responseJson.put("onlyofficeUrl", util.getEditorUrl());
                responseJson.put("mime", mimeType);

                if (preview) {
                    if (((String)configManager.getOrDefault("webpreview", "")).equals("true")) {
                        responseJson.put("previewEnabled", true);
                    } else {
                        responseJson.put("previewEnabled", false);
                        response.getWriter().write(responseJson.toString(3));
                        return;
                    }
                }

                boolean canWrite = isEditable(mimeType) && permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.ALLOWED;

                String contentUrl = util.getContentUrl(nodeRef);
                String key = util.getKey(nodeRef);
                String username = AuthenticationUtil.getFullyAuthenticatedUser();
                NodeRef person = personService.getPersonOrNull(username);
                PersonInfo personInfo = null;
                if (person != null) {
                    personInfo = personService.getPerson(person);
                }

                configJson.put("type", "desktop");
                configJson.put("width", "100%");
                configJson.put("height", "100%");
                configJson.put("documentType", getDocType(docExt));

                configJson.put("document", documentObject);
                documentObject.put("title", docTitle);
                documentObject.put("url", contentUrl);
                documentObject.put("fileType", docExt);
                documentObject.put("key", key);
                documentObject.put("permissions", permObject);

                configJson.put("editorConfig", editorConfigObject);
                editorConfigObject.put("lang", mesService.getLocale().toLanguageTag());
                if (isReadOnly || preview || !canWrite) {
                    editorConfigObject.put("mode", "view");
                    permObject.put("edit", false);
                } else {
                    if (!cociService.isCheckedOut(nodeRef)) {
                        NodeRef copyRef = cociService.checkout(nodeRef);
                        nodeService.setProperty(copyRef, Util.EditingKeyAspect, key);
                        nodeService.setProperty(copyRef, Util.EditingHashAspect, util.generateHash());
                    }

                    editorConfigObject.put("mode", "edit");
                    permObject.put("edit", true);
                    editorConfigObject.put("callbackUrl", util.getCallbackUrl(nodeRef));
                }

                editorConfigObject.put("user", userObject);

                userObject.put("id", username);
                editorConfigObject.put("customization", customizationObject);
                if (preview) {
                    JSONObject embeddedObject = new JSONObject();
                    embeddedObject.put("saveUrl", contentUrl);
                    editorConfigObject.put("embedded", embeddedObject)
                }
                customizationObject.put("forcesave", configManager.getAsBoolean("forcesave"));

                if (personInfo == null) {
                    userObject.put("name", username);
                } else {
                    userObject.put("firstname", personInfo.getFirstName());
                    userObject.put("lastname", personInfo.getLastName());
                    userObject.put("name", personInfo.getFirstName() + " " + personInfo.getLastName());
                }

                if (jwtManager.jwtEnabled()) {
                    configJson.put("token", jwtManager.createToken(configJson));
                }

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

    private static Set<String> EditableSet = new HashSet<String>() {{
        add("text/plain");
        add("text/csv");
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        add("application/vnd.openxmlformats-officedocument.presentationml.presentation");
    }};

    private boolean isEditable(String mime) {
        return EditableSet.contains(mime);
    }

    private String getDocType(String ext) {
        if (".doc.docx.docm.dot.dotx.dotm.odt.fodt.ott.rtf.txt.html.htm.mht.pdf.djvu.fb2.epub.xps".indexOf(ext) != -1) return "text";
        if (".xls.xlsx.xlsm.xlt.xltx.xltm.ods.fods.ots.csv".indexOf(ext) != -1) return "spreadsheet";
        if (".pps.ppsx.ppsm.ppt.pptx.pptm.pot.potx.potm.odp.fodp.otp".indexOf(ext) != -1) return "presentation";
        return null;
    }
}
