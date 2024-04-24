<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">

/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

function main() {
    model.tools = Admin.getConsoleTools("onlyoffice-config");
    model.metadata = Admin.getServerMetaData();
}

main();