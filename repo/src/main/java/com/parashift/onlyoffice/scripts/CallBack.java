package com.parashift.onlyoffice.scripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.model.documenteditor.Callback;
import com.onlyoffice.model.documenteditor.callback.Action;
import com.onlyoffice.service.documenteditor.callback.CallbackService;
import com.parashift.onlyoffice.util.*;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Created by cetra on 20/10/15.
 */
 /*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.callback.post")
public class CallBack extends AbstractWebScript {

    @Autowired
    @Qualifier("checkOutCheckInService")
    CheckOutCheckInService cociService;

    @Autowired
    NodeService nodeService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    SettingsManager settingsManager;

    @Autowired
    CallbackService callbackService;

    ObjectMapper objectMapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(final WebScriptRequest request, final WebScriptResponse response) throws IOException {

        Integer code = 0;
        Exception error = null;

        logger.debug("Received JSON Callback");
        try {
            Callback callback = objectMapper.readValue(request.getContent().getContent(), Callback.class);
            String authorizationHeader = request.getHeader(settingsManager.getSecurityHeader());

            callback = callbackService.verifyCallback(callback, authorizationHeader);

            String username = null;

            if (callback.getUsers() != null) {
                List<String> users = callback.getUsers();
                if (users.size() > 0) {
                    username = users.get(0);
                }
            }

            if (username == null && callback.getActions() != null) {
                List<Action> actions = callback.getActions();
                if (actions.size() > 0) {
                    username = actions.get(0).getUserid();
                }
            }

            if (username != null) {
                AuthenticationUtil.clearCurrentSecurityContext();
                TenantContextHolder.setTenantDomain(AuthenticationUtil.getUserTenant(username).getSecond());
                AuthenticationUtil.setRunAsUser(username);
            } else {
                throw new SecurityException("No user information");
            }

            NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));
            String hash = null;
            if (cociService.isCheckedOut(nodeRef)) {
                hash = (String) nodeService.getProperty(cociService.getWorkingCopy(nodeRef), Util.EditingHashAspect);
            }
            String queryHash = request.getParameter("cb_key");

            if (hash == null || queryHash == null || !hash.equals(queryHash)) {
                throw new SecurityException("Security hash verification failed");
            }

            Boolean reqNew = transactionService.isReadOnly();
            transactionService.getRetryingTransactionHelper()
                .doInTransaction(new ProccessRequestCallback(callback, nodeRef), reqNew, reqNew);
            AuthenticationUtil.clearCurrentSecurityContext();

        } catch (SecurityException ex) {
            code = 403;
            error = ex;
        } catch (Exception ex) {
            code = 500;
            error = ex;
        }

        if (error != null) {
            response.setStatus(code);
            logger.error("Error execution script Callback", error);

            response.getWriter().write("{\"error\":1, \"message\":\"" + error.getMessage() + "\"}");
        } else {
            response.getWriter().write("{\"error\":0}");
        }
    }

    private class ProccessRequestCallback implements RetryingTransactionCallback<Object> {
        private Callback callback;
        private NodeRef nodeRef;

        public ProccessRequestCallback(final Callback callback, final NodeRef node) {
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

