/*
   Copyright (c) Ascensio System SIA 2024. All rights reserved.
   http://www.onlyoffice.com
*/

package com.onlyoffice.web.model;

import com.onlyoffice.model.common.Format;

import java.util.List;
import java.util.Map;

public class OnlyofficeSettings {
    Map<String, Boolean> editableFormats;
    Boolean convertOriginal;
    List<Format> supportedFormats;

    public OnlyofficeSettings() {
    }

    public OnlyofficeSettings(Map<String, Boolean> editableFormats, Boolean convertOriginal,
                              List<Format> supportedFormats) {
        this.editableFormats = editableFormats;
        this.convertOriginal = convertOriginal;
        this.supportedFormats = supportedFormats;
    }

    public Map<String, Boolean> getEditableFormats() {
        return editableFormats;
    }

    public void setEditableFormats(Map<String, Boolean> editableFormats) {
        this.editableFormats = editableFormats;
    }

    public Boolean getConvertOriginal() {
        return convertOriginal;
    }

    public void setConvertOriginal(Boolean convertOriginal) {
        this.convertOriginal = convertOriginal;
    }

    public List<Format> getSupportedFormats() {
        return supportedFormats;
    }

    public void setSupportedFormats(List<Format> supportedFormats) {
        this.supportedFormats = supportedFormats;
    }
}
