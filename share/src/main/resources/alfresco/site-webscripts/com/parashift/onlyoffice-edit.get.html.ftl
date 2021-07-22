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
    </div>
    <script>
        var onAppReady = function (event) {
            if (${demo?c}) {
                 docEditor.showMessage("${msg("alfresco.document.onlyoffice.action.edit.msg.demo")}");
            }
        };

        var config = ${config};

        config.events = {
            "onAppReady": onAppReady
        };

        var docEditor = new DocsAPI.DocEditor("placeholder", config);
    </script>
</body>
</html>

