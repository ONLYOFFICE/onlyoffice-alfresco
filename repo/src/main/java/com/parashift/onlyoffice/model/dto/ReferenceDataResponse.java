/*
    Copyright (c) Ascensio System SIA 2026. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.model.dto;

import com.onlyoffice.model.documenteditor.config.document.ReferenceData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ReferenceDataResponse {
    private String fileType;
    private String path;
    private String key;
    private String url;
    private String link;
    private ReferenceData referenceData;
    private String token;
}

