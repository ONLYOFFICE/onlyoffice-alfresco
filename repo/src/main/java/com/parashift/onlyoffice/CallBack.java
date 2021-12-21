package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by cetra on 20/10/15.
 */
 /*
    Copyright (c) Ascensio System SIA 2021. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.callback.post")
public class CallBack extends AbstractWebScript {

    @Autowired
    @Qualifier("checkOutCheckInService")
    CheckOutCheckInService cociService;

    @Autowired
    @Qualifier("policyBehaviourFilter")
    BehaviourFilter behaviourFilter;

    @Autowired
    ContentService contentService;

    @Autowired
    ConfigManager configManager;

    @Autowired
    JwtManager jwtManager;

    @Autowired
    NodeService nodeService;

    @Autowired
    MimetypeService mimetypeService;

    @Autowired
    Converter converterService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    Util util;

    @Autowired
    OwnableService ownableService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {

        Integer code = 0;
        Exception error = null;

        logger.debug("Received JSON Callback");
        try {
            JSONObject callBackJSon = new JSONObject(request.getContent().getContent());
            logger.debug(callBackJSon.toString(3));

            if (jwtManager.jwtEnabled()) {
                String token = callBackJSon.optString("token");
                Boolean inBody = true;

                if (token == null || token == "") {
                    String jwth = jwtManager.getJwtHeader();
                    String header = request.getHeader(jwth);
                    token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : header;
                    inBody = false;
                }

                if (token == null || token == "") {
                    throw new SecurityException("Expected JWT");
                }

                if (!jwtManager.verify(token)) {
                    throw new SecurityException("JWT verification failed");
                }

                JSONObject bodyFromToken = new JSONObject(new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), "UTF-8"));

                if (inBody) {
                    callBackJSon = bodyFromToken;
                } else {
                    callBackJSon = bodyFromToken.getJSONObject("payload");
                }
            }

            String username = null;

            if (callBackJSon.has("users")) {
                JSONArray array = callBackJSon.getJSONArray("users");
                if (array.length() > 0) {
                    username = (String) array.get(0);
                }
            }

            if (username == null && callBackJSon.has("actions")) {
                JSONArray array = callBackJSon.getJSONArray("actions");
                if (array.length() > 0) {
                    username = ((JSONObject) array.get(0)).getString("userid");
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
                .doInTransaction(new ProccessRequestCallback(callBackJSon, nodeRef), reqNew, reqNew);
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

        private JSONObject callBackJSon;
        private NodeRef nodeRef;

        private Boolean forcesave;

        public ProccessRequestCallback(JSONObject json, NodeRef node) {
            callBackJSon = json;
            nodeRef = node;
            forcesave = configManager.getAsBoolean("forcesave", "fasle");
        }

        @Override
        public Object execute() throws Throwable {
            NodeRef wc = cociService.getWorkingCopy(nodeRef);
            String downloadUrl = null;
            //Status codes from here: https://api.onlyoffice.com/editors/editor
            switch(callBackJSon.getInt("status")) {
                case 0:
                    logger.error("ONLYOFFICE has reported that no doc with the specified key can be found");
                    AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
                    cociService.cancelCheckout(wc);
                    break;
                case 1:
                    logger.debug("User has entered/exited ONLYOFFICE");
                    break;
                case 2:
                    logger.debug("Document Updated, changing content");
                    downloadUrl = util.replaceDocEditorURLToInternal(callBackJSon.getString("url"));
                    updateNode(wc, downloadUrl);

                    logger.info("removing prop");
                    nodeService.removeProperty(wc, Util.EditingHashAspect);
                    nodeService.removeProperty(wc, Util.EditingKeyAspect);

                    cociService.checkin(wc, null, null);
                    break;
                case 3:
                    logger.error("ONLYOFFICE has reported that saving the document has failed");
                    AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
                    cociService.cancelCheckout(wc);
                    break;
                case 4:
                    logger.debug("No document updates, unlocking node");
                    AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
                    cociService.cancelCheckout(wc);
                    break;
                case 6:
                    if (!forcesave) {
                        logger.debug("Forcesave is disabled, ignoring forcesave request");
                        return null;
                    }

                    logger.debug("Forcesave request (type: " + callBackJSon.getInt("forcesavetype") + ")");
                    downloadUrl = util.replaceDocEditorURLToInternal(callBackJSon.getString("url"));
                    updateNode(wc, downloadUrl);

                    String hash = (String) nodeService.getProperty(wc, Util.EditingHashAspect);
                    String key = (String) nodeService.getProperty(wc, Util.EditingKeyAspect);

                    nodeService.removeProperty(wc, Util.EditingHashAspect);
                    nodeService.removeProperty(wc, Util.EditingKeyAspect);

                    cociService.checkin(wc, null, null, true);

                    nodeService.setProperty(wc, Util.EditingHashAspect, hash);
                    nodeService.setProperty(wc, Util.EditingKeyAspect, key);
                    logger.debug("Forcesave complete");
                    break;
            }
            return null;
        }
    }

    private void updateNode(final NodeRef nodeRef, String url) throws Exception {
        logger.debug("Retrieving URL:" + url);

        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
            public Void doWork() {
                NodeRef sourcesNodeRef = cociService.getCheckedOut(nodeRef);
                nodeService.setProperty(sourcesNodeRef, ContentModel.PROP_LOCK_OWNER, currentUser);
                nodeService.setProperty(nodeRef, ContentModel.PROP_WORKING_COPY_OWNER, currentUser);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());

        ContentData contentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        String mimeType = contentData.getMimetype();

        if (converterService.shouldConvertBack(mimeType)) {
            try {
                logger.debug("Should convert back");
                String downloadExt = util.getFileExtension(url).replace(".", "");
                url = converterService.convert(util.getKey(nodeRef), downloadExt, mimetypeService.getExtension(mimeType), url);
            } catch (Exception e) {
                throw new Exception("Error while converting document back to original format: " + e.getMessage(), e);
            }
        }

        try {
            checkCert();
            InputStream in = new URL( url ).openStream();
            contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true).putContent(in);
        } catch (IOException e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
            throw new Exception("Error while downloading new document version: " + e.getMessage(), e);
        }
    }

    private void checkCert() {
        String cert = (String) configManager.getOrDefault("cert", "no");
        if (cert.equals("true")) {
            TrustManager[] trustAllCerts = new TrustManager[]
            {
                new X509TrustManager()
                {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType)
                    {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType)
                    {
                    }
                }
            };

            SSLContext sc;

            try
            {
                sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            }
            catch (NoSuchAlgorithmException | KeyManagementException ex)
            {
            }

            HostnameVerifier allHostsValid = new HostnameVerifier()
            {
                @Override
                public boolean verify(String hostname, SSLSession session)
                {
                return true;
                }
            };

            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        }
    }
}

