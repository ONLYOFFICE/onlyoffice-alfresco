/*
   Copyright (c) Ascensio System SIA 2024. All rights reserved.
   http://www.onlyoffice.com
*/

package com.onlyoffice.web.evaluator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;

public class IsOnlyEditEvaluator extends BaseEvaluator {

    @Override
    public boolean evaluate(JSONObject jsonObject) {
        try {
            JSONObject node = (JSONObject) jsonObject.get("node");
            if (node == null) return false;

            JSONObject properties = (JSONObject) node.get("properties");
            if (properties == null) return false;

            if (properties.get(":onlyoffice:editing-hash") != null) {
                return true;
            }

            return false;
        } catch (Exception err) {
            throw new AlfrescoRuntimeException("Failed to run action evaluator", err);
        }
    }

}
