package com.parashift.onlyoffice.sdk.manager.settings;

import com.onlyoffice.manager.settings.DefaultSettingsManager;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;

/*
   Copyright (c) Ascensio System SIA 2024. All rights reserved.
   http://www.onlyoffice.com
*/

public class SettingsManagerImpl extends DefaultSettingsManager {
    private static final String SETTINGS_PREFIX = "onlyoffice.";

    @Autowired
    AttributeService attributeService;

    @Override
    public String getSetting(String name) {
        return (String) attributeService.getAttribute(SETTINGS_PREFIX + name);
    }

    @Override
    public void setSetting(String name, String value) {
        attributeService.setAttribute(value, SETTINGS_PREFIX + name);
    }
}
