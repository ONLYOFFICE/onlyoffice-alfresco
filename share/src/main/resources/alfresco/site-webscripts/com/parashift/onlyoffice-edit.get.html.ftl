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
</head>

<body>
    <div>
        <div id="placeholder"></div>
        <form id="saveDialog">
            <div id="messageModal">
                <p></p>
            </div>
            <h3>${msg("onlyoffice-editor.save-as.title")}</h3>
            <div>
                <label for="filenameInput">${msg("onlyoffice-editor.save-as.file-name")}</label>
                <input id="filenameInput" name="filenameInput" type="text" required pattern="^\S+$"/>
                <br>
                <input id="newWindowCheckbox" name="newWindow" type="checkbox"/>
                <label for="newWindowCheckbox">${msg("onlyoffice-editor.open-in-new-window-checkbox")}</label>
            </div>
            <div id="treeFolder">
                <iframe id="saveAsFrame"
                        frameborder="0"
                        width="100%"
                        height="100%"
                        allow="display-capture"
                        scrolling="no"
                        src="${share}page/context/mine/myfiles">
                </iframe>
            </div>
            <div>
                <p id="currentPath"></p>
            </div>
            <div id="buttonDiv">
                <button id="saveButton">${msg("onlyoffice-editor.save-as.save-btn")}</button>
                <button id="cancelButton">${msg("onlyoffice-editor.save-as.cancel-btn")}</button>
            </div>
        </form>
        <div id="black-overlay"></div>
    </div>
    <script>
        var onAppReady = function (event) {
            if (${demo?c}) {
                 docEditor.showMessage("${msg("alfresco.document.onlyoffice.action.edit.msg.demo")}");
            }
        };
        document.getElementById("black-overlay").onclick = function (event) {
            event.target.style.display = "none";
            if (document.getElementById("saveDialog").style.display == "block") {
                document.getElementById("saveDialog").style.display = "none";
            }
        };

        var onRequestSaveAs = function (event) {
            var frame = document.getElementById("saveAsFrame");
            document.getElementById("black-overlay").style.display = "block";
            var dialog = document.getElementById("saveDialog");
            dialog.onsubmit = function (event) {
                event.preventDefault();
            };
            dialog.style.display = "block";
            var ext = event.data.title.substring(event.data.title.lastIndexOf(".") + 1);
            var url = event.data.url;
            var title = "";
            var currentPath = "";
            for (var folder of ${currentPath}) {
                    currentPath += " > " + folder;
            }
            var mainFolder = frame.contentWindow.document.getElementById("template_x002e_tree_x002e_myfiles_x0023_default-h2").innerText;
            var currentPathElem = document.getElementById("currentPath");
            currentPathElem.innerText = "${msg("onlyoffice-editor.save-as.current-location")} " + mainFolder + currentPath;
            document.getElementById("filenameInput").value = event.data.title.substring(0, event.data.title.lastIndexOf(" "));
            frame.contentWindow.document.getElementsByClassName("yui-resize-handle yui-resize-handle-r")[0].style.display = "none";
            frame.contentWindow.document.getElementById("template_x002e_tree_x002e_myfiles_x0023_default-h2").style.display = "none";
            var sharePath = frame.contentWindow.document.getElementById("template_x002e_documentlist_v2_x002e_myfiles_x0023_default-breadcrumb");
            Object.assign(frame.contentWindow.document.getElementById("template_x002e_tree_x002e_myfiles").style, {
                position: "fixed",
                top: "0",
                left: "0",
                height: "100%",
                width: "100%",
                overflow: "overlay",
                backgroundColor: "white",
                zIndex: "10"
            });
            var hideFunction = function () {
                dialog.style.display = "none";
                document.getElementById("black-overlay").style.display = "none";
            };
            var showMessage = function (message) {
                var modal = document.getElementById("messageModal");
                modal.children[0].innerText = message;
                modal.style.display = "block";
                setTimeout(function () {
                    modal.style.display = "none";
                    hideFunction();
                }, 2000);
            };
            var postData = function () {
                title = document.getElementById("filenameInput").value;
                if (document.getElementById("filenameInput").validity.valid) {
                    var saveNode = sharePath.children.length != 1 ? sharePath.children[sharePath.children.length - 1].children[1].children[0].href.split("nodeRef=")[1]
                        :  sharePath.children[sharePath.children.length - 1].children[0].children[0].href.split("nodeRef=")[1];
                    var data = {
                        title: title,
                        ext: ext,
                        url: url,
                        saveNode: saveNode
                    };
                    fetch("${saveas}",  {
                        method: "POST",
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify(data)
                    })
                        .then((response) => response.json())
                        .then((data) => {
                            showMessage("${msg("onlyoffice-editor.save-as.success")}");
                            if (document.getElementById("newWindowCheckbox").checked) {
                                window.open("${share}page/context/mine/onlyoffice-edit?nodeRef=" + data.nodeRef);
                            }
                        })
                        .catch((error) => {
                            showMessage("${msg("onlyoffice-editor.save-as.error")}");
                        });
                }
            };
            document.getElementById("cancelButton").onclick = hideFunction;
            document.getElementById("saveButton").onclick = postData;
        };

        var config = ${config};

        config.events = {
            "onAppReady": onAppReady,
            "onRequestSaveAs": onRequestSaveAs
        };

        if (/android|avantgo|playbook|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od|ad)|iris|kindle|lge |maemo|midp|mmp|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\|plucker|pocket|psp|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i
            .test(navigator.userAgent)) {
            config.type='mobile';
        }
        var docEditor = new DocsAPI.DocEditor("placeholder", config);
    </script>
</body>
</html>

