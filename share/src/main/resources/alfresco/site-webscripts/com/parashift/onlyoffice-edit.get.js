/*
    Copyright (c) Ascensio System SIA 2021. All rights reserved.
    http://www.onlyoffice.com
*/

var query = "?nodeRef=" + url.args.nodeRef;
if (url.args.readonly) query += "&readonly=1";
if (url.args.new) query += "&new=" + url.args.new;
if (url.args.parentNodeRef) query+= "&parentNodeRef=" + url.args.parentNodeRef;

pObj = eval('(' + remote.call("/parashift/onlyoffice/prepare" + query) + ')');
model.onlyofficeUrl = pObj.onlyofficeUrl;
model.docTitle = pObj.config.document.title;
model.editorConfig = JSON.stringify(pObj.config);
model.demo = pObj.demo;
model.documentType=pObj.documentType;
model.share = pObj.share;
model.favorite = pObj.favorite;
model.historyUrl = pObj.historyUrl;
model.ticket = pObj.ticket;
model.saveAsUri = pObj.saveAsUri;
model.currentPath = pObj.currentPath;
