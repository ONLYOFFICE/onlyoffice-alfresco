package com.parashift.onlyoffice.sdk.manager.document;

import com.onlyoffice.manager.document.DefaultDocumentManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.parashift.onlyoffice.util.Util;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.Serializable;
import java.util.Map;

/*
   Copyright (c) Ascensio System SIA 2024. All rights reserved.
   http://www.onlyoffice.com
*/

public class DocumentManagerImpl extends DefaultDocumentManager {

    @Autowired
    @Qualifier("checkOutCheckInService")
    CheckOutCheckInService cociService;

    @Autowired
    NodeService nodeService;

    public DocumentManagerImpl(final SettingsManager settingsManager) {
        super(settingsManager);
    }

    @Override
    public String getDocumentKey(final String fileId, final boolean embedded) {
        NodeRef nodeRef = new NodeRef(fileId);

        String key = null;
        if (cociService.isCheckedOut(nodeRef)) {
            key = (String) nodeService.getProperty(cociService.getWorkingCopy(nodeRef), Util.EDITING_HASH_ASPECT);
        }

        if (key == null) {
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
            String version = (String) properties.get(ContentModel.PROP_VERSION_LABEL);

            if (version == null || version.isEmpty()) {
                key = nodeRef.getId() + "_1.0";
            } else {
                key = nodeRef.getId() + "_" + version;
            }

            key = embedded ? key + "_embedded" : key;
        }

        return key;
    }

    @Override
    public String getDocumentName(final String fileId) {
        NodeRef nodeRef = new NodeRef(fileId);

        return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    }
}
