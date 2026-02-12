/*
    Copyright (c) Ascensio System SIA 2026. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.sdk.service;

import com.onlyoffice.model.settings.validation.ValidationResult;

import java.util.Map;


public interface SettingsValidationService extends com.onlyoffice.service.settings.SettingsValidationService {
    Map<String, ValidationResult> validateSettings();
}
