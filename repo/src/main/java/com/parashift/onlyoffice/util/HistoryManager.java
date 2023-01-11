package com.parashift.onlyoffice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/*
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
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

    public static final QName ContentVersionUUID = QName.createQName("onlyoffice:content-version-uuid");

    public void saveHistory(NodeRef nodeRef, JSONObject historyData, String changesUrl) {
        logger.debug("Saving history for node: " + nodeRef.toString());

        saveHistoryData(nodeRef, historyData.toString(), "changes.json", true);
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
                    requestManager.executeRequestToDocumentServer(data, new RequestManager.Callback<Void>() {
                        public Void doWork(HttpEntity httpEntity) throws IOException {
                            writer.setMimetype(mimeType);
                            writer.putContent(httpEntity.getContent());
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
        List<Info> history = new ArrayList<>();

        Collections.reverse(versions);

        for (Version version : versions) {
            Info info = new Info();
            info.setVersion(version.getVersionLabel());
            info.setKey(util.getKey(version.getFrozenStateNodeRef()));
            Date created = (Date) version.getVersionProperty(Version2Model.PROP_FROZEN_MODIFIED);
            info.setCreated(ISO8601DateFormat.format(created));

            NodeRef person = personService.getPersonOrNull(version.getVersionProperty("modifier").toString());

            PersonService.PersonInfo personInfo = null;
            if (person != null) {
                personInfo = personService.getPerson(person);
                if (personInfo != null) {
                    info.setUser(personInfo.getUserName(), personInfo.getFirstName() + " " + personInfo.getLastName());
                }
            }

            NodeRef changesNodeRef = getHistoryNodeForVersion(version, "changes.json");

            if (changesNodeRef != null) {
                ContentReader reader = contentService.getReader(changesNodeRef, ContentModel.PROP_CONTENT);
                JSONObject changes = null;

                try {
                    changes = new JSONObject(reader.getContentString());
                    info.setChanges(objectMapper.readValue(changes.getJSONArray("changes").toString(), Object.class));
                    info.setServerVersion(changes.getString("serverVersion"));
                } catch (JSONException e) {
                    throw new IOException(e.getMessage(), e);
                }
            }

            history.add(info);
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
                historyData = new HistoryData();
                historyData.setVersion(version.getVersionLabel());
                historyData.setKey(util.getKey(version.getFrozenStateNodeRef()));
                historyData.setUrl(urlManager.getContentUrl(version.getFrozenStateNodeRef()));
                historyData.setFileType(util.getExtension(version.getFrozenStateNodeRef()));

                NodeRef diffZipNodeRef = getHistoryNodeForVersion(version, "diff.zip");

                if (diffZipNodeRef != null) {
                    if (previousMajorVersion != null) {
                        historyData.setChangesUrl(urlManager.getHistoryDiffUrl(version.getFrozenStateNodeRef()));
                        historyData.setPrevious(
                                util.getKey(previousMajorVersion.getFrozenStateNodeRef()),
                                urlManager.getContentUrl(previousMajorVersion.getFrozenStateNodeRef()),
                                util.getExtension(previousMajorVersion.getFrozenStateNodeRef())
                        );
                    }
                }
            }

            if (versionType.equals(VersionType.MAJOR)) {
                previousMajorVersion = version;
            }
        }

        if (jwtManager.jwtEnabled() && historyData != null) {
            try {
                historyData.setToken(jwtManager.createToken(historyData));
            } catch (Exception e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        return historyData;
    }

    public class Info {
        public String version;
        public String key;
        public Object changes;
        public String created;
        public User user;
        public String serverVersion;

        public Info() { }

        public void setVersion(String version) { this.version = version; }

        public void setKey(String key) { this.key = key; }

        public void setChanges(Object changes) { this.changes = changes; }

        public void setCreated(String created) { this.created = created; }

        public void setUser(String id, String name) {
            this.user = new User(id, name);
        }

        public void setServerVersion(String serverVersion) { this.serverVersion = serverVersion; }

        public class User {
            public String id;
            public String name;

            public User(String id, String name) {
                this.id = id;
                this.name = name;
            }
        }
    }

    public class HistoryData {
        public String version;
        public String key;
        public String url;
        public String fileType;
        public String changesUrl;
        public Previous previous;
        public String token;

        public HistoryData() { }

        public void setVersion(String version) {
            this.version = version;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public void setChangesUrl(String changesUrl) {
            this.changesUrl = changesUrl;
        }

        public void setPrevious(String key, String url, String fileType) {
            this.previous = new Previous(key, url, fileType);
        }

        public void setToken(String token) { this.token = token; }

        public class Previous {
            public String key;
            public String url;
            public String fileType;

            public Previous(String key, String url, String fileType) {
                this.key = key;
                this.url = url;
                this.fileType = fileType;
            }
        }
    }
}
