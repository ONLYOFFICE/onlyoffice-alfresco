package com.parashift.onlyoffice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.request.RequestManager;
import com.onlyoffice.manager.security.JwtManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.model.common.User;
import com.onlyoffice.model.documenteditor.HistoryData;
import com.onlyoffice.model.documenteditor.callback.History;
import com.onlyoffice.model.documenteditor.historydata.Previous;
import com.parashift.onlyoffice.sdk.manager.url.UrlManager;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/
@Service
public class HistoryManager {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    NodeService nodeService;

    @Autowired
    @Qualifier("dbNodeService")
    NodeService dbNodeService;

    @Autowired
    VersionService versionService;

    @Autowired
    MimetypeService mimetypeService;

    @Autowired
    ContentService contentService;

    @Autowired
    PersonService personService;

    @Autowired
    Util util;

    @Autowired
    UrlManager urlManager;

    @Autowired
    JwtManager jwtManager;

    @Autowired
    RequestManager requestManager;

    @Autowired
    SettingsManager settingsManager;

    @Autowired
    DocumentManager documentManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final QName ContentVersionUUID = QName.createQName("onlyoffice:content-version-uuid");

    public void saveHistory(NodeRef nodeRef, History history, String changesUrl) throws JsonProcessingException {
        logger.debug("Saving history for node: " + nodeRef.toString());

        saveHistoryData(nodeRef, objectMapper.writeValueAsString(history), "changes.json", true);
        saveHistoryData(nodeRef, changesUrl, "diff.zip", false);

        logger.debug("History saved successfully.");
    }

