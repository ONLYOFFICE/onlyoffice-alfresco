package com.parashift.onlyoffice.util;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.*;

/*
   Copyright (c) Ascensio System SIA 2024. All rights reserved.
   http://www.onlyoffice.com
*/

@Service
public class Util {
    @Autowired
    VersionService versionService;

    @Autowired
    NodeService nodeService;

    @Autowired
    SearchService searchService;

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    ActivityService activityService;

    @Autowired
    SiteService siteService;

    @Autowired
    TenantService tenantService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final QName EditingKeyAspect = QName.createQName("onlyoffice:editing-key");
    public static final QName EditingHashAspect = QName.createQName("onlyoffice:editing-hash");
    public static final QName ForcesaveAspect = QName.createQName("onlyoffice:forcesave");

    public void ensureVersioningEnabled(NodeRef nodeRef) {
        Map<QName, Serializable> versionProps = new HashMap<>();
        versionProps.put(ContentModel.PROP_AUTO_VERSION, true);
        versionProps.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
        versionService.ensureVersioningEnabled(nodeRef, versionProps);
    }

    public String generateHash() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    public NodeRef getNodeByPath(String path) {
        String storePath = "workspace://SpacesStore";
        StoreRef storeRef = new StoreRef(storePath);
        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, path, null, namespaceService, false);
        return nodeRefs.get(0);
    }

    public String getCorrectName(NodeRef nodeFolder, String title, String ext) {
        String name = (title + "." + ext).replaceAll("[*?:\"<>/|\\\\]","_");
        NodeRef node = nodeService.getChildByName(nodeFolder, ContentModel.ASSOC_CONTAINS, name);

        Integer i = 0;
        while (node != null) {
            i++;
            name = title + " (" + i + ")." + ext;
            node = nodeService.getChildByName(nodeFolder, ContentModel.ASSOC_CONTAINS, name);
        }
        return name;
    }

    public NodeRef getParentNodeRef (NodeRef node) {
        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(node);
        if (parentAssoc == null || parentAssoc.getParentRef() == null) {
            return null;
        } else {
            return parentAssoc.getParentRef();
        }
    }

    public NodeRef getChildNodeByName(NodeRef nodeRef, String name) {
        List<ChildAssociationRef> changesNodeRef = nodeService.getChildAssocs(nodeRef);

        for (ChildAssociationRef assoc : changesNodeRef) {
            if (getTitle(assoc.getChildRef()).equals(name)) {
                return assoc.getChildRef();
            }
        }

        return null;
    }

    public String getTitle(NodeRef nodeRef) {
        return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    }

    private String getCurrentTenantDomain() {
        String tenantDomain = tenantService.getCurrentUserDomain();
        if (tenantDomain == null) {
            return TenantService.DEFAULT_DOMAIN;
        }

        return tenantDomain;
    }

    public void postActivity(NodeRef nodeRef, boolean isNew) {
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN)) {
            return;
        }

        SiteInfo siteInfo = siteService.getSite(nodeRef);
        String siteId = (siteInfo != null ? siteInfo.getShortName() : null);

        if (siteId == null || siteId.equals("")) {
            return;
        }

        JSONObject json = new JSONObject();

        try {
            json.put("nodeRef", nodeRef);
            json.put("title", nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
            json.put("page", "document-details?nodeRef=" + nodeRef);

            String tenantDomain = getCurrentTenantDomain();

            if (tenantDomain!= null && !tenantDomain.equals(TenantService.DEFAULT_DOMAIN)) {
                json.put("tenantDomain", tenantDomain);
            }
        } catch (JSONException jsonError) {
            throw new AlfrescoRuntimeException("Unabled to create activities json", jsonError);
        }

        activityService.postActivity(
                isNew ? ActivityType.FILE_ADDED : ActivityType.FILE_UPDATED,
                siteId,
                "onlyoffice",
                json.toString()
        );
    }
}
