package com.parashift.onlyoffice.util;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.stereotype.Service;

/*
   Copyright (c) Ascensio System SIA 2022. All rights reserved.
   http://www.onlyoffice.com
*/

@Service
public class UtilDocConfig {

    @Autowired
    @Qualifier("checkOutCheckInService")
    CheckOutCheckInService cociService;

    @Autowired
    MessageService mesService;

    @Autowired
    MimetypeService mimetypeService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    NodeService nodeService;

    @Autowired
    PersonService personService;

    @Autowired
    OwnableService ownableService;

    @Autowired
    ConfigManager configManager;

    @Autowired
    JwtManager jwtManager;

    @Autowired
    Util util;

    @Autowired
    FavouritesService favouritesService;

    public JSONObject getConfigJson (NodeRef nodeRef, String sharedId, String username, String documentType,
            String docTitle, String docExt, Boolean preview, Boolean isReadOnly) throws JSONException {
        JSONObject configJson = new JSONObject();

        configJson.put("type", preview ? "embedded" : "desktop");
        configJson.put("width", "100%");
        configJson.put("height", "100%");
        configJson.put("documentType", documentType);

        JSONObject documentObject = new JSONObject();
        configJson.put("document", documentObject);
        documentObject.put("title", docTitle);
        documentObject.put("url", util.getContentUrl(nodeRef));
        documentObject.put("fileType", docExt);
        documentObject.put("key", util.getKey(nodeRef));

        JSONObject info = new JSONObject();
        if (username != null) {
            info.put("favorite", favouritesService.isFavourite(username, nodeRef));
        }
        documentObject.put("info", info);

        JSONObject permObject = new JSONObject();
        documentObject.put("permissions", permObject);
        JSONObject editorConfigObject = new JSONObject();
        configJson.put("editorConfig", editorConfigObject);

        mesService.registerResourceBundle("alfresco/messages/prepare");
        editorConfigObject.put("lang", mesService.getLocale().toLanguageTag());


        String mimeType = mimetypeService.getMimetype(docExt);
        editorConfigObject.put("createUrl", util.getCreateNewUrl(nodeRef, docExt));
        boolean canWrite = util.isEditable(docExt) && permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.ALLOWED;

        editorConfigObject.put("templates", util.getTemplates(nodeRef, docExt));
        if (isReadOnly || preview || !canWrite) {
            editorConfigObject.put("mode", "view");
            permObject.put("edit", false);
        } else {
            if (!cociService.isCheckedOut(nodeRef)) {
                NodeRef copyRef = cociService.checkout(nodeRef);
                ownableService.setOwner(copyRef, ownableService.getOwner(nodeRef));
                nodeService.setProperty(copyRef, Util.EditingKeyAspect, documentObject.getString("key"));
                nodeService.setProperty(copyRef, Util.EditingHashAspect, util.generateHash());
            }

            editorConfigObject.put("mode", "edit");
            permObject.put("edit", true);
            editorConfigObject.put("callbackUrl", util.getCallbackUrl(nodeRef));
        }

        if (preview) {
            JSONObject embeddedObject = new JSONObject();
            editorConfigObject.put("embedded", embeddedObject);
            if (sharedId != null) {
                embeddedObject.put("saveUrl", util.getEmbeddedSaveUrl(sharedId, docTitle));
            } else {
                embeddedObject.put("saveUrl", util.getEmbeddedSaveUrl(nodeRef, docTitle));
            }

        }

        JSONObject customizationObject = new JSONObject();
        editorConfigObject.put("customization", customizationObject);
        customizationObject.put("forcesave", configManager.getAsBoolean("forcesave", "false"));

        if (!preview) {
            JSONObject goBack = new JSONObject();
            goBack.put("url", util.getBackUrl(nodeRef));
            customizationObject.put("goback", goBack);
        }
        customizationObject.put("chat", configManager.getAsBoolean("chat", "true"));
        customizationObject.put("help", configManager.getAsBoolean("help", "true"));
        customizationObject.put("compactHeader", configManager.getAsBoolean("compactHeader", "false"));
        customizationObject.put("toolbarNoTabs", configManager.getAsBoolean("toolbarNoTabs", "false"));
        customizationObject.put("feedback", configManager.getAsBoolean("feedback", "false"));
        customizationObject.put("review", configManager.getOrDefault("reviewDisplay", "original"));

        JSONObject userObject = new JSONObject();
        editorConfigObject.put("user", userObject);

        NodeRef person = personService.getPersonOrNull(username);
        PersonInfo personInfo = null;
        if (person != null) {
            personInfo = personService.getPerson(person);
        }

        userObject.put("id", username);
        if (personInfo == null) {
            userObject.put("name", username);
        } else {
            userObject.put("firstname", personInfo.getFirstName());
            userObject.put("lastname", personInfo.getLastName());
            userObject.put("name", personInfo.getFirstName() + " " + personInfo.getLastName());
        }

        if (jwtManager.jwtEnabled()) {
            try {
                configJson.put("token", jwtManager.createToken(configJson));
            } catch (Exception e) {
                throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Token creation error", e);
            }
        }

        return configJson;
    }
}
