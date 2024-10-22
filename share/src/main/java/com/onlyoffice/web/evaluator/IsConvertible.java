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


public class IsConvertible extends BaseEvaluator {
    private OnlyofficeSettingsQuery onlyofficeSettings;

    public void setOnlyofficeSettings(final OnlyofficeSettingsQuery onlyofficeSettings) {
        this.onlyofficeSettings = onlyofficeSettings;
    }

    @Override
    public boolean evaluate(final JSONObject jsonObject) {
        try {
            return hasPermission(jsonObject) && isConvertibleFormat(jsonObject);
        } catch (Exception err) {
            throw new AlfrescoRuntimeException("Failed to run action evaluator", err);
        }
    }

    private boolean hasPermission(final JSONObject jsonObject) {
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


    private boolean isConvertibleFormat(final JSONObject jsonObject) {
        String fileName = (String) jsonObject.get("fileName");
        String docExt = fileName.substring(fileName.lastIndexOf(".") + 1).trim().toLowerCase();

        for (Format format :  onlyofficeSettings.getSupportedFormats()) {
            if (format.getName().equals(docExt) && format.getType() != null) {
                switch (format.getType()) {
                    case WORD:
                        if (format.getName().equals("docxf") && format.getConvert().contains("pdf")) {
                            return true;
                        }
                        if (format.getConvert().contains("docx")) {
                            return true;
                        }
                        break;
                    case CELL:
                        if (format.getConvert().contains("xlsx")) {
                            return true;
                        }
                        break;
                    case SLIDE:
                        if (format.getConvert().contains("pptx")) {
                            return true;
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        return false;
    }
}
