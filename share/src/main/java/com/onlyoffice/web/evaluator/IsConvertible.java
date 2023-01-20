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

public class IsConvertible extends BaseEvaluator {
    private OnlyofficeSettingsQuery onlyofficeSettings;

    public void setOnlyofficeSettings(OnlyofficeSettingsQuery onlyofficeSettings) {
        this.onlyofficeSettings = onlyofficeSettings;
    }

    @Override
    public boolean evaluate(JSONObject jsonObject) {
        try {
            return hasPermission(jsonObject) && isConvertibleFormat(jsonObject);
        } catch (Exception err) {
            throw new AlfrescoRuntimeException("Failed to run action evaluator", err);
        }
    }

    private boolean hasPermission (JSONObject jsonObject) {
        if (onlyofficeSettings.getConvertOriginal()) {
            JSONObject node = (JSONObject) jsonObject.get("node");
            if (node != null && node.containsKey("permissions")) {
                JSONObject perm = (JSONObject) node.get("permissions");
                if (perm != null && perm.containsKey("user")) {
                    JSONObject user = (JSONObject) perm.get("user");
                    if (user != null && (boolean) user.getOrDefault("Write", false)) {
                        return true;
                    }
                }
            }
        } else {
            JSONObject parent = (JSONObject) jsonObject.get("parent");
            if (parent != null && parent.containsKey("permissions")) {
                JSONObject perm = (JSONObject) parent.get("permissions");
                if (perm != null && perm.containsKey("user")) {
                    JSONObject user = (JSONObject) perm.get("user");
                    if (user != null && (boolean) user.getOrDefault("CreateChildren", false)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    private boolean isConvertibleFormat(JSONObject jsonObject) {
        String fileName = (String) jsonObject.get("fileName");
        String docExt = fileName.substring(fileName.lastIndexOf(".") + 1).trim().toLowerCase();

        JSONArray supportedFormats = onlyofficeSettings.getSupportedFormats();

        for (int i = 0; i < supportedFormats.size(); i++) {
            JSONObject format = (JSONObject) supportedFormats.get(i);
            JSONArray outputTypes = (JSONArray) format.get("convertTo");

            if (format.get("name").equals(docExt)) {
                if (docExt.equals("txt") || docExt.equals("csv")) return false;
                switch (format.get("type").toString()) {
                    case "FORM":
                        if (outputTypes.contains("oform")) return true;
                        break;
                    case "WORD":
                        if (outputTypes.contains("docx")) return true;
                        break;
                    case "CELL":
                        if (outputTypes.contains("xlsx")) return true;
                        break;
                    case "SLIDE":
                        if (outputTypes.contains("pptx")) return true;
                        break;
                    default:
                        break;
                }
            }
        }

        return false;
    }
}