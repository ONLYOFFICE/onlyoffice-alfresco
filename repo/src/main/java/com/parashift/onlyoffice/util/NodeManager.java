/*
    Copyright (c) Ascensio System SIA 2026. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.util;

import com.onlyoffice.client.DocumentServerClient;
import com.onlyoffice.manager.settings.SettingsManager;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


@Service
public class NodeManager {
    @Autowired
    private ContentService contentService;
    @Autowired
    private VersionService versionService;
    @Autowired
    private SettingsManager settingsManager;
    @Autowired
    private DocumentServerClient documentServerClient;

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
        ContentWriter contentWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

        documentServerClient.getFile(fileUrl, contentWriter.getContentOutputStream());

        versionService.createVersion(nodeRef, versionProperties);
    }

}
