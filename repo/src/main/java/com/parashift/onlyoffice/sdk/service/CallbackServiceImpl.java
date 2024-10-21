package com.parashift.onlyoffice.sdk.service;

import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.request.RequestManager;
import com.onlyoffice.manager.security.JwtManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.model.convertservice.ConvertRequest;
import com.onlyoffice.model.convertservice.ConvertResponse;
import com.onlyoffice.model.documenteditor.Callback;
import com.onlyoffice.model.documenteditor.callback.History;
import com.onlyoffice.service.convert.ConvertService;
import com.onlyoffice.service.documenteditor.callback.DefaultCallbackService;
import com.parashift.onlyoffice.util.HistoryManager;
import com.parashift.onlyoffice.util.Util;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

public class CallbackServiceImpl extends DefaultCallbackService {
    @Autowired
    @Qualifier("checkOutCheckInService")
    CheckOutCheckInService cociService;
    @Autowired
    ContentService contentService;
    @Autowired
    NodeService nodeService;
    @Autowired
    HistoryManager historyManager;
    @Autowired
    Util util;
    @Autowired
    RequestManager requestManager;
    @Autowired
    ConvertService convertService;
    @Autowired
    DocumentManager documentManager;
    @Autowired
    VersionService versionService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public CallbackServiceImpl(final JwtManager jwtManager, final SettingsManager settingsManager) {
        super(jwtManager, settingsManager);
    }

    public void handlerEditing(final Callback callback, final String fileId) throws Exception {
        logger.debug("User has entered/exited ONLYOFFICE");
    }

    @Override
    public void handlerSave(final Callback callback, final String fileId) throws Exception {
        NodeRef nodeRef = new NodeRef(fileId);
        NodeRef wc = cociService.getWorkingCopy(nodeRef);
        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        Version oldVersion = versionService.getCurrentVersion(nodeRef);

        logger.debug("Document Updated, changing content");
        updateNode(wc, callback.getUrl(), callback.getFiletype());

        logger.info("removing prop");
        nodeService.removeProperty(wc, Util.EditingHashAspect);
        nodeService.removeProperty(wc, Util.EditingKeyAspect);

        if (getSettingsManager().getSettingBoolean("minorVersion", false)) {
            versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
        } else {
            versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
        }

        cociService.checkin(wc, versionProperties, null);

        History history = callback.getHistory();
        if (history != null) {
            try {
                historyManager.saveHistory(
                        nodeRef,
                        history,
                        callback.getChangesurl()
                );
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        // Delete history(changes.json and diff.zip) for previous forcesave version if exists.
        if (oldVersion.getVersionProperty(Util.ForcesaveAspect.getLocalName()) != null
                && (Boolean) oldVersion.getVersionProperty(Util.ForcesaveAspect.getLocalName())) {
            try {
                historyManager.deleteHistory(nodeRef, oldVersion);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        util.postActivity(nodeRef, false);

        logger.debug("Save complete");
    }

    @Override
    public void handlerSaveCorrupted(final Callback callback, final String fileId) throws Exception {
        logger.error("ONLYOFFICE has reported that saving the document has failed");
        NodeRef nodeRef = new NodeRef(fileId);
        NodeRef wc = cociService.getWorkingCopy(nodeRef);
        AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
        cociService.cancelCheckout(wc);
    }

    @Override
    public void handlerClosed(final Callback callback, final String fileId) throws Exception {
        logger.debug("No document updates, unlocking node");
        NodeRef nodeRef = new NodeRef(fileId);
        NodeRef wc = cociService.getWorkingCopy(nodeRef);
        AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
        cociService.cancelCheckout(wc);
    }

    @Override
    public void handlerForcesave(final Callback callback, final String fileId) throws Exception {
        if (!super.getSettingsManager().getSettingBoolean("customization.forcesave", false)) {
            logger.debug("Forcesave is disabled, ignoring forcesave request");
            return;
        }

        NodeRef nodeRef = new NodeRef(fileId);
        NodeRef wc = cociService.getWorkingCopy(nodeRef);
        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        Version oldVersion = versionService.getCurrentVersion(nodeRef);

        logger.debug("Forcesave request (type: " + callback.getForcesavetype() + ")");
        updateNode(wc, callback.getUrl(), callback.getFiletype());

        String hash = (String) nodeService.getProperty(wc, Util.EditingHashAspect);
        String key = (String) nodeService.getProperty(wc, Util.EditingKeyAspect);

        nodeService.removeProperty(wc, Util.EditingHashAspect);
        nodeService.removeProperty(wc, Util.EditingKeyAspect);

        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
        versionProperties.put(VersionModel.PROP_DESCRIPTION, "ONLYOFFICE (forcesave)");
        versionProperties.put(Util.ForcesaveAspect.getLocalName(), true);
        cociService.checkin(wc, versionProperties, null, true);

        nodeService.setProperty(wc, Util.EditingHashAspect, hash);
        nodeService.setProperty(wc, Util.EditingKeyAspect, key);

        History history = callback.getHistory();
        if (history != null) {
            try {
                historyManager.saveHistory(
                        nodeRef,
                        history,
                        callback.getChangesurl()
                );
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        // Delete history(changes.json and diff.zip) for previous forcesave version if exists.
        if (oldVersion.getVersionProperty(Util.ForcesaveAspect.getLocalName()) != null
                    && (Boolean) oldVersion.getVersionProperty(Util.ForcesaveAspect.getLocalName())) {
            try {
                historyManager.deleteHistory(nodeRef, oldVersion);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        util.postActivity(nodeRef, false);

        logger.debug("Forcesave complete");
    }

    private void updateNode(final NodeRef nodeRef, final String url, final String fileType) throws Exception {
        logger.debug("Retrieving URL:" + url);

        String fileUrl = url;
        String documentName = documentManager.getDocumentName(nodeRef.toString());
        String currentFileType = documentManager.getExtension(documentName);
        final String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
            public Void doWork() {
                NodeRef sourcesNodeRef = cociService.getCheckedOut(nodeRef);
                nodeService.setProperty(sourcesNodeRef, ContentModel.PROP_LOCK_OWNER, currentUser);
                nodeService.setProperty(nodeRef, ContentModel.PROP_WORKING_COPY_OWNER, currentUser);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());

        if (!currentFileType.equals(fileType)) {
            try {
                logger.debug("Should convert back");
                ConvertRequest convert = ConvertRequest.builder()
                        .outputtype(currentFileType)
                        .url(url)
                        .build();

                ConvertResponse convertResponse = convertService.processConvert(convert, nodeRef.toString());

                if (convertResponse.getError() != null && convertResponse.getError().equals(ConvertResponse.Error.TOKEN)) {
                    throw new SecurityException();
                }

                if (convertResponse.getEndConvert() == null || !convertResponse.getEndConvert()
                        || convertResponse.getFileUrl() == null || convertResponse.getFileUrl().isEmpty()) {
                    throw new Exception("'endConvert' is false or 'fileUrl' is empty");
                }

                fileUrl = convertResponse.getFileUrl();
            } catch (Exception e) {
                throw new Exception("Error while converting document back to original format: " + e.getMessage(), e);
            }
        }

        requestManager.executeGetRequest(fileUrl, new RequestManager.Callback<Void>() {
            public Void doWork(final Object response) throws IOException {
                contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true).putContent(((HttpEntity)response).getContent());
                return null;
            }
        });
    }
}
