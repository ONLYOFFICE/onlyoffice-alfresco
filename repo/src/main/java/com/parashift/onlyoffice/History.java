package com.parashift.onlyoffice;


import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;
import org.alfresco.repo.security.permissions.AccessDeniedException;

import java.io.IOException;

/*
    Copyright (c) Ascensio System SIA 2021. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.history.get")
public class History extends AbstractWebScript {

    @Autowired
    Util util;

    @Autowired
    PermissionService permissionService;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setContentEncoding("UTF-8");
        if (request.getParameter("nodeRef") != null) {
            NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));
            if (permissionService.hasPermission(nodeRef, PermissionService.READ) != AccessStatus.ALLOWED) {
                throw new AccessDeniedException("Access denied. You do not have the appropriate permissions to perform this operation");
            }
            JSONObject history = util.getHistoryObj(nodeRef);
            if (request.getParameter("version") == null) {
                try {
                    response.getWriter().write(history.getJSONArray("history").toString(3));
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new WebScriptException("Error on casting json to string: " + e.getMessage());
                }
            } else {
                Boolean versionExist = false;
                String version = request.getParameter("version");
                try {
                    JSONArray historyDataArray = history.getJSONArray("data");
                    for (int i = 0; i < historyDataArray.length(); i++) {
                        if (version.equals(historyDataArray.getJSONObject(i).getString("version"))) {
                            versionExist = true;
                            response.getWriter().write(historyDataArray.getJSONObject(i).toString(3));
                        }
                    }
                    if (!versionExist) {
                        response.getWriter().write((String) null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new WebScriptException("Error with historyData array :" + e.getMessage());
                }
            }
        }
    }
}
