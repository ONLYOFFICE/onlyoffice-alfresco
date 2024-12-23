/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.util;

import net.sf.acegisecurity.Authentication;
import org.alfresco.repo.lock.mem.LockState;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.parashift.onlyoffice.model.OnlyofficeDocsModel.ASPECT_EDITING_IN_ONLYOFFICE_DOCS;
import static com.parashift.onlyoffice.model.OnlyofficeDocsModel.PROP_DOCUMENT_KEY;


@Service
public class EditorLockManager {
    public static final int TIMEOUT_INFINITY = 0;
    public static final int TIMEOUT_CONNECTING_EDITOR = 60;

    private static final int EDITING_HASH_ASPECT_LENGTH = 32;

    @Autowired
    private NodeService nodeService;
    @Autowired
    private LockService lockService;
    @Autowired
    @Qualifier("policyBehaviourFilter")
    private BehaviourFilter behaviourFilter;

    public void lockInEditor(final NodeRef nodeRef, final int timeToExpire) {
        Map<QName, Serializable> aspectProperties = new HashMap<>();
        aspectProperties.put(PROP_DOCUMENT_KEY, generateHash());

        lockInEditor(nodeRef, aspectProperties, timeToExpire);
    }

    public void lockInEditor(final NodeRef nodeRef, final Map<QName, Serializable> aspectProperties) {
        lockInEditor(nodeRef, aspectProperties, TIMEOUT_INFINITY);
    }

    public void lockInEditor(final NodeRef nodeRef, final Map<QName, Serializable> aspectProperties,
                             final int timeToExpire) {
        behaviourFilter.disableBehaviour();
        try {
            nodeService.addAspect(nodeRef, ASPECT_EDITING_IN_ONLYOFFICE_DOCS, aspectProperties);

            lockService.lock(nodeRef, LockType.READ_ONLY_LOCK, timeToExpire);
        } finally {
            behaviourFilter.enableBehaviour(nodeRef);
        }
    }

    public void changeLockOwner(final NodeRef nodeRef, final String newLockOwner) {
        Authentication currentAuthentication = AuthenticationUtil.getFullAuthentication();

        Map<QName, Serializable> aspectEditingProperties = getEditorLockProperties(nodeRef);

        unlockFromEditor(nodeRef);

        try {
            AuthenticationUtil.clearCurrentSecurityContext();
            AuthenticationUtil.setFullyAuthenticatedUser(newLockOwner);

            lockInEditor(nodeRef, aspectEditingProperties);
        } finally {
            AuthenticationUtil.clearCurrentSecurityContext();
            AuthenticationUtil.setFullAuthentication(currentAuthentication);
        }
    }

    public void refreshTimeToExpireLock(final NodeRef nodeRef, final int timeToExpire) {
        Map<QName, Serializable> aspectEditingProperties = getEditorLockProperties(nodeRef);

        unlockFromEditor(nodeRef);

        lockInEditor(nodeRef, aspectEditingProperties, timeToExpire);
    }

    public void unlockFromEditor(final NodeRef nodeRef) {
        behaviourFilter.disableBehaviour();
        try {
            LockState lockState = lockService.getLockState(nodeRef);
            String lockOwner = lockState.getOwner();

            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
                public Void doWork() {
                    lockService.unlock(nodeRef);
                    return null;
                }
            }, lockOwner);

            nodeService.removeAspect(nodeRef, ASPECT_EDITING_IN_ONLYOFFICE_DOCS);
        } finally {
            behaviourFilter.enableBehaviour();
        }
    }

    public boolean isLockedInEditor(final NodeRef nodeRef) {
        return lockService.isLocked(nodeRef) && nodeService.hasAspect(nodeRef, ASPECT_EDITING_IN_ONLYOFFICE_DOCS);
    }

    public boolean isLockedNotInEditor(final NodeRef nodeRef) {
        return lockService.isLocked(nodeRef) && !nodeService.hasAspect(nodeRef, ASPECT_EDITING_IN_ONLYOFFICE_DOCS);
    }

    public boolean isValidDocumentKey(final NodeRef nodeRef, final String key) {
       String currentKey = (String) nodeService.getProperty(nodeRef, PROP_DOCUMENT_KEY);

       if (currentKey == null || currentKey.isEmpty()) {
           return false;
       }

       return currentKey.equals(key);
    }

    public Map<QName, Serializable> getEditorLockProperties(final NodeRef nodeRef) {
        if (nodeService.hasAspect(nodeRef, ASPECT_EDITING_IN_ONLYOFFICE_DOCS)) {
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

            return properties.entrySet().stream()
                    .filter(entry -> entry.getKey()
                            .getNamespaceURI()
                            .equals(ASPECT_EDITING_IN_ONLYOFFICE_DOCS.getNamespaceURI()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        }

        return null;
    }

    private String generateHash() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[EDITING_HASH_ASPECT_LENGTH];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }
}
