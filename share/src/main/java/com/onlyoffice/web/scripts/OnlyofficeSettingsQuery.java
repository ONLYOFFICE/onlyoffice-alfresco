/*
   Copyright (c) Ascensio System SIA 2023. All rights reserved.
   http://www.onlyoffice.com
*/

package com.onlyoffice.web.scripts;

import org.alfresco.error.AlfrescoRuntimeException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.extensions.webscripts.ScriptRemote;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Response;
import java.util.HashSet;
import java.util.Set;

public class OnlyofficeSettingsQuery {
    private static Set<String> editableFormats = new HashSet<String>();
    private static Boolean convertOriginal = false;
    private static JSONArray supportedFormats = new JSONArray();
    private static long timeLastRequest = 0;
    private ScriptRemote remote;

    public void setRemote(ScriptRemote remote) {
        this.remote = remote;
    }

    private void requestOnlyofficeSettingsFromRepo() {
        if ((System.nanoTime() - timeLastRequest)/1000000000 > 10) {
            Set<String> editableFormats = new HashSet<>();
            Response response = remote.call("/parashift/onlyoffice/onlyoffice-settings");
            if (response.getStatus().getCode() == Status.STATUS_OK) {
                timeLastRequest = System.nanoTime();
                try {
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(response.getResponse());

                    JSONArray formats = (JSONArray) json.get("editableFormats");
                    for (Object format : formats) {
                        editableFormats.add((String) format);
                    }
                    this.editableFormats = editableFormats;
                    this.convertOriginal = (Boolean) json.get("convertOriginal");
                    this.supportedFormats = (JSONArray) json.get("supportedFormats");
                } catch (Exception err) {
                    throw new AlfrescoRuntimeException("Failed to parse response from Alfresco: " + err.getMessage());
                }
            }
            else
            {
                throw new AlfrescoRuntimeException("Unable to retrieve editable mimetypes information from Alfresco: " + response.getStatus().getCode());
            }
        }
    }

    public Set<String> getEditableFormats() {
        requestOnlyofficeSettingsFromRepo();
        return editableFormats;
    }

    public Boolean getConvertOriginal() {
        requestOnlyofficeSettingsFromRepo();
        return convertOriginal;
    }

    public JSONArray getSupportedFormats() {
        requestOnlyofficeSettingsFromRepo();
        return supportedFormats;
    }
}
