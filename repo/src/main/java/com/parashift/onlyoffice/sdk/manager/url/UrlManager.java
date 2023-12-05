package com.parashift.onlyoffice.sdk.manager.url;

import org.alfresco.service.cmr.repository.NodeRef;

/*
   Copyright (c) Ascensio System SIA 2023. All rights reserved.
   http://www.onlyoffice.com
*/

public interface UrlManager extends com.onlyoffice.manager.url.UrlManager {
    String getHistoryDiffUrl(NodeRef nodeRef);
    String getShareUrl();
    String getEmbeddedSaveUrl(String fileId, String sharedId);
    String getEmbeddedSaveUrl(String fileId);
    String getFavoriteUrl(NodeRef nodeRef);
    String getHistoryInfoUrl(NodeRef nodeRef);
    String getHistoryDataUrl(NodeRef nodeRef);
}
