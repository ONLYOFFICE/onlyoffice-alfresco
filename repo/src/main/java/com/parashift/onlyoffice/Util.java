package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
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
   Copyright (c) Ascensio System SIA 2021. All rights reserved.
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
    FileFolderService fileFolderService;

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

    private String parseDate(String date){
        java.time.format.DateTimeFormatter dtf =
                java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy");
        LocalDateTime dateParsed = LocalDateTime.parse(date, dtf);
        return dateParsed.toString().replace("T", " ").substring(0, dateParsed.toString().length());
    }

    public JSONObject getHistoryObj(NodeRef nodeRef){
        JSONObject historyObj = new JSONObject();
        JSONArray historyData = new JSONArray();
        JSONArray history = new JSONArray();
        try {
            List<Version> versions = (List<Version>) versionService.getVersionHistory(nodeRef).getAllVersions();
            for (Version version : versions) {
                Integer jsonZipIndex= null;
                JSONObject jsonVersion = new JSONObject();
                NodeRef person = personService.getPersonOrNull(version.getVersionProperty("modifier").toString());
                PersonService.PersonInfo personInfo = null;
                if (person != null) {
                    personInfo = personService.getPerson(person);
                }
                JSONObject user = new JSONObject();
                if (personInfo != null) {
                    user.put("id", personInfo.getUserName());
                    user.put("name", personInfo.getFirstName() + " " + personInfo.getLastName());
                }
                jsonVersion.put("created", parseDate(version.getVersionProperty("created").toString()));
                jsonVersion.put("user", user);
                jsonVersion.put("changes", (Collection) null);
                NodeRef zipNodeRef = null;
                if (!version.getVersionLabel().equals("1.0")) {
                    NodeRef jsonNodeRef = null;
                    Boolean isCurrentVersion = version.getVersionLabel().equals(versionService.getCurrentVersion(nodeRef).getVersionLabel());
                    for(ChildAssociationRef assoc : nodeService.getChildAssocs(isCurrentVersion ? nodeRef : version.getFrozenStateNodeRef())) {
                        if (nodeService.getProperty(assoc.getChildRef(), ContentModel.PROP_NAME).equals("changes.json")) {
                            jsonNodeRef = assoc.getChildRef();
                        } else if (nodeService.getProperty(assoc.getChildRef(), ContentModel.PROP_NAME).equals("diff.zip")) {
                            zipNodeRef = assoc.getChildRef();
                        }
                    }
                    if (jsonNodeRef != null) {
                        List<Version> jsonVersions = (List<Version>) versionService.getVersionHistory(jsonNodeRef).getAllVersions();
                        for (Version jsonNodeVersion : jsonVersions) {
                            if (checkTimes(jsonNodeVersion, version)) {
                                jsonZipIndex = jsonVersions.indexOf(jsonNodeVersion);
                            }
                        }
                        if (jsonZipIndex != null) {
                            NodeRef jsonNode;
                            if (jsonZipIndex != 0) {
                                jsonNode = jsonVersions.get(jsonZipIndex).getFrozenStateNodeRef();
                            } else {
                                jsonNode = jsonNodeRef;
                            }
                            ContentReader reader = this.contentService.getReader(jsonNode, ContentModel.PROP_CONTENT);
                            JSONObject hist = new JSONObject(reader.getContentString());
                            jsonVersion.put("changes", hist.getJSONArray("changes"));
                            jsonVersion.put("created", ((JSONObject) hist.getJSONArray("changes").get(0)).getString("created"));
                            jsonVersion.put("serverVersion", hist.getString("serverVersion"));
                            jsonVersion.put("user", ((JSONObject) hist.getJSONArray("changes").get(0)).getJSONObject("user"));
                        }
                    }
                }
                jsonVersion.put("key", getKey(nodeRef).split("_")[0] + "_" + version.getVersionLabel());
                jsonVersion.put("version", version.getVersionLabel());
                history.put(jsonVersion);
                JSONObject historyDataObj = new JSONObject();
                if (versionService.getVersionHistory(nodeRef).getAllVersions().size() == 1) {
                    historyDataObj.put("version", "1.0");
                    historyDataObj.put("key", getKey(nodeRef));
                    historyDataObj.put("url", getContentUrl(nodeRef));
                    historyData.put(historyDataObj);
                } else {
                    if (version.getVersionLabel().equals("1.0")) {
                        JSONObject firstVersion = new JSONObject();
                        NodeRef rootVersion = versionService.getVersionHistory(nodeRef).getRootVersion().getFrozenStateNodeRef();
                        firstVersion.put("version", "1.0");
                        firstVersion.put("key", getKey(rootVersion));
                        firstVersion.put("url", getContentUrl(rootVersion));
                        if (jwtManager.jwtEnabled()) {
                            try {
                                firstVersion.put("token", jwtManager.createToken(firstVersion));
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new WebScriptException("Unable to create JWT");
                            }
                        }
                        historyData.put(firstVersion);
                    } else {
                        NodeRef versionChild;
                        if (version.getVersionLabel().equals(versionService.getCurrentVersion(nodeRef).getVersionLabel())) {
                            versionChild = nodeRef;
                        } else {
                            versionChild = version.getFrozenStateNodeRef();
                        }
                        String vers = version.getVersionLabel();
                        historyDataObj.put("version", vers);
                        historyDataObj.put("key", getKey(nodeRef).split("_")[0] + "_" + vers);
                        historyDataObj.put("url", getContentUrl(versionChild));
                        if (jsonZipIndex != null) {
                            List<Version> zipVersions = (List<Version>) versionService.getVersionHistory(zipNodeRef).getAllVersions();
                            JSONObject previous = new JSONObject();
                            Integer previousMajor = getPreviousMajorVersion(versions, version);
                            if (previousMajor == null) {
                                throw new WebScriptException("Error to get previous major version");
                            }
                            String previousKey = getKey(versions.get(previousMajor).getFrozenStateNodeRef());
                            String previousUrl = getContentUrl(versions.get(previousMajor).getFrozenStateNodeRef());
                            previous.put("key", previousKey);
                            previous.put("url", previousUrl);
                            historyDataObj.put("previous", previous);
                            historyDataObj.put("changesUrl", jsonZipIndex == 0 ? getContentUrlWithoutJWTCheck(zipNodeRef) : getContentUrlWithoutJWTCheck(zipVersions.get(jsonZipIndex).getFrozenStateNodeRef()));
                        }
                        historyData.put(historyDataObj);
                    }
                }
                if (jwtManager.jwtEnabled()) {
                    try {
                        historyDataObj.put("token", jwtManager.createToken(historyDataObj));
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new WebScriptException("Unable to create JWT");
                    }
                }
                historyObj.put("data", historyData);
                historyObj.put("history", history);
            }
        } catch (JSONException ex){
            ex.printStackTrace();
        }
        return historyObj;
    }

    private Boolean checkTimes(Version jsonNodeVersion, Version version) {
        java.time.format.DateTimeFormatter dtf =
                java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy");
        LocalDateTime jsonDateParsed = LocalDateTime.parse(jsonNodeVersion.getVersionProperty("frozenModified").toString(), dtf);
        LocalDateTime nodeDateParsed = LocalDateTime.parse(version.getVersionProperty("frozenModified").toString(), dtf);
        if (jsonDateParsed.minusSeconds(1L).equals(nodeDateParsed) || jsonNodeVersion.getVersionProperty("modified").toString().equals(version.getVersionProperty("created").toString())
                || jsonNodeVersion.getVersionProperty("modified").toString().equals(version.getVersionProperty("modified").toString())
                || jsonNodeVersion.getVersionProperty("created").toString().equals(version.getVersionProperty("created").toString())
                || jsonNodeVersion.getVersionProperty("frozenModified").toString().equals(version.getVersionProperty("frozenModified").toString())) {
            return true;
        }
        return false;
    }

    private Integer getPreviousMajorVersion(List<Version> versions, Version versionForPreviousVersion) {
        int index = versions.indexOf(versionForPreviousVersion);
        for (int i = 0 ; i < versions.size(); i++) {
            if (versions.get(i).getVersionType() == VersionType.MAJOR && i > index) {
                return i;
            }
        }
        return null;
    }

    public String getCreateNewUrl(NodeRef nodeRef, String docExtMime){
        String folderNodeRef = this.nodeService.getPrimaryParent(nodeRef).getParentRef().toString();
        return getShareUrl() + "page/onlyoffice-edit?nodeRef=" + folderNodeRef + "&new=" + docExtMime;
    }

    public String getFavouriteUrl(NodeRef nodeRef){
        try {
            return getAlfrescoUrl() + "s/parashift/onlyoffice/favourite?nodeRef=" + java.net.URLEncoder.encode( nodeRef.toString(), String.valueOf(StandardCharsets.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHistoryUrl(NodeRef nodeRef) {
        return getAlfrescoUrl() + "s/parashift/onlyoffice/history?nodeRef=" + nodeRef.toString();
    }

    private String getContentUrlWithoutJWTCheck(NodeRef nodeRef) {
        return getAlfrescoUrl() + "s/parashift/onlyoffice/download?nodeRef=" + nodeRef.toString() + "&wjc=true&alf_ticket=" + authenticationService.getCurrentTicket();
    }

    public String getContentUrl(NodeRef nodeRef) {
        return  getAlfrescoUrl() + "s/parashift/onlyoffice/download?nodeRef=" + nodeRef.toString() + "&alf_ticket=" + authenticationService.getCurrentTicket();
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
        String path = "";
        while(this.nodeService.getPrimaryParent(nodeRef).getParentRef() != null){
            if(this.nodeService.getProperty(this.nodeService.getPrimaryParent(nodeRef).getParentRef(),
                    ContentModel.PROP_NAME).toString().equals(HOME_DIRECTORY)) {
                break;
            }
            path = ("/" + this.nodeService.getProperty(this.nodeService.getPrimaryParent(nodeRef).getParentRef(),
                    ContentModel.PROP_NAME)) + path;
            nodeRef=this.nodeService.getPrimaryParent(nodeRef).getParentRef();
        }
        path = "|" + path + "|";

        if(path.equals("||")){
            return getShareUrl() + "page/context/mine/myfiles";
        } else{
            try {
                return getShareUrl() + "page/context/mine/myfiles#filter=path" + java.net.URLEncoder.encode(path, String.valueOf(StandardCharsets.UTF_8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
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

    public JSONArray getTemplates(NodeRef nodeRef, String docExt, String mimeType){
        JSONArray templates = new JSONArray();
        NodeRef templatesNodeRef = getNodeByPath("/app:company_home/app:dictionary/app:node_templates");
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(templatesNodeRef);
        for(ChildAssociationRef assoc : assocs){
            String docName = nodeService.getProperty(assoc.getChildRef(), ContentModel.PROP_NAME).toString();
            if(docExt.equals(docName.substring(docName.lastIndexOf(".") + 1))){
                JSONObject template = new JSONObject();
                String image = getShareUrl() + "proxy/alfresco/api/node/workspace/SpacesStore/" + assoc.getChildRef().toString().split("/SpacesStore/")[1] + "/content/thumbnails/doclib?ph=true";
                String title = nodeService.getProperty(assoc.getChildRef(), ContentModel.PROP_NAME).toString();
                String url = getCreateNewUrl(nodeRef, mimeType) + "&parentNodeRef=" + assoc.getChildRef();
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
        String name = title + "." + ext;
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
        List<String> wordFormats = configManager.getListDefaultProperty("docservice.type.word");
        List<String> cellFormats = configManager.getListDefaultProperty("docservice.type.cell");
        List<String> slideFormats = configManager.getListDefaultProperty("docservice.type.slide");

        if (wordFormats.contains(ext)) return "word";
        if (cellFormats.contains(ext)) return "cell";
        if (slideFormats.contains(ext)) return "slide";

        return null;
    }

    public boolean isEditable(String mime) {
        List<String> defaultEditableMimeTypes = configManager.getListDefaultProperty("docservice.mime.edit");
        Set<String> customizableEditableMimeTypes = configManager.getCustomizableEditableSet();
        return defaultEditableMimeTypes.contains(mime) || customizableEditableMimeTypes.contains(mime);
    }

    public String replaceDocEditorURLToInternal(String url) {
        String innerDocEditorUrl = getEditorInnerUrl();
        String publicDocEditorUrl = getEditorUrl();
        if (!publicDocEditorUrl.equals(innerDocEditorUrl)) {
            url = url.replace(publicDocEditorUrl, innerDocEditorUrl);
        }
        return url;
    }

    public String getSaveAsUrl() {
        return getAlfrescoUrl() + "s/parashift/onlyoffice/saveas";
    }

    public String getCurrentPath(NodeRef nodeRef) {
        try {
            List<String> path = fileFolderService.getNameOnlyPath(nodeService.getRootNode(nodeRef.getStoreRef()), nodeRef);
            path.remove(path.size() - 1);
            path.remove(0);
            return org.json.simple.JSONArray.toJSONString(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
