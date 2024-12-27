/*
    Copyright (c) Ascensio System SIA 2025. All rights reserved.
    http://www.onlyoffice.com
*/

package com.onlyoffice.web.evaluator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;


public class IsWritePermission extends BaseEvaluator {

    @Override
    public boolean evaluate(final JSONObject jsonObject) {
        try {
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

            return false;
        } catch (Exception err) {
            throw new AlfrescoRuntimeException("Failed to run action evaluator", err);
        }
    }
}
