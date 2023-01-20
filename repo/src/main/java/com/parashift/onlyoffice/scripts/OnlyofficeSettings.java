package com.parashift.onlyoffice.scripts;

import com.parashift.onlyoffice.util.ConfigManager;
import com.parashift.onlyoffice.constants.Formats;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/*
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.onlyoffice-settings.get")
public class OnlyofficeSettings extends AbstractWebScript {

    @Autowired
    ConfigManager configManager;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        JSONObject responseJson = new JSONObject();
        try {
            Set<String> editableFormats = configManager.getCustomizableEditableSet();
            responseJson.put("editableFormats", editableFormats);
            responseJson.put("convertOriginal", configManager.getAsBoolean("convertOriginal", "false"));
            responseJson.put("supportedFormats", Formats.getSupportedFormatsAsJson());

            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");
            response.getWriter().write(responseJson.toString());
        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON: " + e.getMessage());
        }
    }
}

