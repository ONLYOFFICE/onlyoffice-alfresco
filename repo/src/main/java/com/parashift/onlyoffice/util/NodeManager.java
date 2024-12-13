/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.util;

import com.onlyoffice.manager.request.RequestManager;
import com.onlyoffice.manager.settings.SettingsManager;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.hc.core5.http.HttpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class NodeManager {
    @Autowired
    private NodeService nodeService;
    @Autowired
    private ContentService contentService;
    @Autowired
    private VersionService versionService;
    @Autowired
    private SettingsManager settingsManager;
    @Autowired
    private RequestManager requestManager;

    public void createNewVersion(final NodeRef nodeRef, final String fileUrl) throws Exception {
        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        if (settingsManager.getSettingBoolean("minorVersion", false)) {
            versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
        } else {
            versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
        }

        createNewVersion(nodeRef, fileUrl, versionProperties);
    }

    public void createNewVersion(final NodeRef nodeRef, final String fileUrl,
                                 final Map<String, Serializable> versionProperties) throws Exception {
        requestManager.executeGetRequest(fileUrl, new RequestManager.Callback<Void>() {
            public Void doWork(final Object response) throws IOException {
                contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true)
                        .putContent(((HttpEntity) response).getContent());
                return null;
            }
        });

        versionService.createVersion(nodeRef, versionProperties);
    }

    public Map<QName, Serializable> getPropertiesByAspect(final NodeRef nodeRef, final QName aspect) {
        if (nodeService.hasAspect(nodeRef, aspect)) {
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

            return properties.entrySet().stream()
                    .filter(entry -> entry.getKey()
                            .getNamespaceURI()
                            .equals(aspect.getNamespaceURI()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        }

        return null;
    }

}
