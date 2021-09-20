<!--
    Copyright (c) Ascensio System SIA 2021. All rights reserved.
    http://www.onlyoffice.com
-->
<html>
<head>
    <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>

    <title>${docTitle} - ONLYOFFICE</title>

    <link href="${url.context}/res/components/onlyoffice/onlyoffice.css" type="text/css" rel="stylesheet">

    <!--Change the address on installed ONLYOFFICE™ Online Editors-->
    <script id="scriptApi" type="text/javascript" src="${onlyofficeUrl}OfficeWeb/apps/api/documents/api.js"></script>
    <link rel="shortcut icon" href="${url.context}/res/components/images/filetypes/${documentType}.ico" type="image/vnd.microsoft.icon" />
    <link rel="icon" href="${url.context}/res/components/images/filetypes/${documentType}.ico" type="image/vnd.microsoft.icon" />

    <script type="text/javascript" src="${url.context}/res/js/yui-common.js"></script>
    <script type="text/javascript" src="${url.context}/noauth/messages.js?locale=${locale}"></script>
    <script type="text/javascript" src="${url.context}/res/js/alfresco.js"></script>
    <#if theme = 'default'>
        <link rel="stylesheet" type="text/css" href="${url.context}/res/yui/assets/skins/default/skin.css" />
    <#else>
        <link rel="stylesheet" type="text/css" href="${url.context}/res/themes/${theme}/yui/assets/skin.css" />
    </#if>
    <link rel="stylesheet" type="text/css" href="${url.context}/res/css/base.css" />
    <link rel="stylesheet" type="text/css" href="${url.context}/res/css/yui-layout.css" />
    <link rel="stylesheet" type="text/css" href="${url.context}/res/themes/${theme}/presentation.css" />

</head>

