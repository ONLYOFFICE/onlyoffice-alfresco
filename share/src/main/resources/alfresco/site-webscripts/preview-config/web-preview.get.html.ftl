<!--
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
-->

<#if documentServerApiUrl??>
    <@markup id="onlyoffice-preview" target="js" action="after" scope="global">
        <script id="scriptApi" type="text/javascript" src="${documentServerApiUrl}"></script>
        <script>
            var docEditor;
            var editorConfig = ${editorConfig};

            var connectEditor = function () {
                docEditor = new DocsAPI.DocEditor("embeddedView", editorConfig);
            }

            YAHOO.Bubbling.on("webPreviewSetupComplete", function() {
                setTimeout(function() {
                    connectEditor();
                }, 100);
            });

        </script>
    </@markup>
</#if>