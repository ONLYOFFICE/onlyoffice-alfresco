/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.sdk.manager.document;

import com.onlyoffice.manager.document.DefaultDocumentManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.parashift.onlyoffice.util.NodeManager;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;

import static com.parashift.onlyoffice.model.OnlyofficeDocsModel.PROP_DOCUMENT_KEY;

public class DocumentManagerImpl extends DefaultDocumentManager {

    @Autowired
    private NodeService nodeService;
    @Autowired
    private NodeManager nodeManager;


    public DocumentManagerImpl(final SettingsManager settingsManager) {
        super(settingsManager);
    }

    @Override
    public String getDocumentKey(final String fileId, final boolean embedded) {
        NodeRef nodeRef = new NodeRef(fileId);

        if (embedded || !nodeManager.isLocked(nodeRef)) {
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
            String version = (String) properties.get(ContentModel.PROP_VERSION_LABEL);

            String key;
            if (version == null || version.isEmpty()) {
                key = nodeRef.getId() + "_1.0";
            } else {
                key = nodeRef.getId() + "_" + version;
            }

            return embedded ? key + "_embedded" : key;
        } else {
            return (String) nodeService.getProperty(nodeRef, PROP_DOCUMENT_KEY);
        }
    }

    @Override
    public String getDocumentName(final String fileId) {
        NodeRef nodeRef = new NodeRef(fileId);

        return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    }
}
