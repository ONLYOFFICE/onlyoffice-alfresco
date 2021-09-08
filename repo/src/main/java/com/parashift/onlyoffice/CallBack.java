package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
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
import java.io.Serializable;
import java.net.URL;
import java.util.Base64;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    VersionService versionService;

    @Autowired
    Util util;

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
            forcesave = configManager.getAsBoolean("forcesave", "false");
        }

        @Override
        public Object execute() throws Throwable {
            NodeRef wc = cociService.getWorkingCopy(nodeRef);
            String lockOwner = (String)nodeService.getProperty(wc, ContentModel.PROP_WORKING_COPY_OWNER);
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

                    AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
                    cociService.checkin(wc, null, null);
                    saveHistoryToChildNode(nodeRef, callBackJSon,false);
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

                    AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
                    cociService.checkin(wc, null, null, true);
                    saveHistoryToChildNode(nodeRef, callBackJSon, true);

                    AuthenticationUtil.clearCurrentSecurityContext();
                    TenantContextHolder.setTenantDomain(AuthenticationUtil.getUserTenant(lockOwner).getSecond());
                    AuthenticationUtil.setRunAsUser(lockOwner);

                    nodeService.setProperty(wc, Util.EditingHashAspect, hash);
                    nodeService.setProperty(wc, Util.EditingKeyAspect, key);
                    logger.debug("Forcesave complete");
                    break;
            }
            return null;
        }
    }

    private void saveHistoryToChildNode(NodeRef nodeRef, JSONObject changes, Boolean forceSave) {
        Map<QName, Serializable> props = new HashMap<>();
        NodeRef jsonNode = null;
        NodeRef zipNode = null;
        for(ChildAssociationRef assoc : nodeService.getChildAssocs(nodeRef)) {
            if (nodeService.getProperty(assoc.getChildRef(), ContentModel.PROP_NAME).equals("changes.json")) {
                jsonNode = assoc.getChildRef();
            } else if (nodeService.getProperty(assoc.getChildRef(), ContentModel.PROP_NAME).equals("diff.zip")) {
                zipNode = assoc.getChildRef();
            }
        }
        if (jsonNode == null && zipNode == null) {
            props.put(ContentModel.PROP_NAME, "diff.zip");
            NodeRef historyNodeRefZip = this.nodeService.createNode(nodeRef, RenditionModel.ASSOC_RENDITION,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "diff.zip"),
                    ContentModel.TYPE_CONTENT, props).getChildRef();

            props.clear();
            props.put(ContentModel.PROP_NAME, "changes.json");
            NodeRef historyNodeRefJson = this.nodeService.createNode(nodeRef, RenditionModel.ASSOC_RENDITION,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "changes.json"),
                    ContentModel.TYPE_CONTENT, props).getChildRef();
            writeContent(historyNodeRefZip, historyNodeRefJson, changes, forceSave, nodeRef);

            util.ensureVersioningEnabled(historyNodeRefZip);
            util.ensureVersioningEnabled(historyNodeRefJson);
        } else {
            writeContent(zipNode, jsonNode, changes, forceSave, nodeRef);
        }
    }
    private void writeContent(NodeRef zipNode, NodeRef jsonNode, JSONObject changes, Boolean forceSave, NodeRef nodeRef) {
        try {
            if (!forceSave) {
                Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(1);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
                if (!versionService.getCurrentVersion(nodeRef).getVersionLabel().equals("1.0")) {
                    Version nodeRefVersion = versionService.createVersion(nodeRef, versionProperties);
                    versionProperties.put(ContentModel.PROP_INITIAL_VERSION.getLocalName(), false);
                    Version zipNodeVersion = versionService.createVersion(zipNode, versionProperties);
                    Version jsonNodeVersion = versionService.createVersion(jsonNode, versionProperties);
                    zipNodeVersion.getVersionProperties().put(VersionModel.PROP_CREATED_DATE, nodeRefVersion.getVersionProperty(VersionModel.PROP_CREATED_DATE));
                    jsonNodeVersion.getVersionProperties().put(VersionModel.PROP_CREATED_DATE, nodeRefVersion.getVersionProperty(VersionModel.PROP_CREATED_DATE));
                } else {
                    versionService.createVersion(nodeRef, versionProperties);
                }
            }
            ContentWriter writer = this.contentService.getWriter(zipNode, ContentModel.PROP_CONTENT, true);
            writer.setMimetype("application/zip");
            URL url = new URL(changes.getString("changesurl"));
            InputStream in = url.openStream();
            writer.putContent(in);
            writer = this.contentService.getWriter(jsonNode, ContentModel.PROP_CONTENT, true);
            writer.setMimetype("application/json");
            writer.putContent(changes.getJSONObject("history").toString());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private void updateNode(NodeRef nodeRef, String url) throws Exception {
        logger.debug("Retrieving URL:" + url);
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

