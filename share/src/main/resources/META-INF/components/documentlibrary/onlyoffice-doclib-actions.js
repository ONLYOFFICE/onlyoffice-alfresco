/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

(function () {
    YAHOO.Bubbling.fire("registerAction",
    {
        actionName: "onOnlyofficeCreateDocx",
        fn: function (obj) { openAndRefresh(obj, "application/vnd.openxmlformats-officedocument.wordprocessingml.document"); }
    });
    YAHOO.Bubbling.fire("registerAction",
    {
        actionName: "onOnlyofficeCreateXlsx",
        fn: function (obj) { openAndRefresh(obj, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); }
    });
    YAHOO.Bubbling.fire("registerAction",
    {
        actionName: "onOnlyofficeCreatePptx",
        fn: function (obj) { openAndRefresh(obj, "application/vnd.openxmlformats-officedocument.presentationml.presentation"); }
    });
    function openAndRefresh(obj, mime) {
        window.open("onlyoffice-edit?parentNodeRef=" + obj.nodeRef + "&new=" + mime);
        setTimeout(function() { YAHOO.Bubbling.fire("metadataRefresh", obj); }, 1000);
    }
})();