/*
   Copyright (c) Ascensio System SIA 2024. All rights reserved.
   http://www.onlyoffice.com
*/

package com.onlyoffice.web.scripts;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.model.common.Format;
import com.onlyoffice.web.model.OnlyofficeSettings;
import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.webscripts.ScriptRemote;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OnlyofficeSettingsQuery {
    private static Set<String> editableFormats = new HashSet<String>();
    private static Boolean convertOriginal = false;
    private static List<Format> supportedFormats = new ArrayList<>();
    private static long timeLastRequest = 0;
    private ScriptRemote remote;

    public void setRemote(final ScriptRemote remote) {
        this.remote = remote;
    }

    private void requestOnlyofficeSettingsFromRepo() {
        if ((System.nanoTime() - timeLastRequest)/1000000000 > 10) {
            Response response = remote.call("/parashift/onlyoffice/onlyoffice-settings");
            if (response.getStatus().getCode() == Status.STATUS_OK) {
                timeLastRequest = System.nanoTime();
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

                    OnlyofficeSettings settings = objectMapper.readValue(response.getResponse(),
                            OnlyofficeSettings.class);

                    for (Map.Entry<String, Boolean> format : settings.getEditableFormats().entrySet()) {
                        if (format.getValue()) {
                            this.editableFormats.add(format.getKey());
                        }
                    }
                    for (Format format : settings.getSupportedFormats()) {
                        if (format.getActions().contains("edit")) {
                            this.editableFormats.add(format.getName());
                        }
                    }


                    this.convertOriginal = settings.getConvertOriginal();
                    this.supportedFormats = settings.getSupportedFormats();
                } catch (Exception err) {
                    throw new AlfrescoRuntimeException("Failed to parse response from Alfresco: " + err.getMessage());
                }
            }
            else
            {
                throw new AlfrescoRuntimeException("Unable to retrieve editable mimetypes information from Alfresco: "
                        + response.getStatus().getCode());
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

    public List<Format> getSupportedFormats() {
        requestOnlyofficeSettingsFromRepo();
        return supportedFormats;
    }
}
