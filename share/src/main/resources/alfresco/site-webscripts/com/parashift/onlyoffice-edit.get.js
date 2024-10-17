/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

if (url.args.nodeRef) {
    var query = "nodeRef=" + url.args.nodeRef;
    if (url.args.readonly) query += "&readonly=1";
if (url.args.sample) query +="&sample=" + url.args.sample;

    pObj = eval('(' + remote.call("/parashift/onlyoffice/prepare?" + query) + ')');

    model.doRedirect = false;
    model.onlyofficeUrl = pObj.onlyofficeUrl;
    model.editorConfig = JSON.stringify(pObj.editorConfig);
    model.docTitle = pObj.editorConfig.document.title;
    model.documentType = pObj.editorConfig.documentType;
    model.folderNode = pObj.folderNode;
    model.demo = pObj.demo;
    model.favorite = pObj.favorite;
    model.historyInfoUrl = pObj.historyInfoUrl;
    model.historyDataUrl = pObj.historyDataUrl;
    model.canManagePermissions = pObj.canManagePermissions;
    model.nodeRef = url.args.nodeRef;
} else {
    var query = "parentNodeRef=" + url.args.parentNodeRef;
    query += "&new=" + url.args.new;
    if (url.args.templateNodeRef) query+= "&templateNodeRef=" + url.args.templateNodeRef;

    pObj = eval('(' + remote.call("/parashift/onlyoffice/prepare?" + query) + ')');

    model.doRedirect = true;
    model.nodeRef = pObj.nodeRef;
}
