<!--
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
    http://www.onlyoffice.com
-->

<#if onlyofficeUrl??>
    <@markup id="onlyoffice-preview" target="js" action="after" scope="global">
        <script id="scriptApi" type="text/javascript" src="${onlyofficeUrl}web-apps/apps/api/documents/api.js"></script>
        <script>
            var docEditor;
            var editorConfig = ${editorConfig};

            var connectEditor = function () {
                docEditor = new DocsAPI.DocEditor("embeddedView", editorConfig);
            }

            if (window.addEventListener) {
                window.addEventListener("load", connectEditor);
            } else if (window.attachEvent) {
                window.attachEvent("load", connectEditor);
            }
        </script>
    </@markup>
</#if>