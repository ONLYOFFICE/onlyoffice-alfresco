/*
    Copyright (c) Ascensio System SIA 2025. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.scripts;

import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.settings.SettingsManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component(value = "webscript.onlyoffice.onlyoffice-settings.get")
public class OnlyofficeSettings extends AbstractWebScript {
    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private DocumentManager documentManager;

    @Override
    public void execute(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
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

