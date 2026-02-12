/*
    Copyright (c) Ascensio System SIA 2026. All rights reserved.
    http://www.onlyoffice.com
*/
/**
 * Created by cetra on 20/10/15.
 */


package com.parashift.onlyoffice.scripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.model.documenteditor.Callback;
import com.onlyoffice.service.documenteditor.callback.CallbackService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component(value = "webscript.onlyoffice.callback.post")
public class CallBack extends AbstractWebScript {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private CallbackService callbackService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(final WebScriptRequest request, final WebScriptResponse response) throws IOException {

        Integer code = 0;
        Exception error = null;

        logger.debug("Received JSON Callback");
        try {
            NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));

            Callback callback = objectMapper.readValue(request.getContent().getContent(), Callback.class);
            String authorizationHeader = request.getHeader(settingsManager.getSecurityHeader());

            callback = callbackService.verifyCallback(callback, authorizationHeader);

            Boolean reqNew = transactionService.isReadOnly();
            transactionService.getRetryingTransactionHelper()
                .doInTransaction(new ProcessRequestCallback(callback, nodeRef), reqNew, reqNew);
        } catch (SecurityException ex) {
            code = Status.STATUS_FORBIDDEN;
            error = ex;
        } catch (Exception ex) {
            code = Status.STATUS_INTERNAL_SERVER_ERROR;
            error = ex;
        }

        if (error != null) {
            response.setStatus(code);
            logger.error(error.getMessage(), error);

            response.getWriter().write("{\"error\":1, \"message\":\"" + error.getMessage() + "\"}");
        } else {
            response.getWriter().write("{\"error\":0}");
        }
    }

    private class ProcessRequestCallback implements RetryingTransactionCallback<Object> {
        private Callback callback;
        private NodeRef nodeRef;

        ProcessRequestCallback(final Callback callback, final NodeRef node) {
            this.callback = callback;
            this.nodeRef = node;
        }

        @Override
        public Object execute() throws Throwable {
            callbackService.processCallback(callback, nodeRef.toString());
            return null;
        }
    }
}

