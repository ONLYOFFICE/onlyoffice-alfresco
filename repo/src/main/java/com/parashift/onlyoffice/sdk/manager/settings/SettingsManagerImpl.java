/*
    Copyright (c) Ascensio System SIA 2025. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.sdk.manager.settings;

import com.onlyoffice.manager.settings.DefaultSettingsManager;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;


public class SettingsManagerImpl extends DefaultSettingsManager {
    private static final String SETTINGS_PREFIX = "onlyoffice.";
    @Autowired
    private AttributeService attributeService;

    @Autowired
    @Qualifier("global-properties")
    private Properties globalProp;

    @Override
    public String getSetting(final String name) {
        Object value = attributeService.getAttribute(SETTINGS_PREFIX + name);

        if (value == null) {
            value = globalProp.get(SETTINGS_PREFIX + name);
        }

        return (String) value;
    }

    @Override
    public void setSetting(final String name, final String value) {
        attributeService.setAttribute(value, SETTINGS_PREFIX + name);
    }
}
