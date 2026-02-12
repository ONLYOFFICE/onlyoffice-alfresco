/*
    Copyright (c) Ascensio System SIA 2026. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.scripts;

import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.model.settings.SettingsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.stereotype.Component;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


@Component(value = "webscript.onlyoffice.onlyoffice-config.get")
public class Config extends DeclarativeWebScript {
    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private DocumentManager documentManager;

    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put("demoAvailable", settingsManager.isDemoAvailable());
        model.put("webpreview", settingsManager.getSettingBoolean("webpreview", false));
        model.put("convertOriginal", settingsManager.getSettingBoolean("convertOriginal", false));
        model.put("minorVersion", settingsManager.getSettingBoolean("minorVersion", false));

        Map<String, Boolean> lossyEditable = documentManager.getLossyEditableMap();

        if (settingsManager.getSetting(SettingsConstants.LOSSY_EDIT) == null
                || settingsManager.getSetting(SettingsConstants.LOSSY_EDIT).isEmpty()) {
            lossyEditable.put("txt", true);
            lossyEditable.put("csv", true);
        }

        model.put("lossyEditable", lossyEditable);

        try {
            Map<String, String> settings = settingsManager.getSettings();

            if (settings.get("customization.review.reviewDisplay") == null
                    || settings.get("customization.review.reviewDisplay").isEmpty()) {
                settings.put("customization.review.reviewDisplay", "ORIGINAL");
            }

            if (settings.get("customization.help") == null || settings.get("customization.help").isEmpty()) {
                settings.put("customization.help", "true");
            }

            if (settings.get("customization.chat") == null || settings.get("customization.chat").isEmpty()) {
                settings.put("customization.chat", "true");
            }

            model.put("settings", settings);
            model.put("pathApiUrl", settingsManager.getDocsIntegrationSdkProperties().getDocumentServer().getApiUrl());

        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return model;
    }
}

