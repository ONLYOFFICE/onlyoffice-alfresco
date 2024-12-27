/*
    Copyright (c) Ascensio System SIA 2025. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.sdk.manager.url;

import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.manager.url.DefaultUrlManager;
import com.onlyoffice.model.documenteditor.config.document.DocumentType;
import com.onlyoffice.model.settings.SettingsConstants;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.imap.ImapService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.UrlUtil;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.URLEncoder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class UrlManagerImpl extends DefaultUrlManager implements UrlManager {

    @Autowired
    private NodeService nodeService;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private SysAdminParams sysAdminParams;
    @Autowired
    private DocumentManager documentManager;
    @Autowired
    private ImapService imapService;

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

        return getAlfrescoUrl()
                + "s/parashift/onlyoffice/callback?nodeRef="
                + nodeRef.toString();
    }

    @Override
    public String getCreateUrl(final String fileId) {
        //Todo: check if user have access create new document in current folder
        NodeRef nodeRef = new NodeRef(fileId);

        String fileName = documentManager.getDocumentName(fileId);
        String folderNodeRef = this.nodeService.getPrimaryParent(nodeRef).getParentRef().toString();

        DocumentType documentType = documentManager.getDocumentType(fileName);

        String docMime;
        switch (documentType) {
            case CELL:
                docMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                break;
            case SLIDE:
                docMime = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                break;
            default:
                docMime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
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

        UrlBuilder urlBuilder = new UrlBuilder(UrlUtil.getShareUrl(sysAdminParams));
        urlBuilder.addPath("/proxy/alfresco-noauth/api/internal/shared/node");
        urlBuilder.addPath(sharedId);
        urlBuilder.addPath("content");
        urlBuilder.addPath(URLEncoder.encodeUriComponent(fileName));
        urlBuilder.addParameter("c", "force");
        urlBuilder.addParameter("noCache", new Date().getTime());
        urlBuilder.addParameter("a", "true");

        return urlBuilder.toString();
    }

    public String getEmbeddedSaveUrl(final String fileId) {
        NodeRef nodeRef = new NodeRef(fileId);
        String fileName = documentManager.getDocumentName(fileId);

        StoreRef storeRef = nodeRef.getStoreRef();
        UrlBuilder urlBuilder = new UrlBuilder(UrlUtil.getShareUrl(sysAdminParams));
        urlBuilder.addPath("/proxy/alfresco/slingshot/node/content");
        urlBuilder.addPath(storeRef.getProtocol());
        urlBuilder.addPath(storeRef.getIdentifier());
        urlBuilder.addPath(nodeRef.getId());
        urlBuilder.addPath(URLEncoder.encodeUriComponent(fileName));
        urlBuilder.addParameter("a", true);

        return urlBuilder.toString();
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
