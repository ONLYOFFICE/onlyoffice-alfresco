/*
    Copyright (c) Ascensio System SIA 2025. All rights reserved.
    http://www.onlyoffice.com
*/

(function () {
    var supportedFormats = null;

    var getOutputTypes = function (docExt) {
        var outputTypes = null;

        if (supportedFormats) {
            for (var format of supportedFormats) {
                if (format.name == docExt) {
                    outputTypes = format.convert;
                }
            }

            if (outputTypes != null) {
                if (outputTypes.includes(docExt)) {
                    outputTypes.splice(outputTypes.indexOf(docExt), 1);
                }
            }
        }

        return outputTypes;
    };

    var multipleDownloadPost = function (requestData) {
        var waitDialog = Alfresco.util.PopupManager.displayMessage({
            text : Alfresco.util.message("label.loading"),
            spanClass : "wait",
            displayTime : 0
        });

        Alfresco.util.Ajax.jsonPost({
            url: Alfresco.constants.PROXY_URI + "parashift/onlyoffice/download-as",
            responseContentType: "application/json",
            dataObj: requestData,
            successMessage: Alfresco.util.message("alfresco.document.onlyoffice.action.download-as.msg.success-multi"),
            failureMessage: Alfresco.util.message("alfresco.document.onlyoffice.action.download-as.msg.failure-multi"),
            successCallback: {
                fn: function (response) {
                    waitDialog.destroy()

                    var form = document.createElement("form");
                    form.method = "GET";
                    form.action = Alfresco.constants.PROXY_URI + response.json.downloadUrl;
                    document.body.appendChild(form);

                    var input = document.createElement("input");
                    input.type = "hidden"
                    input.name = "a"
                    input.value = "true";
                    form.appendChild(input);

                    var d = form.ownerDocument;
                    var iframe = d.createElement("iframe");
                    iframe.style.display = "none";
                    YAHOO.util.Dom.generateId(iframe, "downloadArchive");
                    iframe.name = iframe.id;
                    document.body.appendChild(iframe);

                    // makes it possible to target the frame properly in IE.
                    window.frames[iframe.name].name = iframe.name;

                    form.target = iframe.name;
                    form.submit();
                },
                scope: this
            },
            failureCallback: {
                fn: function () {
                    waitDialog.destroy()
                },
                scope: this
            }
        });
    };

    var dialogOnlyofficeDownloadAs = function (record) {
        var nodeList = [];
        if (Array.isArray(record)) {
            for (var node of record) {
                nodeList.push(node);
            }
        } else {
            nodeList.push(record);
        }

        var body = [];
        body.push(
            "<label>" + Alfresco.util.message("alfresco.document-onlyoffice-download-as.form.field.select-type") + "</label>",
            "<div class='download-as-main'>"
        );

        var showCheckbox = nodeList.length == 1 ? "hidden" : "";
        for (var node of nodeList) {
            var ext = node.displayName.substring(node.displayName.lastIndexOf(".") + 1);
            var outputTypes = getOutputTypes(ext);

            if (node.jsNode.mimetype != undefined && outputTypes != null) {
                body.push(
                    "<div class='download-as-file'>",
                    "<input type='checkbox' class='download-as-checkbox " + showCheckbox + "' checked='true' value='" + node.nodeRef + "'>",
                    "<p class='download-as-filename'>" + node.displayName + "</p>",
                    "<p class='download-as-convert-label'>" + Alfresco.util.message("alfresco.document-onlyoffice-download-as.form.field.p.convert-into") + "</p>",
                    "<div style='width: 80px;'>",
                    "<select class='download-as-select' onfocus='this.size=10;' onblur='this.size=1;' onchange='this.size=1; this.blur();'>"
                );

                for (var type of outputTypes) {
                    body.push("<option>" + type + "</option>");
                }

                body.push(
                    "</select>",
                    "</div>",
                    "</div>"
                );
            }
        }

        body.push("</div>");

        var prompt = new YAHOO.widget.SimpleDialog("prompt", {
            close:true,
            constraintoviewport: true,
            draggable: false,
            effect: null,
            modal: true,
            visible: false,
            zIndex: this.zIndex++,
            buttons: [
                {
                    text: Alfresco.util.message("alfresco.document-onlyoffice-download-as.form.submit-button"),
                    handler: function onAction_success() {
                        var requestData = [];
                        var fileList = document.getElementsByClassName("download-as-file");

                        for (var file of fileList) {
                            var checkbox = file.querySelector(".download-as-checkbox");
                            if (checkbox.checked) {
                                var select = file.querySelector(".download-as-select");
                                requestData.push({
                                    nodeRef: checkbox.value,
                                    outputType: select.value
                                });
                            }
                        }

                        if (requestData.length > 0) {
                            this.destroy();
                            multipleDownloadPost(requestData);
                        }
                    }
                },
                {
                    text : Alfresco.util.message("button.cancel"),
                    handler : function onAction_cancel() {
                        this.destroy();
                    },
                    isDefault : true
                }
            ]
        });

        prompt.setHeader(Alfresco.util.message("actions.document.onlyoffice-download-as"));
        prompt.setBody(body.join(""));
        prompt.render(document.body);
        prompt.center();
        prompt.show();

        YAHOO.util.Event.removeListener(prompt.close, "click");
        YAHOO.util.Event.on(prompt.close, "click", function() {
            this.destroy();
        }, prompt, true);
    }

    YAHOO.Bubbling.fire("registerAction", {
        actionName: "onOnlyofficeDownloadAs",
        fn: function (record, owner) {
            if (!supportedFormats) {
                var waitDialog = Alfresco.util.PopupManager.displayMessage({
                    text : "",
                    spanClass : "wait",
                    displayTime : 0
                });

                Alfresco.util.Ajax.jsonGet({
                    url: Alfresco.constants.PROXY_URI + "parashift/onlyoffice/onlyoffice-settings",
                    successCallback: {
                        fn: function(response) {
                            waitDialog.destroy()
                            supportedFormats = response.json.supportedFormats;
                            dialogOnlyofficeDownloadAs(record);
                        },
                        scope: this
                    },
                    failureCallback: {
                        fn: function () {
                            waitDialog.destroy()
                            Alfresco.util.PopupManager.displayMessage({
                                text: Alfresco.util.message("alfresco.document.onlyoffice.action.download-as.msg.failure-multi")
                            });
                        },
                        scope: this
                    }
                });
            } else {
                dialogOnlyofficeDownloadAs(record);
            }
        }
    });
})();