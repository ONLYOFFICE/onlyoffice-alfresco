package com.parashift.onlyoffice.util;

import com.parashift.onlyoffice.constants.Format;
import com.parashift.onlyoffice.constants.Formats;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.imap.ImapService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/*
   Copyright (c) Ascensio System SIA 2022. All rights reserved.
   http://www.onlyoffice.com
*/

@Service
public class Util {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Autowired
    @Qualifier("checkOutCheckInService")
    CheckOutCheckInService cociService;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    VersionService versionService;

    @Autowired
    SysAdminParams sysAdminParams;

    @Autowired
    NodeService nodeService;

    @Autowired
    PersonService personService;

    @Autowired
    ConfigManager configManager;

    @Autowired
    SearchService searchService;

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    ContentService contentService;

    @Autowired
    JwtManager jwtManager;

    @Autowired
    SiteService siteService;

    @Autowired
    ImapService imapService;

    public static final QName EditingKeyAspect = QName.createQName("onlyoffice:editing-key");
    public static final QName EditingHashAspect = QName.createQName("onlyoffice:editing-hash");
    private static final String HOME_DIRECTORY = "Company Home";

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

    public String getHash(NodeRef nodeRef) {
        String hash = null;
        if (cociService.isCheckedOut(nodeRef)) {
            hash = (String) nodeService.getProperty(cociService.getWorkingCopy(nodeRef), EditingHashAspect);
        }
        return hash;
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

    public String getCreateNewUrl(NodeRef nodeRef, String docExt){
        String folderNodeRef = this.nodeService.getPrimaryParent(nodeRef).getParentRef().toString();
        String docType = getDocType(docExt);
        String docMime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        switch (docType) {
            case "cell": {
                docMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                break;
            }
            case "slide": {
                docMime = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                break;
            }
        }
        return getShareUrl() + "page/onlyoffice-edit?parentNodeRef=" + folderNodeRef + "&new=" + docMime;
    }

    public String getFavoriteUrl(NodeRef nodeRef){
        return "parashift/onlyoffice/editor-api/favorite?nodeRef=" + nodeRef.toString();
    }

    public String getHistoryInfoUrl(NodeRef nodeRef) {
        return "parashift/onlyoffice/history/info?nodeRef=" + nodeRef.toString();
    }

    public String getHistoryDataUrl(NodeRef nodeRef) {
        return "parashift/onlyoffice/history/data?nodeRef=" + nodeRef.toString();
    }

    public String getHistoryDiffUrl(NodeRef nodeRef) {
        return getAlfrescoUrl() + "s/parashift/onlyoffice/download/diff?nodeRef=" + nodeRef.toString() + "&alf_ticket=" + authenticationService.getCurrentTicket();
    }

    public String getContentUrl(NodeRef nodeRef) {
        return  getAlfrescoUrl() + "s/parashift/onlyoffice/download/file?nodeRef=" + nodeRef.toString() + "&alf_ticket=" + authenticationService.getCurrentTicket();
    }

    public String getCallbackUrl(NodeRef nodeRef) {
        return getAlfrescoUrl() + "s/parashift/onlyoffice/callback?nodeRef=" + nodeRef.toString() + "&cb_key=" + getHash(nodeRef);
    }

    public String getTestConversionUrl() {
        return getAlfrescoUrl() + "s/parashift/onlyoffice/convertertest?alf_ticket=" + authenticationService.getCurrentTicket();
    }

    public String getEditorUrl() {
        return configManager.demoActive() ? configManager.getDemo("url") : (String) configManager.getOrDefault("url", "http://127.0.0.1/");
    }

    public String getBackUrl(NodeRef nodeRef){
        String url = imapService.getContentFolderUrl(nodeRef);

        if (url.contains("?filter=path|")) {
            List<String> urlParts = Arrays.asList(url.split("\\|"));
            url = urlParts.get(0) + URLEncoder.encodeUriComponent("|" + urlParts.get(1));
        }

        return url;
    }

    public String getEditorInnerUrl() {
        String url = (String) configManager.getOrDefault("innerurl", "");
        if (url.isEmpty() || configManager.demoActive()) {
            return getEditorUrl();
        } else {
            return url;
        }
    }

    public String getEmbeddedSaveUrl(String sharedId, String docTitle) {
        StringBuilder embeddedSaveUrl = new StringBuilder(8);
        embeddedSaveUrl.append(UrlUtil.getShareUrl(sysAdminParams));
        embeddedSaveUrl.append("/proxy/alfresco-noauth/api/internal/shared/node/");
        embeddedSaveUrl.append(sharedId);
        embeddedSaveUrl.append("/content/");
        embeddedSaveUrl.append(URLEncoder.encodeUriComponent(docTitle));
        embeddedSaveUrl.append("?c=force");
        embeddedSaveUrl.append("&noCache=" + new Date().getTime());
        embeddedSaveUrl.append("&a=true");

        return embeddedSaveUrl.toString();
    }

    public String getEmbeddedSaveUrl(NodeRef nodeRef, String docTitle) {
        StringBuilder embeddedSaveUrl = new StringBuilder(7);
        StoreRef storeRef = nodeRef.getStoreRef();
        embeddedSaveUrl.append(UrlUtil.getShareUrl(sysAdminParams));
        embeddedSaveUrl.append("/proxy/alfresco/slingshot/node/content");
        embeddedSaveUrl.append("/" + storeRef.getProtocol());
        embeddedSaveUrl.append("/" + storeRef.getIdentifier());
        embeddedSaveUrl.append("/" + nodeRef.getId());
        embeddedSaveUrl.append("/" + URLEncoder.encodeUriComponent(docTitle));
        embeddedSaveUrl.append("?a=true");

        return embeddedSaveUrl.toString();
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
            String docName = nodeService.getProperty(assoc.getChildRef(), ContentModel.PROP_NAME).toString();
            String templateExt = docName.substring(docName.lastIndexOf(".") + 1);
            String templateType = getDocType(templateExt);
            if ((docType.equals(templateType) && templateExtList.contains(templateExt)) || (docType.equals("form") && templateExt.equals("docx"))) {
                JSONObject template = new JSONObject();
                String image = getShareUrl() + "res/components/images/filetypes/" + (docType.equals("form") ? "word" : docType) + ".svg";
                String title = nodeService.getProperty(assoc.getChildRef(), ContentModel.PROP_NAME).toString();
                String url = getCreateNewUrl(nodeRef, docExt) + "&templateNodeRef=" + assoc.getChildRef();
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

    public String getShareUrl(){
        return UrlUtil.getShareUrl(sysAdminParams) + "/";
    }

    private String getAlfrescoUrl() {
        String alfUrl = (String) configManager.getOrDefault("alfurl", "");
        if (alfUrl.isEmpty()) {
            return UrlUtil.getAlfrescoUrl(sysAdminParams) + "/";
        } else {
            return alfUrl + "alfresco/";
        }
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

    public String replaceDocEditorURLToInternal(String url) {
        String innerDocEditorUrl = getEditorInnerUrl();
        String publicDocEditorUrl = getEditorUrl();
        if (!publicDocEditorUrl.equals(innerDocEditorUrl) && !configManager.demoActive()) {
            url = url.replace(publicDocEditorUrl, innerDocEditorUrl);
        }
        return url;
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
            if (nodeService.getProperty(assoc.getChildRef(), ContentModel.PROP_NAME).equals(name)) {
                return assoc.getChildRef();
            }
        }

        return null;
    }

    public String getTitle(NodeRef nodeRef) {
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        return (String) properties.get(ContentModel.PROP_NAME);
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
}
