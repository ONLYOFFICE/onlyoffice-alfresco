/*
   Copyright (c) Ascensio System SIA 2023. All rights reserved.
   http://www.onlyoffice.com
*/

package com.onlyoffice.web.evaluator;

import com.onlyoffice.web.scripts.OnlyofficeSettingsQuery;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class IsCorrectDownloadAs extends BaseEvaluator {
    private OnlyofficeSettingsQuery onlyofficeSettings;

    public void setOnlyofficeSettings(OnlyofficeSettingsQuery onlyofficeSettings) {
        this.onlyofficeSettings = onlyofficeSettings;
    }

    @Override
    public boolean evaluate(JSONObject jsonObject) {
        try {
            String docName = jsonObject.get("displayName").toString();
            String docExt = docName.substring(docName.lastIndexOf(".") + 1);
            return isSuppotredFormats(docExt);
        } catch (Exception err) {
            throw new AlfrescoRuntimeException("Failed to run action evaluator", err);
        }
    }

    private boolean isSuppotredFormats(String ext) {
        JSONArray supportedFormats = onlyofficeSettings.getSupportedFormats();
        for (int i = 0; i < supportedFormats.size(); i++) {
            JSONObject format = (JSONObject) supportedFormats.get(i);
            JSONArray outputTypes = (JSONArray) format.get("convertTo");
            if (format.get("name").equals(ext) && outputTypes != null && outputTypes.size() > 0){
                return true;
            }
        }

        return false;
    }
}