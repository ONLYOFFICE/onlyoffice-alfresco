package com.parashift.onlyoffice.sdk.manager.url;

import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.manager.url.DefaultUrlManager;
import com.onlyoffice.model.documenteditor.config.document.DocumentType;
import com.onlyoffice.model.settings.SettingsConstants;
import com.parashift.onlyoffice.util.Util;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.imap.ImapService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.URLEncoder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/*
   Copyright (c) Ascensio System SIA 2024. All rights reserved.
   http://www.onlyoffice.com
*/

public class UrlManagerImpl extends DefaultUrlManager implements UrlManager {

    @Autowired
    @Qualifier("checkOutCheckInService")
    CheckOutCheckInService cociService;
    @Autowired
    NodeService nodeService;
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    SysAdminParams sysAdminParams;
    @Autowired
    DocumentManager documentManager;
    @Autowired
    ImapService imapService;

    public UrlManagerImpl(final SettingsManager settingsManager) {
        super(settingsManager);
    }

    @Override
    public String getFileUrl(final String fileId) {
        NodeRef nodeRef = new NodeRef(fileId);

        return getAlfrescoUrl()
                + "s/parashift/onlyoffice/download/file?nodeRef="
                + nodeRef.toString()
                + "&alf_ticket="
                + authenticationService.getCurrentTicket();
    }

    @Override
    public String getCallbackUrl(final String fileId) {
        NodeRef nodeRef = new NodeRef(fileId);

        String hash = null;
        if (cociService.isCheckedOut(nodeRef)) {
            hash = (String) nodeService.getProperty(cociService.getWorkingCopy(nodeRef), Util.EditingHashAspect);
        }

        return getAlfrescoUrl()
                + "s/parashift/onlyoffice/callback?nodeRef="
                + nodeRef.toString()
                + "&cb_key="
                + hash;
    }

    @Override
    public String getCreateUrl(final String fileId) {
        //Todo: check if user have access create new document in current folder
        NodeRef nodeRef = new NodeRef(fileId);

        String fileName = documentManager.getDocumentName(fileId);
        String folderNodeRef = this.nodeService.getPrimaryParent(nodeRef).getParentRef().toString();

        DocumentType documentType = documentManager.getDocumentType(fileName);

        String docMime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        switch (documentType) {
            case CELL: {
                docMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                break;
            }
            case SLIDE: {
                docMime = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                break;
            }
        }
        return getShareUrl() + "page/onlyoffice-edit?parentNodeRef=" + folderNodeRef + "&new=" + docMime;
    }

    @Override
    public String getTestConvertUrl(final String productUrl) {
        if (productUrl != null && !productUrl.isEmpty()) {
            return sanitizeUrl(productUrl)
                    + "alfresco/s/parashift/onlyoffice/convertertest?alf_ticket="
                    + authenticationService.getCurrentTicket();
        } else {
            return getAlfrescoUrl()
                    + "s/parashift/onlyoffice/convertertest?alf_ticket="
                    + authenticationService.getCurrentTicket();
        }
    }

    @Override
    public String getGobackUrl(final String fileId) {
        NodeRef nodeRef = new NodeRef(fileId);

        String url = imapService.getContentFolderUrl(nodeRef);

        if (url.contains("?filter=path|")) {
            List<String> urlParts = Arrays.asList(url.split("\\|"));
            url = urlParts.get(0) + URLEncoder.encodeUriComponent("|" + urlParts.get(1));
        }

        return url;
    }

    public String getHistoryDiffUrl(final NodeRef nodeRef) {
        return getAlfrescoUrl()
                + "s/parashift/onlyoffice/download/diff?nodeRef="
                + nodeRef.toString()
                + "&alf_ticket="
                + authenticationService.getCurrentTicket();
    }

    public String getShareUrl() {
        return UrlUtil.getShareUrl(sysAdminParams) + "/";
    }

    public String getEmbeddedSaveUrl(final String fileId, final String sharedId) {
        String fileName = documentManager.getDocumentName(fileId);

        StringBuilder embeddedSaveUrl = new StringBuilder(8);
        embeddedSaveUrl.append(UrlUtil.getShareUrl(sysAdminParams));
        embeddedSaveUrl.append("/proxy/alfresco-noauth/api/internal/shared/node/");
        embeddedSaveUrl.append(sharedId);
        embeddedSaveUrl.append("/content/");
        embeddedSaveUrl.append(URLEncoder.encodeUriComponent(fileName));
        embeddedSaveUrl.append("?c=force");
        embeddedSaveUrl.append("&noCache=" + new Date().getTime());
        embeddedSaveUrl.append("&a=true");

        return embeddedSaveUrl.toString();
    }

    public String getEmbeddedSaveUrl(final String fileId) {
        NodeRef nodeRef = new NodeRef(fileId);
        String fileName = documentManager.getDocumentName(fileId);

        StringBuilder embeddedSaveUrl = new StringBuilder(7);
        StoreRef storeRef = nodeRef.getStoreRef();
        embeddedSaveUrl.append(UrlUtil.getShareUrl(sysAdminParams));
        embeddedSaveUrl.append("/proxy/alfresco/slingshot/node/content");
        embeddedSaveUrl.append("/" + storeRef.getProtocol());
        embeddedSaveUrl.append("/" + storeRef.getIdentifier());
        embeddedSaveUrl.append("/" + nodeRef.getId());
        embeddedSaveUrl.append("/" + URLEncoder.encodeUriComponent(fileName));
        embeddedSaveUrl.append("?a=true");

        return embeddedSaveUrl.toString();
    }

    public String getFavoriteUrl(final NodeRef nodeRef) {
        return "parashift/onlyoffice/editor-api/favorite?nodeRef="
                + nodeRef.toString();
    }

    public String getHistoryInfoUrl(final NodeRef nodeRef) {
        return "parashift/onlyoffice/history/info?nodeRef="
                + nodeRef.toString();
    }

    public String getHistoryDataUrl(final NodeRef nodeRef) {
        return "parashift/onlyoffice/history/data?nodeRef="
                + nodeRef.toString();
    }

    private String getAlfrescoUrl() {
        String alfUrl = getSettingsManager().getSetting(SettingsConstants.PRODUCT_INNER_URL);
        if (alfUrl == null || alfUrl.isEmpty()) {
            return sanitizeUrl(UrlUtil.getAlfrescoUrl(sysAdminParams)) + "/";
        } else {
            return sanitizeUrl(alfUrl) + "/alfresco/";
        }
    }
}
