package com.parashift.onlyoffice.constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/*
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
    http://www.onlyoffice.com
*/

public class Formats {
    public static final List<Format> formats = new ArrayList<Format>() {{
        add(new Format("djvu", Type.WORD, new ArrayList<String>()));
        add(new Format("doc", Type.WORD, Arrays.asList("docm", "docx", "docxf", "dotx", "dotm", "epub", "fb2", "html", "odt", "ott", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("docm", Type.WORD, Arrays.asList("docx", "docxf", "dotx", "dotm", "epub", "fb2", "html", "odt", "ott", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("docx", Type.WORD, true, Arrays.asList("docm", "dotx", "dotm", "docxf", "epub", "fb2", "html", "odt", "ott", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("docxf", Type.FORM, true, Arrays.asList("docm", "docx", "oform", "dotx", "dotm", "epub", "fb2", "html", "odt", "ott", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("oform", Type.WORD, true, Arrays.asList( "pdf")));
        add(new Format("dot", Type.WORD, Arrays.asList("docm", "docx", "docxf", "dotx", "dotm", "epub", "fb2", "html", "odt", "ott", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("dotm", Type.WORD, Arrays.asList("docm", "docx", "docxf", "dotx", "epub", "fb2", "html", "odt", "ott", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("dotx", Type.WORD, Arrays.asList("docm", "docx", "docxf", "dotm", "epub", "fb2", "html", "odt", "ott", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("epub", Type.WORD, Arrays.asList("docm", "docx", "docxf", "dotx", "dotm", "fb2", "html", "odt", "ott", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("fb2", Type.WORD, Arrays.asList("docm", "docx", "docxf", "dotx", "dotm", "epub", "html", "odt", "ott", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("fodt", Type.WORD, Arrays.asList("docm", "docx", "docxf", "dotx", "dotm", "epub", "fb2", "html", "odt", "ott", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("html", Type.WORD, Arrays.asList("docm", "docx", "docxf", "dotx", "dotm", "epub", "fb2", "odt", "ott", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("mht", Type.WORD, Arrays.asList("docm", "docx", "docxf", "dotx", "dotm", "epub", "fb2", "odt", "ott", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("odt", Type.WORD, Arrays.asList("docm", "docx", "docxf", "dotx", "dotm", "epub", "fb2", "html", "ott", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("ott", Type.WORD, Arrays.asList("docm", "docx", "docxf", "dotx", "dotm", "epub", "fb2", "html", "odt", "pdf", "pdfa", "rtf", "txt")));
        add(new Format("pdf", Type.WORD, new ArrayList<String>()));
        add(new Format("rtf", Type.WORD, Arrays.asList("docm", "docx", "docxf", "dotx", "dotm", "epub", "fb2", "html", "odt", "ott", "pdf", "pdfa", "txt")));
        add(new Format("txt", Type.WORD, new ArrayList<String>()));
        add(new Format("xps", Type.WORD, Arrays.asList("pdf", "pdfa")));
        add(new Format("oxps", Type.WORD, Arrays.asList("pdf", "pdfa")));
        add(new Format("xml", Type.WORD, Arrays.asList("docm", "docx", "docxf", "dotx", "dotm", "epub", "fb2", "html", "odt", "ott", "pdf", "pdfa", "rtf", "txt")));

        add(new Format("csv", Type.CELL, new ArrayList<String>()));
        add(new Format("fods", Type.CELL, Arrays.asList("csv", "ods", "ots", "pdf", "pdfa", "xlsx", "xltx", "xlsm", "xltm")));
        add(new Format("ods", Type.CELL, Arrays.asList("csv", "ots", "pdf", "pdfa", "xlsx", "xltx", "xlsm", "xltm")));
        add(new Format("ots", Type.CELL, Arrays.asList("csv", "ods", "pdf", "pdfa", "xlsx", "xltx", "xlsm", "xltm")));
        add(new Format("xls", Type.CELL, Arrays.asList("csv", "ods", "ots", "pdf", "pdfa", "xlsx", "xltx", "xlsm", "xltm")));
        add(new Format("xlsm", Type.CELL, Arrays.asList("csv", "ods", "ots", "pdf", "pdfa", "xlsx", "xltx", "xltm")));
        add(new Format("xlsx", Type.CELL, true, Arrays.asList("csv", "ods", "ots", "pdf", "pdfa", "xltx", "xlsm", "xltm")));
        add(new Format("xlt", Type.CELL, Arrays.asList("csv", "ods", "ots", "pdf", "pdfa", "xlsx", "xltx", "xlsm", "xltm")));
        add(new Format("xltm", Type.CELL, Arrays.asList("csv", "ods", "ots", "pdf", "pdfa", "xlsx", "xltx", "xlsm")));
        add(new Format("xltx", Type.CELL, Arrays.asList("csv", "ods", "ots", "pdf", "pdfa", "xlsx", "xlsm", "xltm")));

        add(new Format("fodp", Type.SLIDE, Arrays.asList("odp", "otp", "pdf", "pdfa", "potx", "pptx", "pptm", "potm")));
        add(new Format("odp", Type.SLIDE, Arrays.asList("otp", "pdf", "pdfa", "potx", "pptx", "pptm", "potm")));
        add(new Format("otp", Type.SLIDE, Arrays.asList("odp", "pdf", "pdfa", "potx", "pptx", "pptm", "potm")));
        add(new Format("pot", Type.SLIDE, Arrays.asList("odp", "otp", "pdf", "pdfa", "potx", "pptx", "pptm", "potm")));
        add(new Format("potm", Type.SLIDE, Arrays.asList("odp", "otp", "pdf", "pdfa", "potx", "pptx", "pptm")));
        add(new Format("potx", Type.SLIDE, Arrays.asList("odp", "otp", "pdf", "pdfa", "pptx", "pptm", "potm")));
        add(new Format("pps", Type.SLIDE, Arrays.asList("odp", "otp", "pdf", "pdfa", "potx", "pptx", "pptm", "potm")));
        add(new Format("ppsm", Type.SLIDE, Arrays.asList("odp", "otp", "pdf", "pdfa", "potx", "pptx", "pptm", "potm")));
        add(new Format("ppsx", Type.SLIDE, Arrays.asList("odp", "otp", "pdf", "pdfa", "potx", "pptx", "pptm", "potm")));
        add(new Format("ppt", Type.SLIDE, Arrays.asList("odp", "otp", "pdf", "pdfa", "potx", "pptx", "pptm", "potm")));
        add(new Format("pptm", Type.SLIDE, Arrays.asList("odp", "otp", "pdf", "pdfa", "potx", "pptx", "potm")));
        add(new Format("pptx", Type.SLIDE, true, Arrays.asList( "odp", "otp", "pdf", "pdfa", "potx", "pptm", "potm")));
    }};

    public static List<Format> getSupportedFormats() {
        return formats;
    }

    public static JSONArray getSupportedFormatsAsJson() throws JSONException {
        JSONArray array = new JSONArray();
        for (Format format : formats) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", format.name);
            jsonObject.put("type", format.type);
            jsonObject.put("edit", format.edit);
            jsonObject.put("convertTo", new JSONArray(format.convertTo));
            array.put(jsonObject);
        }
        return array;
    }
}
