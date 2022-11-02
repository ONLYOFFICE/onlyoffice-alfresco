package com.parashift.onlyoffice.util;

import com.parashift.onlyoffice.constants.Format;
import com.parashift.onlyoffice.constants.Formats;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

/*
   Copyright (c) Ascensio System SIA 2022. All rights reserved.
   http://www.onlyoffice.com
*/

@Service
public class Util {
    @Autowired
    @Qualifier("checkOutCheckInService")
    CheckOutCheckInService cociService;

    @Autowired
    VersionService versionService;

    @Autowired
    NodeService nodeService;

    @Autowired
    ConfigManager configManager;

    @Autowired
    SearchService searchService;

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    UrlManager urlManager;

    @Autowired
    ActivityService activityService;

    @Autowired
    SiteService siteService;

    @Autowired
    TenantService tenantService;

    public static final QName EditingKeyAspect = QName.createQName("onlyoffice:editing-key");
    public static final QName EditingHashAspect = QName.createQName("onlyoffice:editing-hash");

    public static final Map<String, String> PathLocale = new HashMap<String, String>(){{
        put("az", "az-Latn-AZ");
        put("bg", "bg-BG");
        put("cs", "cs-CZ");
        put("de", "de-DE");
        put("el", "el-GR");
        put("en-GB", "en-GB");
        put("en", "en-US");
        put("es", "es-ES");
        put("fr", "fr-FR");
        put("it", "it-IT");
        put("ja", "ja-JP");
        put("ko", "ko-KR");
        put("lv", "lv-LV");
        put("nl", "nl-NL");
        put("pl", "pl-PL");
        put("pt-BR", "pt-BR");
        put("pt", "pt-PT");
        put("ru", "ru-RU");
        put("sk", "sk-SK");
        put("sv", "sv-SE");
        put("uk", "uk-UA");
        put("vi", "vi-VN");
        put("zh", "zh-CN");
    }};

    public String getKey(NodeRef nodeRef) {
        String key = null;
        if (cociService.isCheckedOut(nodeRef)) {
            key = (String) nodeService.getProperty(cociService.getWorkingCopy(nodeRef), EditingKeyAspect);
        }

        if (key == null) {
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
            String version = (String) properties.get(ContentModel.PROP_VERSION_LABEL);

            if (version == null || version.isEmpty()) {
                ensureVersioningEnabled(nodeRef);
                Version v = versionService.getCurrentVersion(nodeRef);
                key = nodeRef.getId() + "_" + v.getVersionLabel();
            } else {
                key = nodeRef.getId() + "_" + version;
            }
        }

        return key;
    }

    public void ensureVersioningEnabled(NodeRef nodeRef) {
        Map<QName, Serializable> versionProps = new HashMap<>();
        versionProps.put(ContentModel.PROP_INITIAL_VERSION, true);
        versionService.ensureVersioningEnabled(nodeRef, versionProps);
    }

    public String parseDate(String date){
        java.time.format.DateTimeFormatter dtf =
                java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy");
        LocalDateTime dateParsed = LocalDateTime.parse(date, dtf);
        return dateParsed.toString().replace("T", " ").substring(0, dateParsed.toString().length());
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

    public JSONArray getTemplates(NodeRef nodeRef, String docExt){
        JSONArray templates = new JSONArray();
        NodeRef templatesNodeRef = getNodeByPath("/app:company_home/app:dictionary/app:node_templates");
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(templatesNodeRef);
        String docType = getDocType(docExt);
        List<String> templateExtList = Arrays.asList("docx", "pptx", "xlsx");
        for(ChildAssociationRef assoc : assocs){
            String title = getTitle(assoc.getChildRef());
            String templateExt = getExtension(assoc.getChildRef());
            String templateType = getDocType(templateExt);
            if ((docType.equals(templateType) && templateExtList.contains(templateExt)) || (docType.equals("form") && templateExt.equals("docx"))) {
                JSONObject template = new JSONObject();
                String image = urlManager.getShareUrl() + "res/components/images/filetypes/" + (docType.equals("form") ? "word" : docType) + ".svg";
                String url = urlManager.getCreateNewUrl(nodeRef, docType) + "&templateNodeRef=" + assoc.getChildRef();
                try {
                    template.put("image", image);
                    template.put("title", title);
                    template.put("url", url);
                    templates.put(template);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return templates;
    }

    public String getFileName(String url)
    {
        if (url == null) return "";

        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        fileName = fileName.split("\\?")[0];
        return fileName;
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

    public String getFileExtension(String url)
    {
        String fileName = getFileName(url);
        if (fileName == null) return null;
        String fileExt = fileName.substring(fileName.lastIndexOf("."));
        return fileExt.toLowerCase();
    }

    public String getDocType(String ext) {
        List<Format> supportedFormats = Formats.getSupportedFormats();

        for (Format format : supportedFormats) {
            if (format.getName().equals(ext)) {
                return format.getType().name().toLowerCase();
            }
        }

        return null;
    }

    public boolean isEditable(String ext) {
        List<Format> supportedFormats = Formats.getSupportedFormats();
        Set<String> customizableEditableFormats = configManager.getCustomizableEditableSet();

        boolean defaultEditFormat = false;

        for (Format format : supportedFormats) {
            if (format.getName().equals(ext)) {
                defaultEditFormat = format.isEdit();
                break;
            }
        }

        return defaultEditFormat || customizableEditableFormats.contains(ext);
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

    public String getExtension(NodeRef nodeRef) {
        String title = getTitle(nodeRef);
        String extension = null;

        if (title != null) {
            int index =  title.lastIndexOf('.');
            if (index > -1 && (index < title.length() - 1)) {
                extension = title.substring(index + 1);
            }
        }

        return extension;
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
