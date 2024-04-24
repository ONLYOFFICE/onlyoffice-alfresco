/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

if (model.widgets) {
    for (var i = 0; i < model.widgets.length; i++) {
        var widget = model.widgets[i];
        if (widget.id == "WebPreview") {
            pObj = null;

            if (model.dependencyGroup == "document-details") {
                pObj = eval('(' + remote.call("/parashift/onlyoffice/prepare?nodeRef=" + model.nodeRef + "&preview=true") + ')');
            }
            if (model.dependencyGroup == "web-preview") {
                pObj = eval('(' + remote.call("/parashift/onlyoffice/prepare-quick-share?sharedId=" + model.nodeRef ) + ')');
            }

            if (pObj && pObj.previewEnabled && pObj.onlyofficeUrl && pObj.mime) {
                model.onlyofficeUrl = pObj.onlyofficeUrl;
                model.editorConfig = JSON.stringify(pObj.editorConfig);

                widget.options.pluginConditions = jsonUtils.toJSONString([{
                    attributes: {
                        mimeType: pObj.mime
                    },
                    plugins: [{
                        name: "onlyoffice"
                    }]
                }]);
            }
        }
    }
}
