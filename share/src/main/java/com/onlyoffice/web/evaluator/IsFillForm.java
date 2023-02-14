/*
   Copyright (c) Ascensio System SIA 2023. All rights reserved.
   http://www.onlyoffice.com
*/
package com.onlyoffice.web.evaluator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class IsFillForm extends BaseEvaluator {
    @Override
    public boolean evaluate(JSONObject jsonObject) {
        try {
            String fileName = (String) jsonObject.get("fileName");
            if (fileName != null) {
                String docExt = fileName.substring(fileName.lastIndexOf(".") + 1).trim().toLowerCase();

                if (docExt.equals("oform")) return true;
            }
        } catch (Exception err) {
            throw new AlfrescoRuntimeException("Failed to run action evaluator", err);
        }
        return false;
    }
}
