package com.parashift.onlyoffice.constants;

import java.util.List;

/*
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
    http://www.onlyoffice.com
*/

public class Format {
    public String name;
    public Type type;
    public boolean edit;
    public List<String> convertTo;

    public Format(String name, Type type, List<String> convertTo) {
        this.name = name;
        this.type = type;
        this.edit = false;
        this.convertTo = convertTo;
    }

    public Format(String name, Type type, boolean edit, List<String> convertTo) {
        this.name = name;
        this.type = type;
        this.edit = edit;
        this.convertTo = convertTo;
    }

    public String getName() {
        return name;
    }

    public Type getType() { return type; }

    public boolean isEdit() { return edit; }

    public List<String> getConvertTo() {
        return convertTo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) { this.type = type; }

    public void setConvertTo(List<String> convertTo) {
        this.convertTo = convertTo;
    }

    public void setEdit(boolean edit) { this.edit = edit; }
}