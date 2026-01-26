/*
    Copyright (c) Ascensio System SIA 2026. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.client.DocumentServerClient;
import com.onlyoffice.manager.document.DocumentManager;
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
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.parashift.onlyoffice.model.OnlyofficeDocsModel.FORCESAVE_ASPECT;


@Service
public class HistoryManager {
    public static final QName CONTENT_VERSION_UUID = QName.createQName("onlyoffice:content-version-uuid");

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NodeService nodeService;

    @Autowired
    @Qualifier("dbNodeService")
    private NodeService dbNodeService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private MimetypeService mimetypeService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private PersonService personService;

    @Autowired
    private Util util;

    @Autowired
    private UrlManager urlManager;

    @Autowired
    private JwtManager jwtManager;

    @Autowired
    private DocumentServerClient documentServerClient;

    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private DocumentManager documentManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveHistory(final NodeRef nodeRef, final History history, final String changesUrl)
            throws JsonProcessingException {
        logger.debug("Saving history for node: " + nodeRef.toString());

        saveHistoryData(nodeRef, objectMapper.writeValueAsString(history), "changes.json", true);
        saveHistoryData(nodeRef, changesUrl, "diff.zip", false);

        logger.debug("History saved successfully.");
    }

    public void deleteHistory(final NodeRef nodeRef, final Version version) {
        NodeRef changesNodeRef = util.getChildNodeByName(nodeRef, "changes.json");
        if (changesNodeRef != null) {
            Version changesVersion = getHistoryNodeVersionForVersion(version, "changes.json");
            if (changesVersion != null) {
                versionService.deleteVersion(changesNodeRef, changesVersion);
            }
        }

        NodeRef diffZipNodeRef = util.getChildNodeByName(nodeRef, "diff.zip");
        if (diffZipNodeRef != null) {
            Version diffZipVersion = getHistoryNodeVersionForVersion(version, "diff.zip");
            if (diffZipVersion != null) {
                versionService.deleteVersion(diffZipNodeRef, diffZipVersion);
            }
        }
    }

    private void saveHistoryData(final NodeRef nodeRef, final String data, final String name,
                                 final boolean fromString) {
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
                } else {
                    logger.debug("History node already exists: " + historyNode.toString());
                }

                Map<String, Serializable> versionProperties = new HashMap<>();
                versionProperties.put(CONTENT_VERSION_UUID.getLocalName(), versionNode.getId());
                historyVersion = versionService.createVersion(historyNode, versionProperties);

                NodeRef versionNodeHistory = new NodeRef(
                        StoreRef.PROTOCOL_WORKSPACE,
                        historyVersion.getFrozenStateNodeRef().getStoreRef().getIdentifier(),
                        historyVersion.getFrozenStateNodeRef().getId()
                );

                String extension = name.substring(name.lastIndexOf(".") + 1).trim().toLowerCase();
                final String mimeType = mimetypeService.getMimetype(extension);
                final ContentWriter writer = contentService.getWriter(
                        versionNodeHistory,
                        ContentModel.PROP_CONTENT,
                        true
                );

                if (fromString) {
                    writer.setMimetype(mimeType);
                    writer.putContent(data);
                } else {
                    writer.setMimetype(mimeType);

                    documentServerClient.getFile(data, writer.getContentOutputStream());
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private NodeRef createHistoryNode(final NodeRef parentNodeRef, final String name) {
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

    private NodeRef getHistoryNodeForVersion(final Version version, final String name) {
        Version historyNodeVersion = getHistoryNodeVersionForVersion(version, name);

        if (historyNodeVersion != null) {
            return historyNodeVersion.getFrozenStateNodeRef();
        }

        return null;
    }

    private Version getHistoryNodeVersionForVersion(final Version version, final String name) {
        NodeRef historyNodeRef = util.getChildNodeByName(version.getVersionedNodeRef(), name);

        if (historyNodeRef != null) {
            List<Version> versionsHistory = (List<Version>) versionService.getVersionHistory(historyNodeRef)
                    .getAllVersions();
            for (Version versionHistory : versionsHistory) {
                String contentVersionUUID = (String) Optional.ofNullable(versionHistory.getVersionProperty(
                        CONTENT_VERSION_UUID.getLocalName())).orElse("");

                if (contentVersionUUID.isEmpty()) {
                    contentVersionUUID = (String) Optional.ofNullable(nodeService.getProperty(
                            versionHistory.getFrozenStateNodeRef(),
                            CONTENT_VERSION_UUID
                    )).orElse("");
                }

                if (contentVersionUUID.equals(version.getFrozenStateNodeRef().getId())) {
                    return versionHistory;
                }
            }
        }

        return null;
    }

    public NodeRef getHistoryNodeByVersionNode(final NodeRef versionNodeRef, final String name) {
        NodeRef historyNodeRef = null;

        NodeRef workspaceVersionNodeRef = new NodeRef(
                StoreRef.PROTOCOL_WORKSPACE,
                versionNodeRef.getStoreRef().getIdentifier(),
                versionNodeRef.getId()
        );

        NodeRef nodeRef = (NodeRef) nodeService.getProperty(
                workspaceVersionNodeRef,
                Version2Model.PROP_QNAME_FROZEN_NODE_REF
        );

        if (nodeRef != null) {
            historyNodeRef = util.getChildNodeByName(nodeRef, name);
            if (historyNodeRef != null) {
                List<Version> versionsHistory = (List<Version>) versionService.getVersionHistory(historyNodeRef)
                        .getAllVersions();
                for (Version versionHistory : versionsHistory) {

                    String contentVersionUUID = (String) Optional.ofNullable(versionHistory.getVersionProperty(
                            CONTENT_VERSION_UUID.getLocalName())).orElse("");

                    if (contentVersionUUID.isEmpty()) {
                        contentVersionUUID = (String) Optional.ofNullable(nodeService.getProperty(
                                versionHistory.getFrozenStateNodeRef(),
                                CONTENT_VERSION_UUID
                        )).orElse("");
                    }

                    if (contentVersionUUID.equals(workspaceVersionNodeRef.getId())) {
                        return versionHistory.getFrozenStateNodeRef();
                    }
                }
            }
        }

        return null;
    }

    public Map<String, Object> getHistoryInfo(final NodeRef nodeRef) throws IOException {
        VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
        List<Version> versions = new ArrayList<>();
        Version latestVersion = null;
        String currentVersion = null;

        if (versionHistory != null) {
            versions = new ArrayList<>(versionHistory.getAllVersions());
            latestVersion = versions.get(0);
            Collections.reverse(versions);
            currentVersion = versionService.getCurrentVersion(nodeRef).getVersionLabel();
        }

        List<com.onlyoffice.model.documenteditor.history.Version> history = new ArrayList<>();
        for (Version internalVersion : versions) {
            if (internalVersion.getVersionProperty(FORCESAVE_ASPECT.getLocalName()) == null
                    || !(Boolean) internalVersion.getVersionProperty(FORCESAVE_ASPECT.getLocalName())
                    || internalVersion.equals(latestVersion)) {


                Date created = (Date) internalVersion.getVersionProperty(Version2Model.PROP_FROZEN_MODIFIED);

                com.onlyoffice.model.documenteditor.history.Version version =
                        com.onlyoffice.model.documenteditor.history.Version.builder()
                                .version(internalVersion.getVersionLabel())
                                .key(
                                        documentManager.getDocumentKey(
                                                internalVersion.getFrozenStateNodeRef().toString(),
                                                false
                                        )
                                )
                                .created(ISO8601DateFormat.format(created))
                                .build();

                NodeRef person =
                        personService.getPersonOrNull(internalVersion.getVersionProperty("modifier").toString());

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
        }

        Map<String, Object> historyInfo = new HashMap<>();

        historyInfo.put("currentVersion", currentVersion);
        historyInfo.put("history", history);

        return historyInfo;
    }

    public HistoryData getHistoryData(final NodeRef nodeRef, final String versionLabel) throws IOException {
        HistoryData historyData = null;
        Version previousMajorVersion = null;
        VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
        List<Version> versions = new ArrayList<>();

        if (versionHistory != null) {
            versions = new ArrayList<>(versionHistory.getAllVersions());
            Collections.reverse(versions);
        }

        for (Version version : versions) {
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
                        String previousMajorVersionFileName = documentManager.getDocumentName(
                                previousMajorVersion.getFrozenStateNodeRef().toString()
                        );

                        historyData.setChangesUrl(urlManager.getHistoryDiffUrl(version.getFrozenStateNodeRef()));
                        historyData.setPrevious(
                                Previous.builder()
                                        .key(documentManager.getDocumentKey(
                                                previousMajorVersion.getFrozenStateNodeRef().toString(), false
                                        ))
                                        .url(urlManager.getFileUrl(
                                                previousMajorVersion.getFrozenStateNodeRef().toString()
                                        ))
                                        .fileType(documentManager.getExtension(previousMajorVersionFileName))
                                        .build()
                        );
                    }
                }
            }

            if (version.getVersionProperty(FORCESAVE_ASPECT.getLocalName()) == null
                || !(Boolean) version.getVersionProperty(FORCESAVE_ASPECT.getLocalName())) {
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
