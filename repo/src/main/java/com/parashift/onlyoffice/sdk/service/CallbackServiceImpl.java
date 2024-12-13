/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.sdk.service;

import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.security.JwtManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.model.convertservice.ConvertRequest;
import com.onlyoffice.model.convertservice.ConvertResponse;
import com.onlyoffice.model.documenteditor.Callback;
import com.onlyoffice.model.documenteditor.callback.History;
import com.onlyoffice.service.convert.ConvertService;
import com.onlyoffice.service.documenteditor.callback.DefaultCallbackService;
import com.parashift.onlyoffice.util.HistoryManager;
import com.parashift.onlyoffice.util.LockManager;
import com.parashift.onlyoffice.util.NodeManager;
import com.parashift.onlyoffice.util.Util;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.parashift.onlyoffice.model.OnlyofficeDocsModel.ASPECT_EDITING_IN_ONLYOFFICE_DOCS;
import static com.parashift.onlyoffice.model.OnlyofficeDocsModel.FORCESAVE_ASPECT;


public class CallbackServiceImpl extends DefaultCallbackService {
    @Autowired
    private HistoryManager historyManager;
    @Autowired
    private Util util;
    @Autowired
    private ConvertService convertService;
    @Autowired
    private DocumentManager documentManager;
    @Autowired
    private VersionService versionService;
    @Autowired
    private LockManager lockManager;
    @Autowired
    private NodeManager nodeManager;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public CallbackServiceImpl(final JwtManager jwtManager, final SettingsManager settingsManager) {
        super(jwtManager, settingsManager);
    }

    public void handlerEditing(final Callback callback, final String fileId) throws Exception {
        logger.debug("User has entered/exited ONLYOFFICE");
    }

    @Override
    public void handlerSave(final Callback callback, final String fileId) throws Exception {
        logger.debug("Document Updated, changing content");

        NodeRef nodeRef = new NodeRef(fileId);
        String documentName = documentManager.getDocumentName(nodeRef.toString());
        String currentFileType = documentManager.getExtension(documentName);
        String fileUrl = callback.getUrl();

        Version oldVersion = versionService.getCurrentVersion(nodeRef);

        lockManager.unlock(nodeRef);

        if (!currentFileType.equals(callback.getFiletype())) {
            fileUrl = convert(fileUrl, currentFileType);
        }

        nodeManager.createNewVersion(nodeRef, fileUrl);

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

        if (oldVersion.getVersionProperty(FORCESAVE_ASPECT.getLocalName()) != null
                && (Boolean) oldVersion.getVersionProperty(FORCESAVE_ASPECT.getLocalName())) {
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
        lockManager.unlock(nodeRef);
    }

    @Override
    public void handlerClosed(final Callback callback, final String fileId) throws Exception {
        logger.debug("No document updates, unlocking node");
        NodeRef nodeRef = new NodeRef(fileId);
        lockManager.unlock(nodeRef);
    }

    @Override
    public void handlerForcesave(final Callback callback, final String fileId) throws Exception {
        if (!super.getSettingsManager().getSettingBoolean("customization.forcesave", false)) {
            logger.debug("Forcesave is disabled, ignoring forcesave request");
            return;
        }
        NodeRef nodeRef = new NodeRef(fileId);
        String documentName = documentManager.getDocumentName(nodeRef.toString());
        String currentFileType = documentManager.getExtension(documentName);
        String fileUrl = callback.getUrl();

        Map<QName, Serializable> aspectEditingProperties = nodeManager.getPropertiesByAspect(
                nodeRef,
                ASPECT_EDITING_IN_ONLYOFFICE_DOCS
        );

        Version oldVersion = versionService.getCurrentVersion(nodeRef);

        lockManager.unlock(nodeRef);

        if (!currentFileType.equals(callback.getFiletype())) {
            fileUrl = convert(fileUrl, currentFileType);
        }

        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
        versionProperties.put(VersionModel.PROP_DESCRIPTION, "ONLYOFFICE (forcesave)");
        versionProperties.put(FORCESAVE_ASPECT.getLocalName(), true);

        nodeManager.createNewVersion(nodeRef, fileUrl, versionProperties);

        lockManager.lock(nodeRef, aspectEditingProperties);

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
        if (oldVersion.getVersionProperty(FORCESAVE_ASPECT.getLocalName()) != null
                && (Boolean) oldVersion.getVersionProperty(FORCESAVE_ASPECT.getLocalName())) {
            try {
                historyManager.deleteHistory(nodeRef, oldVersion);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        util.postActivity(nodeRef, false);

        logger.debug("Forcesave complete");
    }

    private String convert(final String fileUrl, final String outputType) {
        try {
            ConvertRequest convert = ConvertRequest.builder()
                    .outputtype(outputType)
                    .url(fileUrl)
                    .build();

            ConvertResponse convertResponse = convertService.processConvert(convert, null);

            return convertResponse.getFileUrl();
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(
                    "Error while converting document back to original format: " + e.getMessage(),
                    e
            );
        }
    }
}
