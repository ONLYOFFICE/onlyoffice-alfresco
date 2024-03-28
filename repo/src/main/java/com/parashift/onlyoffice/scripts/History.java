package com.parashift.onlyoffice.scripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.model.documenteditor.HistoryData;
import com.parashift.onlyoffice.util.HistoryManager;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import org.springframework.stereotype.Component;
import org.alfresco.repo.security.permissions.AccessDeniedException;

import java.io.*;
import java.util.*;

/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.history.get")
public class History extends AbstractWebScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    PermissionService permissionService;

    @Autowired
    HistoryManager historyManager;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String type = request.getServiceMatch().getTemplateVars().get("type");
        String nodeRefString = request.getParameter("nodeRef");

        if (type == null || type.isEmpty()) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not find required 'type' parameter!");
        }

        if (nodeRefString == null || nodeRefString.isEmpty()) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not find required 'nodeRef' parameter!");
        }

        NodeRef nodeRef = new NodeRef(nodeRefString);

        if (permissionService.hasPermission(nodeRef, PermissionService.READ) != AccessStatus.ALLOWED) {
            throw new AccessDeniedException("Access denied. You do not have the appropriate permissions to perform this operation");
        }

        switch (type.toLowerCase()) {
            case "info":
                Map<String, Object> historyInfo = historyManager.getHistoryInfo(nodeRef);

                response.setContentType("application/json; charset=utf-8");
                response.setContentEncoding("UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(historyInfo));
                break;
            case "data":
                String versionLabel = request.getParameter("version");

                if (versionLabel == null || versionLabel.isEmpty()) {
                    throw new WebScriptException(404, "Not found parameter version!");
                }

                HistoryData historyData = historyManager.getHistoryData(nodeRef, versionLabel);

                response.setContentType("application/json; charset=utf-8");
                response.setContentEncoding("UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(historyData));
                break;
            default:
                throw new WebScriptException(404, "Unknown parameter 'type': '" + type + "'!");
        }
    }
}
