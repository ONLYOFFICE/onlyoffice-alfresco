/*
    Copyright (c) Ascensio System SIA 2026. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.onlyoffice.model.documenteditor.config.document.ReferenceData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceDataRequest {
    private String link;
    private String path;
    private ReferenceData referenceData;
}
