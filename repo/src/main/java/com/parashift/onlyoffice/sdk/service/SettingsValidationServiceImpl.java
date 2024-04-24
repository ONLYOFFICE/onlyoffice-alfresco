package com.parashift.onlyoffice.sdk.service;

import com.onlyoffice.manager.request.RequestManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.manager.url.UrlManager;
import com.onlyoffice.model.common.CommonResponse;
import com.onlyoffice.model.settings.validation.ValidationResult;
import com.onlyoffice.model.settings.validation.status.Status;
import com.onlyoffice.service.settings.DefaultSettingsValidationService;
import org.alfresco.repo.i18n.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

public class SettingsValidationServiceImpl extends DefaultSettingsValidationService implements SettingsValidationService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MessageService messageService;

    public SettingsValidationServiceImpl(RequestManager requestManager, UrlManager urlManager,
                                         SettingsManager settingsManager) {
        super(requestManager, urlManager, settingsManager);
    }

    @Override
    public Map<String, ValidationResult> validateSettings() {
        Map<String, ValidationResult> result = new HashMap<>();

        try {
            result.put(
                    "documentServer",
                    checkDocumentServer()
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            result.put(
                    "documentServer",
                    ValidationResult.builder()
                            .status(Status.FAILED)
                            .error(CommonResponse.Error.CONNECTION)
                            .build()
            );
        }

        try {
            result.put(
                    "commandService",
                    checkCommandService()
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            result.put(
                    "commandService",
                    ValidationResult.builder()
                            .status(Status.FAILED)
                            .error(CommonResponse.Error.CONNECTION)
                            .build()
            );
        }

        try {
            result.put(
                    "convertService",
                    checkConvertService()
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            result.put(
                    "convertService",
                    ValidationResult.builder()
                            .status(Status.FAILED)
                            .error(CommonResponse.Error.CONNECTION)
                            .build()
            );
        }

        messageService.registerResourceBundle("alfresco/messages/onlyoffice");

        if (result.get("documentServer").getStatus().equals(Status.FAILED)) {
            result.get("documentServer")
                    .setMessage(
                            messageService.getMessage(
                                    "onlyoffice.server.common.error." + result.get("documentServer")
                                            .getError()
                                            .toString()
                                            .toLowerCase()
                            )
                    );
        }

        if (result.get("commandService").getStatus().equals(Status.FAILED)) {
            result.get("commandService")
                    .setMessage(
                            messageService.getMessage(
                                    "onlyoffice.service.command.error." + result.get("commandService")
                                            .getError()
                                            .toString()
                                            .toLowerCase()
                            )
                    );
        }

        if (result.get("convertService").getStatus().equals(Status.FAILED)) {
            result.get("convertService")
                    .setMessage(
                            messageService.getMessage(
                                    "onlyoffice.service.convert.error." + result.get("convertService")
                                            .getError()
                                            .toString()
                                            .toLowerCase()
                            )
                    );
        }

        return result;
    }
}