    private void saveHistoryData(final NodeRef nodeRef, final String data, final String name, final boolean fromString) {
        if (data == null || data.isEmpty()) {
            logger.error("Error saving history " + name + "History data is null!");
            return;
        }

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
            public Void doWork() throws Exception {
                NodeRef versionNode = versionService.getCurrentVersion(nodeRef).getFrozenStateNodeRef();
                NodeRef historyNode = util.getChildNodeByName(nodeRef, name);
                Version historyVersion = null;

                if (historyNode == null) {
                    historyNode = createHistoryNode(nodeRef, name);
                    logger.debug("History node was created: " + historyNode.toString());

                    historyVersion = versionService.getCurrentVersion(historyNode);
                } else {
                    logger.debug("History node already exists: " + historyNode.toString());
                    historyVersion = versionService.createVersion(historyNode, null);
                }

                NodeRef versionNodeHistory = new NodeRef(
                        StoreRef.PROTOCOL_WORKSPACE,
                        historyVersion.getFrozenStateNodeRef().getStoreRef().getIdentifier(),
                        historyVersion.getFrozenStateNodeRef().getId()
                );

                dbNodeService.setProperty(versionNodeHistory, ContentVersionUUID, versionNode.getId());

                String extension = name.substring(name.lastIndexOf(".") + 1).trim().toLowerCase();
                final String mimeType = mimetypeService.getMimetype(extension);
                final ContentWriter writer = contentService.getWriter(versionNodeHistory, ContentModel.PROP_CONTENT, true);

                if (fromString) {
                    writer.setMimetype(mimeType);
                    writer.putContent(data);
                } else {
                    requestManager.executeGetRequest(data, new RequestManager.Callback<Void>() {
                        public Void doWork(Object response) throws IOException {
                            writer.setMimetype(mimeType);
                            writer.putContent(((HttpEntity)response).getContent());
                            return null;
                        }
                    });
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private NodeRef createHistoryNode(NodeRef parentNodeRef, String name) {
        Map<QName, Serializable> props = new HashMap<>();

        props.put(ContentModel.PROP_NAME, name);
        props.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);

        NodeRef historyNode = nodeService.createNode(parentNodeRef, RenditionModel.ASSOC_RENDITION,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                ContentModel.TYPE_CONTENT, props).getChildRef();

        nodeService.addAspect(historyNode, RenditionModel.ASPECT_HIDDEN_RENDITION, null);

        util.ensureVersioningEnabled(historyNode);

        return historyNode;
    }

    private NodeRef getHistoryNodeForVersion(Version version, String name) {
        NodeRef historyNodeRef = util.getChildNodeByName(version.getVersionedNodeRef(), name);

        if (historyNodeRef != null) {
            List<Version> versionsHistory = (List<Version>) versionService.getVersionHistory(historyNodeRef).getAllVersions();
            for (Version versionHistory : versionsHistory) {
                String contentVersionUUID = (String) nodeService.getProperty(versionHistory.getFrozenStateNodeRef(), ContentVersionUUID);

                if (contentVersionUUID.equals(version.getFrozenStateNodeRef().getId())) {
                    return versionHistory.getFrozenStateNodeRef();
                }
            }
        }

        return null;
    }

    public NodeRef getHistoryNodeByVersionNode(NodeRef versionNodeRef, String name) {
        NodeRef historyNodeRef = null;

        versionNodeRef = new NodeRef(
                StoreRef.PROTOCOL_WORKSPACE,
                versionNodeRef.getStoreRef().getIdentifier(),
                versionNodeRef.getId()
        );

        NodeRef nodeRef = (NodeRef) nodeService.getProperty(versionNodeRef, Version2Model.PROP_QNAME_FROZEN_NODE_REF);

        if (nodeRef != null) {
            historyNodeRef = util.getChildNodeByName(nodeRef, name);
            if (historyNodeRef != null) {
                List<Version> versionsHistory = (List<Version>) versionService.getVersionHistory(historyNodeRef).getAllVersions();
                for (Version versionHistory : versionsHistory) {
                    String contentVersionUUID = (String) nodeService.getProperty(versionHistory.getFrozenStateNodeRef(), ContentVersionUUID);

                    if (contentVersionUUID.equals(versionNodeRef.getId())) {
                        return versionHistory.getFrozenStateNodeRef();
                    }
                }
            }
        }

        return null;
    }

    public Map<String, Object> getHistoryInfo(NodeRef nodeRef) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Version> versions = (List<Version>) versionService.getVersionHistory(nodeRef).getAllVersions();
        List<com.onlyoffice.model.documenteditor.history.Version> history = new ArrayList<>();

        Collections.reverse(versions);

        for (Version internalVersion : versions) {
            Date created = (Date) internalVersion.getVersionProperty(Version2Model.PROP_FROZEN_MODIFIED);

            com.onlyoffice.model.documenteditor.history.Version version = com.onlyoffice.model.documenteditor.history.Version.builder()
                    .version(internalVersion.getVersionLabel())
                    .key(
                            documentManager.getDocumentKey(
                                internalVersion.getFrozenStateNodeRef().toString(),
                                false
                            )
                    )
                    .created(ISO8601DateFormat.format(created))
                    .build();

            NodeRef person = personService.getPersonOrNull(internalVersion.getVersionProperty("modifier").toString());

            PersonService.PersonInfo personInfo = null;
            if (person != null) {
                personInfo = personService.getPerson(person);
                if (personInfo != null) {
                    User user = User.builder()
                            .id(personInfo.getUserName())
                            .name(personInfo.getFirstName() + " " + personInfo.getLastName())
                            .build();

                    version.setUser(user);
                }
            }

            NodeRef changesNodeRef = getHistoryNodeForVersion(internalVersion, "changes.json");

            if (changesNodeRef != null) {
                ContentReader reader = contentService.getReader(changesNodeRef, ContentModel.PROP_CONTENT);
                History changes = objectMapper.readValue(reader.getContentInputStream(), History.class);

                    version.setChanges(changes.getChanges());
                    version.setServerVersion(changes.getServerVersion());
            }

            history.add(version);
        }

        Map<String, Object> historyInfo = new HashMap<>();

        historyInfo.put("currentVersion", versionService.getCurrentVersion(nodeRef).getVersionLabel());
        historyInfo.put("history", history);

        return historyInfo;
    }

    public HistoryData getHistoryData(NodeRef nodeRef, String versionLabel) throws IOException {
        HistoryData historyData = null;
        Version previousMajorVersion = null;

        List<Version> versions = (List<Version>) versionService.getVersionHistory(nodeRef).getAllVersions();

        Collections.reverse(versions);

        for (Version version : versions) {
            VersionType versionType = (VersionType) version.getVersionProperty(VersionModel.PROP_VERSION_TYPE);
            if (version.getVersionLabel().equals(versionLabel)) {
                String versionFileName = documentManager.getDocumentName(version.getFrozenStateNodeRef().toString());

                historyData = HistoryData.builder()
                        .version(version.getVersionLabel())
                        .key(documentManager.getDocumentKey(version.getFrozenStateNodeRef().toString(), false))
                        .url(urlManager.getFileUrl(version.getFrozenStateNodeRef().toString()))
                        .fileType(documentManager.getExtension(versionFileName))
                        .build();

                NodeRef diffZipNodeRef = getHistoryNodeForVersion(version, "diff.zip");

                if (diffZipNodeRef != null) {
                    if (previousMajorVersion != null) {
                        String previousMajorVersionFileName = documentManager.getDocumentName(previousMajorVersion.getFrozenStateNodeRef().toString());

                        historyData.setChangesUrl(urlManager.getHistoryDiffUrl(version.getFrozenStateNodeRef()));
                        historyData.setPrevious(
                                Previous.builder()
                                        .key(documentManager.getDocumentKey(previousMajorVersion.getFrozenStateNodeRef().toString(), false))
                                        .url(urlManager.getFileUrl(previousMajorVersion.getFrozenStateNodeRef().toString()))
                                        .fileType(documentManager.getExtension(previousMajorVersionFileName))
                                        .build()
                        );
                    }
                }
            }

            if (versionType.equals(VersionType.MAJOR)) {
                previousMajorVersion = version;
            }
        }

        if (settingsManager.isSecurityEnabled() && historyData != null) {
            try {
                historyData.setToken(jwtManager.createToken(historyData));
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        return historyData;
    }
}
