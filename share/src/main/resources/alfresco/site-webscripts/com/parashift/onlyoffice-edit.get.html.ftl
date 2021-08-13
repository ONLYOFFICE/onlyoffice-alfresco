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
        <iframe id="managerFrame"
                width="75%"
                height="85%"
                align="top"
                allow="display-capture"
                src="${share}page/context/mine/myfiles">
        </iframe>
        <div id="black-overlay"></div>
    </div>
    <script>
        var onAppReady = function (event) {
            if (${demo?c}) {
                 docEditor.showMessage("${msg("alfresco.document.onlyoffice.action.edit.msg.demo")}");
            }
        };
        document.getElementById("black-overlay").addEventListener("click", function (event) {
            event.target.style.display = "none";
            document.getElementById("managerFrame").style.display = "none";
        });

        var config = ${config};

        var onRequestCompareFile = function() {
            document.getElementById("black-overlay").style.display = "block";
            var managerFrame = document.getElementById("managerFrame");
            managerFrame.style.display = "block";
            managerFrame.contentWindow.document.getElementsByClassName("header-bar")[0].style.display = "none";
            managerFrame.contentWindow.document.getElementsByClassName("sticky-footer")[0].style.display = "none";
            managerFrame.contentWindow.document.getElementById("alf-hd").style.display = "none";
            var listener =  function (event) {
                var targetHref = null;
                var targetType = null;
                if (event.target.tagName == "A") {
                    targetHref = event.target.href;
                    event.target.href = "#";
                    targetType = event.target.innerText.substring(event.target.innerText.lastIndexOf(".") + 1);
                } else if (event.target.parentNode.tagName == "A") {
                    targetHref = event.target.parentNode.href;
                    event.target.parentNode.href = "#";
                    targetType = event.target.title.substring(event.target.title.lastIndexOf(".") + 1);
                }
                if (targetHref != null && targetType != null && targetType == config.document.fileType) {
                    docEditor.setRevisedFile({
                        "fileType": targetType,
                        "url": "${alfresco}s/parashift/onlyoffice/download?" + targetHref.split("?")[1] + "&alf_ticket=${ticket}"
                    });
                    document.getElementById("black-overlay").style.display = "none";
                    managerFrame.contentWindow.document.removeEventListener("click", listener);
                    managerFrame.style.display = "none";
                    managerFrame.src = "${share}page/context/mine/myfiles";
                }
            };
            managerFrame.contentWindow.document.addEventListener("click", listener);
        };

        config.events = {
            "onAppReady": onAppReady,
            "onRequestCompareFile": onRequestCompareFile
        };

        if (/android|avantgo|playbook|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od|ad)|iris|kindle|lge |maemo|midp|mmp|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\|plucker|pocket|psp|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i
            .test(navigator.userAgent)) {
            config.type='mobile';
        }
        var docEditor = new DocsAPI.DocEditor("placeholder", config);
    </script>
</body>
</html>

