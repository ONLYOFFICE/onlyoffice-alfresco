/*
    Copyright (c) Ascensio System SIA 2026. All rights reserved.
    http://www.onlyoffice.com
*/

package com.onlyoffice.web.evaluator;

import com.onlyoffice.web.scripts.OnlyofficeSettingsQuery;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IsViewable extends BaseEvaluator {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private OnlyofficeSettingsQuery onlyofficeSettings;

    public void setOnlyofficeSettings(final OnlyofficeSettingsQuery onlyofficeSettings) {
        this.onlyofficeSettings = onlyofficeSettings;
    }

    @Override
    public boolean evaluate(final JSONObject jsonObject) {
        try {
            String fileName = (String) jsonObject.get("fileName");
            if (fileName != null) {
                String docExt = fileName.substring(fileName.lastIndexOf(".") + 1).trim().toLowerCase();

                 return onlyofficeSettings.getSupportedFormats().stream()
                        .anyMatch(format -> {
                           return format.getName().equals(docExt) && format.getActions().contains("view");
                        });
            }
        } catch (Exception err) {
            throw new AlfrescoRuntimeException("Failed to run action evaluator", err);
        }

        return false;
    }
}
