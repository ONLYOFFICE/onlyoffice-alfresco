/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

Alfresco.WebPreview.prototype.Plugins.onlyoffice = function(wp, attributes) {
    this.wp = wp;
    return this;
};

Alfresco.WebPreview.prototype.Plugins.onlyoffice.prototype = {
    attributes: {},

    report: function() {
        return null;
    },

    display: function() {
        var previewElement = this.wp.getPreviewerElement();
        previewElement.style.width = "100%";
        previewElement.style.height = "75vh";
        return "<div id='embeddedView'></div>";
    }
};