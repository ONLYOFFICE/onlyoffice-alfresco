<!--
    Copyright (c) Ascensio System SIA 2026. All rights reserved.
    http://www.onlyoffice.com
-->
<html>
<head>
    <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>

    <title>${docTitle!} - ONLYOFFICE</title>

    <#if doRedirect>
        <script type="text/javascript">
           document.location.href = "${page.url.uri}?nodeRef=${nodeRef!}";
        </script>
    </#if>

    <link href="${url.context}/res/components/onlyoffice/onlyoffice.css" type="text/css" rel="stylesheet">

    <!--Change the address on installed ONLYOFFICE™ Online Editors-->
    <script id="scriptApi" type="text/javascript" src="${documentServerApiUrl!}"></script>
    <link rel="shortcut icon" href="${url.context}/res/components/images/filetypes/${documentType!}.ico" type="image/vnd.microsoft.icon" />
    <link rel="icon" href="${url.context}/res/components/images/filetypes/${documentType!}.ico" type="image/vnd.microsoft.icon" />

    <!-- Alfresco web framework common resources -->
    <script type="text/javascript" src="${url.context}/res/js/yui-common.js"></script>
    <script type="text/javascript" src="${url.context}/noauth/messages.js?locale=${locale}"></script>
    <script type="text/javascript" src="${url.context}/res/js/bubbling.v2.1.js"></script>
    <script type="text/javascript">
        YAHOO.Bubbling.unsubscribe = function(layer, handler, scope) {
            this.bubble[layer].unsubscribe(handler, scope);
        };
    </script>
    <script type="text/javascript">
    <!-- Alfresco web framework constants -->
        Alfresco.constants = Alfresco.constants || {};
        Alfresco.constants.PROXY_URI = window.location.protocol + "//" + window.location.host + "${url.context?js_string}/proxy/alfresco/";
        Alfresco.constants.PROXY_URI_RELATIVE = "${url.context?js_string}/proxy/alfresco/";
        Alfresco.constants.THEME = "${theme}";
        Alfresco.constants.USERNAME = "${(user.name!"")?js_string}";
        Alfresco.constants.SITE = "<#if page??>${(page.url.templateArgs.site!"")?url?js_string}</#if>";
        Alfresco.constants.URL_CONTEXT = "${url.context?js_string}/";
        Alfresco.constants.URL_RESCONTEXT = "${url.context?js_string}/res/";
        Alfresco.constants.URL_PAGECONTEXT = "${url.context?js_string}/page/";
        Alfresco.constants.URL_SERVICECONTEXT = "${url.context?js_string}/service/";
        Alfresco.constants.URI_TEMPLATES =
            {
                "remote-site-page": "/site/{site}/{pageid}/p/{pagename}",
                "remote-page": "/{pageid}/p/{pagename}",
                "share-site-page": "/site/{site}/{pageid}/ws/{webscript}",
                "sitedashboardpage": "/site/{site}/dashboard",
                "contextpage": "/context/{pagecontext}/{pageid}",
                "sitepage": "/site/{site}/{pageid}",
                "userdashboardpage": "/user/{userid}/dashboard",
                "userpage": "/user/{userid}/{pageid}",
                "userprofilepage": "/user/{userid}/profile",
                "userdefaultpage": "/user/{pageid}",
                "consoletoolpage": "/console/{pageid}/{toolid}",
                "consolepage": "/console/{pageid}",
                "share-page": "/{pageid}/ws/{webscript}"
            };

        Alfresco.constants.JS_LOCALE = "${locale}";
        Alfresco.constants.CSRF_POLICY = {
            enabled: ${((config.scoped["CSRFPolicy"]["filter"].getChildren("rule")?size > 0)?string)!false},
            cookie: "${config.scoped["CSRFPolicy"]["client"].getChildValue("cookie")!""}",
            header: "${config.scoped["CSRFPolicy"]["client"].getChildValue("header")!""}",
            parameter: "${config.scoped["CSRFPolicy"]["client"].getChildValue("parameter")!""}",
            properties: {}
        };
        <#if config.scoped["CSRFPolicy"]["properties"]??>
            <#assign csrfProperties = (config.scoped["CSRFPolicy"]["properties"].children)![]>
            <#list csrfProperties as csrfProperty>
        Alfresco.constants.CSRF_POLICY.properties["${csrfProperty.name?js_string}"] = "${(csrfProperty.value!"")?js_string}";
            </#list>
        </#if>

        Alfresco.constants.IFRAME_POLICY = {
            sameDomain: "${config.scoped["IFramePolicy"]["same-domain"].value!"allow"}",
            crossDomainUrls: [<#list (config.scoped["IFramePolicy"]["cross-domain"].childrenMap["url"]![]) as c>
                "${c.value?js_string}"<#if c_has_next>,</#if>
            </#list>]
        };

        Alfresco.constants.HIDDEN_PICKER_VIEW_MODES = [
            <#list config.scoped["DocumentLibrary"]["hidden-picker-view-modes"].children as viewMode>
                <#if viewMode.name?js_string == "mode">"${viewMode.value?js_string}"<#if viewMode_has_next>,</#if></#if>
            </#list>
        ];
    </script>
    <script type="text/javascript" src="${url.context}/res/js/alfresco.js"></script>
    <script type="text/javascript" src="${url.context}/res/modules/document-picker/document-picker.js"></script>
    <script type="text/javascript" src="${url.context}/res/components/object-finder/object-finder.js"></script>
    <script type="text/javascript" src="${url.context}/res/components/common/common-component-style-filter-chain.js"></script>
    <script type="text/javascript" src="${url.context}/res/components/documentlibrary/tree.js"></script>
    <script type="text/javascript" src="${url.context}/res/modules/documentlibrary/global-folder.js"></script>
    <script type="text/javascript" src="${url.context}/res/modules/documentlibrary/copy-move-to.js"></script>
    <script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js"></script>
    <script type="text/javascript" src="${url.context}/res/js/forms-runtime.js"></script>
    <script type="text/javascript" src="${url.context}/res/modules/simple-dialog.js"></script>
    <script type="text/javascript" src="${url.context}/res/components/manage-permissions/manage-permissions.js"></script>
    <script type="text/javascript" src="${url.context}/res/modules/roles-tooltip.js"></script>
    <script type="text/javascript" src="${url.context}/res/components/people-finder/authority-finder.js"></script>
    <script type="text/javascript" src="${url.context}/res/templates/manage-permissions/template.manage-permissions.js"></script>

    <link rel="stylesheet" type="text/css" href="${url.context}/res/css/yui-fonts-grids.css" />
    <#if theme = 'default'>
        <link rel="stylesheet" type="text/css" href="${url.context}/res/yui/assets/skins/default/skin.css" />
    <#else>
        <link rel="stylesheet" type="text/css" href="${url.context}/res/themes/${theme}/yui/assets/skin.css" />
    </#if>
    <link rel="stylesheet" type="text/css" href="${url.context}/res/css/base.css" />
    <link rel="stylesheet" type="text/css" href="${url.context}/res/css/yui-layout.css" />
    <link rel="stylesheet" type="text/css" href="${url.context}/res/themes/${theme}/presentation.css" />
    <link rel="stylesheet" type="text/css" href="${url.context}/res/modules/documentlibrary/global-folder.css" />
    <link rel="stylesheet" type="text/css" href="${url.context}/res/components/documentlibrary/tree.css">
    <link rel="stylesheet" type="text/css" href="${url.context}/res/modules/document-picker/document-picker.css" />
    <link rel="stylesheet" type="text/css" href="${url.context}/res/components/object-finder/object-finder.css" />
    <link rel="stylesheet" type="text/css" href="${url.context}/res/modules/roles-tooltip.css" />
    <link rel="stylesheet" type="text/css" href="${url.context}/res/components/people-finder/authority-finder.css" />
    <link rel="stylesheet" type="text/css" href="${url.context}/res/components/manage-permissions/manage-permissions.css" />
</head>

<body id="Share" class="yui-skin-${theme} alfresco-share claro">
    <div id="placeholder"></div>
    <script>
        var documentPicker = new Alfresco.module.DocumentPicker("onlyoffice-editor-docPicker", Alfresco.ObjectRenderer);
        documentPicker.setOptions({
           displayMode: "items",
           itemFamily: "node",
           itemType: "cm:content",
           multipleSelectMode: false,
           parentNodeRef: "${folderNode!}",
           restrictParentNavigationToDocLib: true
        });
        documentPicker.onComponentsLoaded(); // Need to force the component loaded call to ensure setup gets completed.

        YAHOO.Bubbling.on("onDocumentsSelected", (eventName, payload) => {
             documentPicker.onDocumentsSelected(eventName, payload);
        });

        var onAppReady = function (event) {
            if (${(demo!false)?c}) {
                 docEditor.showMessage("${msg("alfresco.document.onlyoffice.action.edit.msg.demo")}");
            }
        };

        var onMetaChange = function (event) {
            var favorite = event.data.favorite;

            Alfresco.util.Ajax.jsonPost({
                url:  Alfresco.constants.PROXY_URI + "${favorite!}",
                successCallback: {
                    fn: function () {
                        docEditor.setFavorite(favorite);
                    },
                    scope: this
                }
            });
        };

        var onRequestHistoryClose = function () {
            document.location.reload();
        };

        var onRequestHistory = function () {
            Alfresco.util.Ajax.jsonGet({
                url:  Alfresco.constants.PROXY_URI + "${historyInfoUrl!}",
                successCallback: {
                    fn: function (response) {
                        var historyInfo = response.json;

                        for (var i = 0; i < historyInfo.history.length; i++) {
                            historyInfo.history[i].created = Alfresco.util.formatDate(Alfresco.util.fromISO8601(historyInfo.history[i].created), "ddd d mmm yyyy HH:MM:ss");

                            if (historyInfo.history[i].changes) {
                                for (var t = 0; t < historyInfo.history[i].changes.length; t++) {
                                    const created = new Date(historyInfo.history[i].changes[t].created);
                                    const createdUTC = new Date(Date.UTC(
                                        created.getFullYear(),
                                        created.getMonth(),
                                        created.getDate(),
                                        created.getHours(),
                                        created.getMinutes(),
                                        created.getSeconds()
                                    ));

                                    historyInfo.history[i].changes[t].created = Alfresco.util.formatDate(Alfresco.util.fromISO8601(createdUTC.toISOString()), "ddd d mmm yyyy HH:MM:ss");
                                }
                            }
                        }

                        docEditor.refreshHistory({
                            currentVersion: historyInfo.currentVersion,
                            history: historyInfo.history
                        });
                    },
                    scope: this
                }
            });
        };

        var onRequestHistoryData = function (event) {
            var version = event.data;

            Alfresco.util.Ajax.jsonGet({
                url:  Alfresco.constants.PROXY_URI + "${historyDataUrl!}" + "&version=" + version,
                successCallback: {
                    fn: function (response) {
                        var historyData = response.json;
                        if (historyData) {
                            docEditor.setHistoryData(historyData);
                        }
                    },
                    scope: this
                }
            });
        };

        var onRequestInsertImage = function (event) {
            documentPicker.singleSelectedItem = null;
            documentPicker.docEditorCommand = event.data.c;
            documentPicker.onDocumentsSelected = (eventName, payload) => {
                insertCallback(payload, docEditor.insertImage)
            };

            documentPicker.onShowPicker();
        };

        var onRequestMailMergeRecipients = function () {
            documentPicker.singleSelectedItem = null;
            documentPicker.docEditorCommand = null;
            documentPicker.onDocumentsSelected = (eventName, payload) => {
                insertCallback(payload, docEditor.setMailMergeRecipients)
            };

            documentPicker.onShowPicker();
        };

        var onRequestCompareFile = function () {
            documentPicker.singleSelectedItem = null;
            documentPicker.docEditorCommand = null;
            documentPicker.onDocumentsSelected = (eventName, payload) => {
                insertCallback(payload, docEditor.setRevisedFile)
            };

            documentPicker.onShowPicker();
        };

        const insertCallback = (payload, docEditorMethod) => {
            if (payload && payload[1].items) {
                var items = [];

                for (var i = 0; i < payload[1].items.length; i++) {
                    items.push(payload[1].items[i].nodeRef);
                }

                if (items.length > 0) {
                    Alfresco.util.Ajax.jsonPost({
                        url: Alfresco.constants.PROXY_URI + "parashift/onlyoffice/editor-api/insert",
                        dataObj: {
                             command: documentPicker.docEditorCommand,
                             nodes: items
                        },
                        successCallback: {
                            fn: function(response) {
                                docEditorMethod(response.json[0]);
                            },
                            scope: this
                        }
                    });
                }
            }
        };

        var onRequestSharingSettings = function (event) {
            var id = "doc-manage-permissions";

            if (YAHOO.Bubbling.defaultActions["action-link"] != null) {
                delete YAHOO.Bubbling.defaultActions["action-link"];
            }

            var managePermissionsDialog = new Alfresco.module.SimpleDialog(id);

            managePermissionsDialog.setOptions({
                width: "auto",
                destroyOnHide: true,
                templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "components/manage-permissions/manage-permissions?nodeRef=${nodeRef}&htmlid=" + id,
                doBeforeDialogShow: {
                    fn: function onPermissionsTemplateLoaded() {
                        this.dialog.header.innerText = Alfresco.util.message("page.managePermissions.title");

                        var body = document.createElement("div");
                        body.className="bd";
                        body.style.cssText = "padding-bottom: 1em;";

                        this.dialog.element.querySelector("#" + id + "-body_h").after(body);

                        while (body.nextSibling) {
                            body.appendChild(body.nextSibling);
                        }

                        // fix before https://github.com/Alfresco/alfresco-community-share/blob/24adbcea9179ff70075b2ee85ab90003acdaf7fe/share/src/main/webapp/modules/simple-dialog.js#L382
                        this.widgets.okButton = Alfresco.util.createYUIButton(this, "okButton", null, { type: "submit" });
                        this.widgets.cancelButton = Alfresco.util.createYUIButton(this, "cancelButton", this.onCancel);

                        var managePermissions = new Alfresco.template.ManagePermissions();

                        managePermissions.setOptions({
                            nodeRef: new Alfresco.util.NodeRef("${nodeRef}"),
                            siteId: "${page.url.templateArgs.site!""}",
                            rootNode: "${(config.scoped["RepositoryLibrary"]["root-node"].getValue())!"alfresco://company/home"}"
                        });

                        managePermissions.onReady();
                        Alfresco.util.ComponentManager.get(id).onReady();

                        Alfresco.util.ComponentManager.get(id)._navigateForward = function() {
                            managePermissionsDialog.hide();
                        }
                    }
                }
            }).show();
        }

        var onRequestSaveAs = function (event) {
            var copyMoveTo = new Alfresco.module.DoclibCopyMoveTo("onlyoffice-editor-copyMoveTo");
            copyMoveTo.setOptions({
                mode: "move",
                siteId: Alfresco.constants.SITE,
                path: "/",
                files: {
                    "node": {}
                },
                parentId: "${folderNode!}",
                title: "${msg("onlyoffice.editor.dialog.save-as.title")}",
                zIndex: 1000
            });

            var title = event.data.title.substring(0, event.data.title.lastIndexOf("."));
            var ext = event.data.title.split(".").pop();
            var url = event.data.url;
            var time = 600;

            function insertFileNameInput () {
                if (!copyMoveTo.widgets.dialog && time > 0) {
                    time--;
                    setTimeout(insertFileNameInput, 100);
                } else if (!copyMoveTo.fileNameInput) {
                    copyMoveTo.widgets.dialog.hide();
                    copyMoveTo.widgets.okButton.set("label", "${msg('button.save')}");

                    var fileNameDiv = document.createElement("div");
                        fileNameDiv.classList.add("wrapper");
                    var fileNameLabel = document.createElement("h3");
                        fileNameLabel.classList.add("fileNameLabel");
                        fileNameLabel.innerHTML = "${msg('label.name')}:";
                    var fileNameInput = document.createElement("input");
                        fileNameInput.id = "fileNameInput";
                        fileNameInput.name = "fileNameInput";
                        fileNameInput.type = "text";
                        fileNameInput.value = title;

                    fileNameDiv.append(fileNameLabel);
                    fileNameDiv.append(fileNameInput);
                    copyMoveTo.widgets.dialog.body.prepend(fileNameDiv);

                    copyMoveTo.fileNameInput = true;
                    copyMoveTo.widgets.dialog.show();
                }

                if (copyMoveTo.fileNameInput) {
                    document.getElementById("fileNameInput").value = title;
                    document.getElementById("fileNameInput").classList.remove("invalid");
                }
            };

            copyMoveTo.showDialog();
            insertFileNameInput();

            copyMoveTo.onOK = function () {
                title = document.getElementById("fileNameInput").value;

                if (!title) {
                    document.getElementById("fileNameInput").classList.add("invalid");
                    return;
                }

                if (this.selectedNode) {
                    var requestData = {
                        title: title,
                        ext: ext,
                        url: url,
                        saveNode: this.selectedNode.data.nodeRef
                    };

                    copyMoveTo.widgets.escapeListener.disable();
                    copyMoveTo.widgets.dialog.hide();

                    var waitDialog = Alfresco.util.PopupManager.displayMessage({
                        text : "",
                        spanClass : "wait",
                        displayTime : 0
                    });

                    Alfresco.util.Ajax.jsonPost({
                        url: Alfresco.constants.PROXY_URI + "parashift/onlyoffice/editor-api/save-as",
                        dataObj: requestData,
                        successMessage: "${msg('onlyoffice.editor.dialog.save-as.message.success')}",
                        successCallback: {
                            fn: function () {
                                waitDialog.destroy();
                            },
                            scope: this
                        },
                        failureCallback: {
                            fn: function (response) {
                                var errorMessage = "";
                                if (response.serverResponse.status == 403) {
                                    errorMessage = "${msg('onlyoffice.editor.dialog.save-as.message.error.forbidden')}";
                                } else {
                                    errorMessage = "${msg('onlyoffice.editor.dialog.save-as.message.error.unknown')}";
                                }
                                waitDialog.destroy();
                                Alfresco.util.PopupManager.displayMessage({
                                    text: errorMessage
                                });
                            },
                            scope: this
                        }
                    });
                }
            };
        };

        const onRequestReferenceData = (event) => {
            requestReferenceData(
                event.data,
                (response) => {
                    docEditor.setReferenceData(response.json);
                },
                (response) => {
                    const status = response.serverResponse.status;
                    if (status == 403 || status == 404) {
                        errorMessage = "${msg('onlyoffice.editor.error.not-found')}";
                    } else {
                        errorMessage = "${msg('onlyoffice.editor.error.unknown')}";
                    }

                    docEditor.setReferenceData({error: errorMessage});
                }
            );
        };

        const onRequestReferenceSource = (event) => {
            documentPicker.singleSelectedItem = null;
            documentPicker.docEditorCommand = null;
            documentPicker.onDocumentsSelected = (eventName, payload) => {
                if (payload && payload[1].items) {
                    var items = [];

                    for (var i = 0; i < payload[1].items.length; i++) {
                        items.push(payload[1].items[i].nodeRef);
                    }

                    if (items.length > 0) {
                        const data = {
                            referenceData: {
                                fileKey: items[0]
                            }
                        };

                        requestReferenceData(
                            data,
                            (response) => {
                                const data = response.json;
                                if (data.fileType != "xlsx") {
                                    Alfresco.util.PopupManager.displayMessage({
                                        text: "${msg('onlyoffice.editor.error.unsupported-type')}"
                                    });
                                    return;
                                }

                                docEditor.setReferenceSource(data);
                            },
                            (response) => {
                                const status = response.serverResponse.status;
                                if (status == 403 || status == 404) {
                                    errorMessage = "${msg('onlyoffice.editor.error.not-found')}";
                                } else {
                                    errorMessage = "${msg('onlyoffice.editor.error.unknown')}";
                                }

                                Alfresco.util.PopupManager.displayMessage({
                                    text: errorMessage
                                });
                            }
                        );
                    }
                }
            };

            documentPicker.onShowPicker();
        };

        const requestReferenceData = (data, successCallback, failureCallback) => {
            Alfresco.util.Ajax.jsonPost({
                url: Alfresco.constants.PROXY_URI + "parashift/onlyoffice/editor-api/reference-data",
                dataObj: data,
                successCallback: {
                    fn: successCallback,
                    scope: this
                },
                failureCallback: {
                    fn: failureCallback,
                    scope: this
                }
            });
        };

        const onRequestOpen = (event) => {
            const data = event.data;

            requestReferenceData(
                data,
                (response) => {
                    window.open(response.json.link, data.windowName);
                },
                (response) => {
                    const requestOpenWindow = window.open("", data.windowName);
                    requestOpenWindow.close();

                    const status = response.serverResponse.status;
                    if (status == 403 || status == 404) {
                        errorMessage = "${msg('onlyoffice.editor.error.not-found')}";
                    } else {
                        errorMessage = "${msg('onlyoffice.editor.error.unknown')}";
                    }

                    Alfresco.util.PopupManager.displayMessage({
                        text: errorMessage
                    });
                }
            );
        }

        if (${(error!false)?c}) {
            Alfresco.util.PopupManager.displayMessage({
                text: Alfresco.util.message("onlyoffice.editor.error.not-found"),
                spanClass: "",
                displayTime: 0
            });
        } else {

            var editorConfig = Object.assign({}, ${editorConfig!});

            editorConfig.events = {
                "onAppReady": onAppReady,
                "onMetaChange": onMetaChange,
                "onRequestHistoryClose": onRequestHistoryClose,
                "onRequestHistory": onRequestHistory,
                "onRequestHistoryData": onRequestHistoryData,
                "onRequestInsertImage": onRequestInsertImage,
                "onRequestMailMergeRecipients": onRequestMailMergeRecipients,
                "onRequestCompareFile": onRequestCompareFile,
                "onRequestSaveAs": onRequestSaveAs,
                "onRequestReferenceData": onRequestReferenceData,
                "onRequestReferenceSource": onRequestReferenceSource,
                "onRequestOpen": onRequestOpen
            };
            if (${(canManagePermissions!false)?c}) {
                editorConfig.events.onRequestSharingSettings = onRequestSharingSettings;
            }

            if (/android|avantgo|playbook|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od|ad)|iris|kindle|lge |maemo|midp|mmp|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\|plucker|pocket|psp|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i
                .test(navigator.userAgent)) {
                editorConfig.type='mobile';
            }

            if(typeof DocsAPI !== "undefined") {
                var editorVersion = DocsAPI.DocEditor.version().split(".");
                if ((editorConfig.document.fileType === "docxf" || editorConfig.document.fileType === "oform")
                    && editorVersion[0] < 7) {
                    Alfresco.util.PopupManager.displayMessage({
                        text : Alfresco.util.message("onlyoffice.editor.old-version-for-docxf-and-oform"),
                        spanClass : "",
                        displayTime : 0
                    });
                } else if (editorVersion[0] < 6 || (editorVersion[0] == 6 && editorVersion[1] == 0)) {
                    Alfresco.util.PopupManager.displayMessage({
                        text : Alfresco.util.message("onlyoffice.editor.old-version.not-supported"),
                        spanClass : "",
                        displayTime : 0
                    });
                } else {
                    var docEditor = new DocsAPI.DocEditor("placeholder", editorConfig);
                }
            } else {
                Alfresco.util.PopupManager.displayMessage({
                    text: Alfresco.util.message("onlyoffice.editor.unreachable"),
                    spanClass: "",
                    displayTime: 0
                });
            }
        }
    </script>
</body>
</html>

