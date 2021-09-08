package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/*
   Copyright (c) Ascensio System SIA 2021. All rights reserved.
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
    ConfigManager configManager;

    @Autowired
    JwtManager jwtManager;

    @Autowired
    Util util;

    @Autowired
    FavouritesService favouritesService;

    public JSONObject getConfigJson (NodeRef nodeRef, String sharedId, String username, String documentType,
            String docTitle, String docExt, Boolean preview, Boolean isReadOnly) throws Exception {
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
        info.put("favorite", favouritesService.isFavourite(username, nodeRef));
        documentObject.put("info", info);

        JSONObject permObject = new JSONObject();
        documentObject.put("permissions", permObject);
        JSONObject editorConfigObject = new JSONObject();
        configJson.put("editorConfig", editorConfigObject);

        mesService.registerResourceBundle("alfresco/messages/prepare");
        editorConfigObject.put("lang", mesService.getLocale().toLanguageTag());


        String mimeType = mimetypeService.getMimetype(docExt);
        editorConfigObject.put("createUrl", util.getCreateNewUrl(nodeRef, mimeType));
        boolean canWrite = util.isEditable(mimeType) && permissionService.hasPermission(nodeRef, PermissionService.WRITE) == AccessStatus.ALLOWED;

        editorConfigObject.put("templates", util.getTemplates(nodeRef, docExt, mimeType));
        if (isReadOnly || preview || !canWrite) {
            editorConfigObject.put("mode", "view");
            permObject.put("edit", false);
        } else {
            if (!cociService.isCheckedOut(nodeRef)) {
                NodeRef copyRef = cociService.checkout(nodeRef);
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
        JSONObject goBack = new JSONObject();
        goBack.put("url", util.getBackUrl(nodeRef));
        customizationObject.put("goback",goBack);

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
            configJson.put("token", jwtManager.createToken(configJson));
        }

        return configJson;
    }
}
