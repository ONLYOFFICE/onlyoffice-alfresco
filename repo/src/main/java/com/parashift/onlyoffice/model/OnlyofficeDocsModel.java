/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.model;

import org.alfresco.service.namespace.QName;


public interface OnlyofficeDocsModel {
    String ORG_ONLYOFFICE_DOCS_MODEL_1_0_URI = "http://www.alfresco.org/model/onlyoffice-docs/1.0";
    QName ASPECT_EDITING_IN_ONLYOFFICE_DOCS =
            QName.createQName(ORG_ONLYOFFICE_DOCS_MODEL_1_0_URI, "editingInOnlyofficeDocs");
    QName PROP_DOCUMENT_KEY =
            QName.createQName(ORG_ONLYOFFICE_DOCS_MODEL_1_0_URI, "documentKey");

    QName FORCESAVE_ASPECT = QName.createQName("onlyoffice:forcesave");
}
