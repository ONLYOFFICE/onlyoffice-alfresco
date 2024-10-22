/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.scripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.model.settings.validation.ValidationResult;
import com.parashift.onlyoffice.sdk.service.SettingsValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component(value = "webscript.onlyoffice.onlyoffice-config-validation.get")
public class ConfigValidation extends AbstractWebScript {
    @Autowired
    private SettingsValidationService settingsValidationService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
        Map<String, ValidationResult> validationResults = settingsValidationService.validateSettings();

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("validationResults", validationResults);

        response.setContentType("application/json; charset=utf-8");
        response.setContentEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseMap));
    }
}

