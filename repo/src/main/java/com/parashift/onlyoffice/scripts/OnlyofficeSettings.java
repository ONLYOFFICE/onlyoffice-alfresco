package com.parashift.onlyoffice.scripts;

import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.settings.SettingsManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.onlyoffice-settings.get")
public class OnlyofficeSettings extends AbstractWebScript {
    @Autowired
    SettingsManager settingsManager;

    @Autowired
    DocumentManager documentManager;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        JSONObject responseJson = new JSONObject();
        try {
            responseJson.put("editableFormats", documentManager.getLossyEditableMap());
            responseJson.put("convertOriginal", settingsManager.getSettingBoolean("convertOriginal", false));
            responseJson.put("supportedFormats", documentManager.getFormats());

            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");
            response.getWriter().write(responseJson.toString());
        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON: " + e.getMessage());
        }
    }
}

