/*
   Copyright (c) Ascensio System SIA 2021. All rights reserved.
   http://www.onlyoffice.com
*/

package com.onlyoffice.web.evaluator;

import com.onlyoffice.web.scripts.OnlyofficeSettingsQuery;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class IsEditable extends BaseEvaluator {
    private OnlyofficeSettingsQuery onlyofficeSettings;

    public void setOnlyofficeSettings(OnlyofficeSettingsQuery onlyofficeSettings) {
        this.onlyofficeSettings = onlyofficeSettings;
    }

    @Override
    public boolean evaluate(JSONObject jsonObject) {
        try {
            String fileName = (String) jsonObject.get("fileName");
            if (fileName != null) {
                String docExt = fileName.substring(fileName.lastIndexOf(".") + 1).trim().toLowerCase();

                JSONArray supportedFormats = onlyofficeSettings.getSupportedFormats();
                boolean defaultEditFormat = false;

                for (int i = 0; i < supportedFormats.size(); i++) {
                    JSONObject format = (JSONObject) supportedFormats.get(i);

                    if (format.get("name").equals(docExt)) {
                        defaultEditFormat = Boolean.parseBoolean(format.get("edit").toString());
                        break;
                    }
                }

                if (defaultEditFormat || onlyofficeSettings.getEditableFormats().contains(docExt)) {
                    return true;
                }
            }
        } catch (Exception err) {
            throw new AlfrescoRuntimeException("Failed to run action evaluator", err);
        }

        return false;
    }
}
