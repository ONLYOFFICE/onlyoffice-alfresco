/*
   Copyright (c) Ascensio System SIA 2024. All rights reserved.
   http://www.onlyoffice.com
*/

package com.onlyoffice.web.evaluator;

import com.onlyoffice.model.common.Format;
import com.onlyoffice.web.scripts.OnlyofficeSettingsQuery;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;

public class IsViewable extends BaseEvaluator {
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

                boolean canView = false;

                for (Format format : onlyofficeSettings.getSupportedFormats()) {
                    if (format.getName().equals(docExt)
                            && format.getActions().contains("view")
                            && !onlyofficeSettings.getEditableFormats().contains(docExt)) {
                        canView = true;
                    }
                }

                return canView && !onlyofficeSettings.getEditableFormats().contains(docExt);
            }
        } catch (Exception err) {
            throw new AlfrescoRuntimeException("Failed to run action evaluator", err);
        }

        return false;
    }
}
