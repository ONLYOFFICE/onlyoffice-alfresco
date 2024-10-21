package com.parashift.onlyoffice.scripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.model.settings.Settings;
import com.onlyoffice.model.settings.security.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.onlyoffice-config.post")
public class ConfigCallback extends AbstractWebScript {
    @Autowired
    private SettingsManager settingsManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(final WebScriptRequest request, final WebScriptResponse response) throws IOException {

        logger.debug("Received new configuration");

        Settings settings = objectMapper.readValue(request.getContent().getContent(), Settings.class);
        Map<String, Object> extraSettings = settings.getExtra();

        settingsManager.setSetting("convertOriginal", (String) extraSettings.get("convertOriginal"));
        settingsManager.setSetting("webpreview", (String) extraSettings.get("webpreview"));
        settingsManager.setSetting("minorVersion", (String) extraSettings.get("minorVersion"));

        if (settings.getDemo() != null && settings.getDemo()) {
            settingsManager.enableDemo();
        } else {
            settingsManager.disableDemo();
        }

        if (settingsManager.isDemoActive()) {
            Security security = settings.getSecurity();
            security.setKey(null);
            security.setHeader(null);

            settings.setUrl(null);
            settings.setInnerUrl(null);
            settings.setSecurity(security);
        }

        try {
            settingsManager.setSettings(settings);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        response.setContentType("application/json; charset=utf-8");
        response.setContentEncoding("UTF-8");
        response.setStatus(200);
    }
}