<body id="Share" class="yui-skin-${theme} alfresco-share claro">
    <div id="placeholder"></div>
    <script>
        var linkWithoutNewParameter = null;
        var onAppReady = function (event) {
            if (${demo?c}) {
                 docEditor.showMessage("${msg("alfresco.document.onlyoffice.action.edit.msg.demo")}");
            }
            linkWithoutNewParameter = document.location.href.substring(0, document.location.href.lastIndexOf("nodeRef")) + "nodeRef=workspace://SpacesStore/"
                + config.document.key.substring(0, config.document.key.lastIndexOf("_"));
            window.history.pushState({}, {}, linkWithoutNewParameter);
        };

        var onRequestSaveAs = function (event) {
            var title = event.data.title.substring(0, event.data.title.lastIndexOf("."));
            var ext = event.data.title.split(".").pop();
            var url = event.data.url;

            var body = [];
            body.push(
                "<div id='fileName'>",
                "<label for='fileNameInput'>${msg('label.name')}:</label>",
                "<input id='fileNameInput' name='fileNameInput' type='text'  value='" + title + "'>",
                "</div>",
                "<div id='treeFolder'>",
                "<img src='${url.context}/res/components/images/lightbox/loading.gif' id='loadingImg'>",
                "<iframe id='saveAsFrame' frameborder='0' width='100%' height='100%' allow='display-capture' scrolling='no' src='${url.context}/page/context/mine/myfiles' style='display: none;'></iframe>",
                "</div>",
                "<div>",
                "<p id='labelForCurrentPath'>${msg('onlyoffice-editor.save-as.current-location')}</p>",
                "<p id='currentPath'></p>",
                "</div>"
            );

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
                        text: "${msg('button.save')}",
                        handler: function onAction_success() {
                            var saveAsFrame = document.getElementById("saveAsFrame").contentWindow.document;
                            var folderElement = saveAsFrame.getElementsByClassName("crumb documentDroppable documentDroppableHighlights");
                            var folderSpan = folderElement[folderElement.length - 1].getElementsByTagName("span");
                            var saveNode = folderSpan[0].getElementsByTagName("a")[0].href.split("nodeRef=")[1];
                            title = document.getElementById("fileNameInput").value;

                            if (!title) {
                                document.getElementById("fileNameInput").classList.add("invalid");
                                return;
                            }

                            var requestData = {
                                title: title,
                                ext: ext,
                                url: url,
                                saveNode: saveNode
                            };

                            var waitDialog = Alfresco.util.PopupManager.displayMessage({
                                text : "",
                                spanClass : "wait",
                                displayTime : 0
                            });

                            this.destroy();

                            Alfresco.util.Ajax.jsonPost({
                                url: "${saveAsUri}",
                                responseContentType: "application/json",
                                dataObj: requestData,
                                successMessage: "${msg('onlyoffice-editor.save-as.message.success')}",
                                successCallback: {
                                    fn: function () {
                                        waitDialog.destroy();
                                    },
                                    scope: this
                                },
                                failureCallback: {
                                    fn: function exampleFailure(response) {
                                        var errorMessage = "";

                                        if (response.serverResponse.status == 403) {
                                            errorMessage = "${msg('onlyoffice-editor.save-as.message.forbidden')}";
                                        } else {
                                            errorMessage = "${msg('onlyoffice-editor.save-as.message.error-unknown')}";
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
                    },
                    {
                        text : "${msg('button.cancel')}",
                        handler : function onAction_cancel() {
                            this.destroy();
                        },
                        isDefault : true
                    }
                ]
            });

            prompt.setHeader("${msg('onlyoffice-editor.save-as.title')}");
            prompt.setBody(body.join(""));
            prompt.render(document.body);
            prompt.center();
            prompt.show();

            var saveAsFrame = document.getElementById("saveAsFrame");

            saveAsFrame.addEventListener("load", function() {
                var mainFolder = saveAsFrame.contentWindow.document.getElementById("ygtvlabelel1").innerText;
                var currentPathElem = document.getElementById("currentPath");
                currentPathElem.innerText = mainFolder + "${currentPath}";

                saveAsFrame.contentWindow.document.getElementsByClassName("yui-resize-handle")[0].style.display = "none";
                saveAsFrame.contentWindow.document.getElementById("template_x002e_tree_x002e_myfiles_x0023_default-h2").style.display = "none";
                Object.assign(saveAsFrame.contentWindow.document.getElementById("template_x002e_tree_x002e_myfiles").style, {
                    position: "fixed",
                    top: "0",
                    left: "0",
                    height: "100%",
                    width: "100%",
                    overflow: "overlay",
                    backgroundColor: "white",
                    zIndex: "10"
                });

                document.getElementById("loadingImg").remove();
                saveAsFrame.style.display = "block";
            });
        };

        var getCookie = function (name) {
            var value = document.cookie;
            var parts = value.split(name);
            if (parts.length === 2) return parts.pop().split(';').shift().substring(1);
        };

        var onMetaChange = function (event) {
            var favorite = !!event.data.favorite;
                        fetch("${favorite} ", {
                            method: "POST",
                            headers: new Headers({
                                'Content-Type': 'application/json',
                                'Alfresco-CSRFToken': decodeURIComponent(getCookie('Alfresco-CSRFToken'))
                            })
                        })
                        .then(response => {
                            var title = document.title.replace(/^\☆/g, "");
                            document.title = (favorite ? "☆" : "") + title;
                            docEditor.setFavorite(favorite);
                        });
        };

        var onRequestHistoryClose = function () {
            document.location.href = linkWithoutNewParameter;
        };

        var onRequestHistory = function () {
            var xhr = new XMLHttpRequest();
            var historyUri = "${historyUrl}";
            xhr.open("GET", historyUri + "&alf_ticket=" + "${ticket}", false);
            xhr.send();
            if (xhr.status == 200) {
                var hist = JSON.parse(xhr.responseText);
                docEditor.refreshHistory({
                    currentVersion: hist[0].version,
                    history: hist.reverse()
                });
            }
        };

        var onRequestHistoryData = function (event) {
            var xhr = new XMLHttpRequest();
            var historyUri = "${historyUrl}";
            var version = event.data;
            xhr.open("GET", historyUri + "&version=" + version + "&alf_ticket=" + "${ticket}", false);
            xhr.send();
            if (xhr.status == 200) {
                var response = JSON.parse(xhr.responseText);
                if (response !== null) {
                    docEditor.setHistoryData(response);
                } else {
                    docEditor.setHistoryData([]);
                }
            }
        };

        var config = ${editorConfig};

        config.events = {
            "onAppReady": onAppReady,
            "onMetaChange": onMetaChange,
            "onRequestHistoryClose": onRequestHistoryClose,
            "onRequestHistory": onRequestHistory,
            "onRequestHistoryData": onRequestHistoryData,
            "onRequestSaveAs": onRequestSaveAs
        };

        if (/android|avantgo|playbook|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od|ad)|iris|kindle|lge |maemo|midp|mmp|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\|plucker|pocket|psp|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i
            .test(navigator.userAgent)) {
            config.type='mobile';
        }

        var docEditor = new DocsAPI.DocEditor("placeholder", config);
        if(config.document.info.favorite){
            var title = document.title.replace(/^\☆/g, "");
            document.title = (config.document.info.favorite ? "☆" : "") + title;
        }
    </script>
</body>
</html>

