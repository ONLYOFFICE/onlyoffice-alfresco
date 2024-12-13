/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.util;

import org.alfresco.repo.policy.BehaviourFilter;
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

import static com.parashift.onlyoffice.model.OnlyofficeDocsModel.ASPECT_EDITING_IN_ONLYOFFICE_DOCS;
import static com.parashift.onlyoffice.model.OnlyofficeDocsModel.PROP_DOCUMENT_KEY;


@Service
public class LockManager {
    private static final int EDITING_HASH_ASPECT_LENGTH = 32;

    @Autowired
    private NodeService nodeService;
    @Autowired
    private LockService lockService;
    @Autowired
    @Qualifier("policyBehaviourFilter")
    private BehaviourFilter behaviourFilter;

    public void lock(final NodeRef nodeRef) {
        Map<QName, Serializable> aspectProperties = new HashMap<>();
        aspectProperties.put(PROP_DOCUMENT_KEY, generateHash());

        lock(nodeRef, aspectProperties);
    }

    public void lock(final NodeRef nodeRef, final Map<QName, Serializable> aspectProperties) {
        try {
            nodeService.addAspect(nodeRef, ASPECT_EDITING_IN_ONLYOFFICE_DOCS, aspectProperties);

            lockService.lock(nodeRef, LockType.READ_ONLY_LOCK);
        } finally {
            behaviourFilter.enableBehaviour(nodeRef);
        }
    }

    public void unlock(final NodeRef nodeRef) {
        behaviourFilter.disableBehaviour();
        try {
            lockService.unlock(nodeRef);
            nodeService.removeAspect(nodeRef, ASPECT_EDITING_IN_ONLYOFFICE_DOCS);
        } finally {
            behaviourFilter.enableBehaviour();
        }
    }

    public boolean isLocked(final NodeRef nodeRef) {
        return lockService.isLocked(nodeRef) && nodeService.hasAspect(nodeRef, ASPECT_EDITING_IN_ONLYOFFICE_DOCS);
    }

    private String generateHash() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[EDITING_HASH_ASPECT_LENGTH];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }
}
