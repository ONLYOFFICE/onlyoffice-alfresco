/*
    Copyright (c) Ascensio System SIA 2025. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.sdk.service;

import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.security.JwtManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.model.convertservice.ConvertRequest;
import com.onlyoffice.model.convertservice.ConvertResponse;
import com.onlyoffice.model.documenteditor.Callback;
import com.onlyoffice.model.documenteditor.callback.Action;
import com.onlyoffice.model.documenteditor.callback.History;
import com.onlyoffice.service.convert.ConvertService;
import com.onlyoffice.service.documenteditor.callback.DefaultCallbackService;
import com.parashift.onlyoffice.sdk.manager.url.UrlManager;
import com.parashift.onlyoffice.util.EditorLockManager;
import com.parashift.onlyoffice.util.HistoryManager;
import com.parashift.onlyoffice.util.NodeManager;
import com.parashift.onlyoffice.util.Util;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.lock.mem.LockState;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.parashift.onlyoffice.model.OnlyofficeDocsModel.FORCESAVE_ASPECT;
import static com.parashift.onlyoffice.model.OnlyofficeDocsModel.PROP_DOCUMENT_KEY;


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
    private EditorLockManager editorLockManager;
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private LockService lockService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private UrlManager urlManager;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public CallbackServiceImpl(final JwtManager jwtManager, final SettingsManager settingsManager) {
        super(jwtManager, settingsManager);
    }

    @Override
    public void handlerEditing(final Callback callback, final String fileId) throws Exception {
        List<Action> actions = Optional.ofNullable(callback.getActions()).orElse(new ArrayList<>());
        NodeRef nodeRef = new NodeRef(fileId);

        for (Action action : actions) {
            String userId = String.valueOf(action.getUserid());

            AuthenticationUtil.clearCurrentSecurityContext();
            AuthenticationUtil.setFullyAuthenticatedUser(userId);

            switch (action.getType()) {
                case CONNECTED:
                    handlerConnecting(callback, nodeRef);
                    break;
                case DISCONNECTED:
                    handlerDisconnecting(callback, nodeRef);
                    break;
                default:
            }
        }
    }

    private void handlerConnecting(final Callback callback, final NodeRef nodeRef) throws RuntimeException {
        String key = callback.getKey();

        if (editorLockManager.isLockedInEditor(nodeRef) && editorLockManager.isValidDocumentKey(nodeRef, key)) {
            LockState lockState = lockService.getLockState(nodeRef);

            if (lockState.getExpires() != null) {
                editorLockManager.refreshTimeToExpireLock(nodeRef, EditorLockManager.TIMEOUT_INFINITY);
            }
        } else if (editorLockManager.isLockedInEditor(nodeRef) && !editorLockManager.isValidDocumentKey(nodeRef, key)) {
            throw new RuntimeException(
                    MessageFormat.format(
                            "Node with ID ({0}) is locked in ONLYOFFICE Docs Editor, but key ({1}) is not valid",
                            nodeRef.toString(),
                            key
                    )
            );
        } else {
            if (editorLockManager.isLockedNotInEditor(nodeRef)) {
                throw new RuntimeException(
                        MessageFormat.format(
                                "Node with ID ({0}) is locked not in ONLYOFFICE Docs Editor",
                                nodeRef.toString()
                        )
                );
            }

            if (!lockService.isLocked(nodeRef)) {
                Map<QName, Serializable> aspectProperties = new HashMap<>();
                aspectProperties.put(PROP_DOCUMENT_KEY, key);

                editorLockManager.lockInEditor(nodeRef, aspectProperties);
            }
        }
    }

    private void handlerDisconnecting(final Callback callback, final NodeRef nodeRef) {
        String key = callback.getKey();

        if (!editorLockManager.isLockedInEditor(nodeRef)) {
            throw new RuntimeException(
                    MessageFormat.format(
                            "Node with ID ({0}) is not locked in ONLYOFFICE Docs Editor",
                            nodeRef.toString()
                    )
            );
        }

        if (editorLockManager.isLockedInEditor(nodeRef) && !editorLockManager.isValidDocumentKey(nodeRef, key)) {
            throw new RuntimeException(
                    MessageFormat.format(
                            "Node with ID ({0}) is locked in ONLYOFFICE Docs Editor, but key ({1}) is not valid",
                            nodeRef.toString(),
                            key
                    )
            );
        }

        List<String> users = callback.getUsers();

        LockState lockState = lockService.getLockState(nodeRef);
        String lockOwner = lockState.getOwner();

        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

        if (users.contains(currentUser) || !lockOwner.equals(currentUser)) {
            return;
        }

        boolean lockOwnerIsChanged = false;
        for (String user : users) {
            AccessStatus hasWritePermission = AuthenticationUtil.runAs(() -> {
                return permissionService.hasPermission(nodeRef, PermissionService.WRITE);
            }, user);

            if (AccessStatus.ALLOWED.equals(hasWritePermission)) {
                editorLockManager.changeLockOwner(nodeRef, user);
                lockOwnerIsChanged = true;
                break;
            }
        }

        if (!lockOwnerIsChanged) {
            throw new RuntimeException(
                    MessageFormat.format(
                            "Can not change lock owner for Node with ID ({0}), "
                                    + "no user has access to the write",
                            nodeRef.toString()
                    )
            );
        }
    }

    @Override
    public void handlerSave(final Callback callback, final String fileId) throws Exception {
        String key = callback.getKey();
        List<Action> actions = callback.getActions();
        NodeRef nodeRef = new NodeRef(fileId);

        for (Action action : actions) {
            String userId = String.valueOf(action.getUserid());

            AuthenticationUtil.clearCurrentSecurityContext();
            AuthenticationUtil.setFullyAuthenticatedUser(userId);

            if (!editorLockManager.isLockedInEditor(nodeRef)) {
                throw new RuntimeException(
                        MessageFormat.format(
                                "Node with ID ({0}) is not locked in ONLYOFFICE Docs Editor",
                                nodeRef.toString()
                        )
                );
            }

            if (editorLockManager.isLockedInEditor(nodeRef) && !editorLockManager.isValidDocumentKey(nodeRef, key)) {
                throw new RuntimeException(
                        MessageFormat.format(
                                "Node with ID ({0}) is locked in ONLYOFFICE Docs Editor, but key ({1}) is not valid",
                                nodeRef.toString(),
                                key
                        )
                );
            }

            String documentName = documentManager.getDocumentName(nodeRef.toString());
            String currentFileType = documentManager.getExtension(documentName);
            Version oldVersion = versionService.getCurrentVersion(nodeRef);
            String fileUrl = callback.getUrl();
            fileUrl = urlManager.replaceToInnerDocumentServerUrl(fileUrl);
            String changesUrl = callback.getChangesurl();
            changesUrl = urlManager.replaceToInnerDocumentServerUrl(changesUrl);

            if (!currentFileType.equals(callback.getFiletype())) {
                fileUrl = convert(fileUrl, currentFileType);
            }

            editorLockManager.unlockFromEditor(nodeRef);
            nodeManager.createNewVersion(nodeRef, fileUrl);

            History history = callback.getHistory();
            if (history != null) {
                try {
                    historyManager.saveHistory(
                            nodeRef,
                            history,
                            changesUrl
                    );
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            if (oldVersion != null && oldVersion.getVersionProperty(FORCESAVE_ASPECT.getLocalName()) != null
                    && (Boolean) oldVersion.getVersionProperty(FORCESAVE_ASPECT.getLocalName())) {
                try {
                    historyManager.deleteHistory(nodeRef, oldVersion);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            util.postActivity(nodeRef, false);
        }
    }

    @Override
    public void handlerClosed(final Callback callback, final String fileId) throws Exception {
        String key = callback.getKey();
        List<Action> actions = callback.getActions();
        NodeRef nodeRef = new NodeRef(fileId);

        for (Action action : actions) {
            String userId = String.valueOf(action.getUserid());

            AuthenticationUtil.clearCurrentSecurityContext();
            AuthenticationUtil.setFullyAuthenticatedUser(userId);

            if (!editorLockManager.isLockedInEditor(nodeRef)) {
                throw new RuntimeException(
                        MessageFormat.format(
                                "Node with ID ({0}) is not locked in ONLYOFFICE Docs Editor",
                                nodeRef.toString()
                        )
                );
            }

            if (editorLockManager.isLockedInEditor(nodeRef) && !editorLockManager.isValidDocumentKey(nodeRef, key)) {
                throw new RuntimeException(
                        MessageFormat.format(
                                "Node with ID ({0}) is locked in ONLYOFFICE Docs Editor, but key ({1}) is not valid",
                                nodeRef.toString(),
                                key
                        )
                );
            }

            editorLockManager.unlockFromEditor(nodeRef);
        }
    }

    @Override
    public void handlerForcesave(final Callback callback, final String fileId) throws Exception {
        if (!super.getSettingsManager().getSettingBoolean("customization.forcesave", false)) {
            logger.debug("Forcesave is disabled, ignoring forcesave request");
            return;
        }

        String key = callback.getKey();
        List<Action> actions = callback.getActions();
        NodeRef nodeRef = new NodeRef(fileId);

        for (Action action : actions) {
            String userId = String.valueOf(action.getUserid());

            AuthenticationUtil.clearCurrentSecurityContext();
            AuthenticationUtil.setFullyAuthenticatedUser(userId);

            if (!editorLockManager.isLockedInEditor(nodeRef)) {
                throw new RuntimeException(
                        MessageFormat.format(
                                "Node with ID ({0}) is not locked in ONLYOFFICE Docs Editor",
                                nodeRef.toString()
                        )
                );
            }

            if (editorLockManager.isLockedInEditor(nodeRef) && !editorLockManager.isValidDocumentKey(nodeRef, key)) {
                throw new RuntimeException(
                        MessageFormat.format(
                                "Node with ID ({0}) is locked in ONLYOFFICE Docs Editor, but key ({1}) is not valid",
                                nodeRef.toString(),
                                key
                        )
                );
            }

            String documentName = documentManager.getDocumentName(nodeRef.toString());
            String currentFileType = documentManager.getExtension(documentName);
            Version oldVersion = versionService.getCurrentVersion(nodeRef);
            String fileUrl = callback.getUrl();
            fileUrl = urlManager.replaceToInnerDocumentServerUrl(fileUrl);
            String changesUrl = callback.getChangesurl();
            changesUrl = urlManager.replaceToInnerDocumentServerUrl(changesUrl);

            if (!currentFileType.equals(callback.getFiletype())) {
                fileUrl = convert(fileUrl, currentFileType);
            }

            Map<QName, Serializable> aspectEditingProperties = editorLockManager.getEditorLockProperties(nodeRef);

            Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
            versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
            versionProperties.put(VersionModel.PROP_DESCRIPTION, "ONLYOFFICE (forcesave)");
            versionProperties.put(FORCESAVE_ASPECT.getLocalName(), true);

            editorLockManager.unlockFromEditor(nodeRef);
            nodeManager.createNewVersion(nodeRef, fileUrl, versionProperties);
            editorLockManager.lockInEditor(nodeRef, aspectEditingProperties);

            History history = callback.getHistory();
            if (history != null) {
                try {
                    historyManager.saveHistory(
                            nodeRef,
                            history,
                            changesUrl
                    );
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            // Delete history(changes.json and diff.zip) for previous forcesave version if exists.
            if (oldVersion != null && oldVersion.getVersionProperty(FORCESAVE_ASPECT.getLocalName()) != null
                    && (Boolean) oldVersion.getVersionProperty(FORCESAVE_ASPECT.getLocalName())) {
                try {
                    historyManager.deleteHistory(nodeRef, oldVersion);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            util.postActivity(nodeRef, false);
        }
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
