package com.parashift.onlyoffice.util;

import com.parashift.onlyoffice.constants.Format;
import com.parashift.onlyoffice.constants.Formats;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.repository.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
    http://www.onlyoffice.com
*/
@Service
public class ConvertManager {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Util util;

    @Autowired
    UrlManager urlManager;

    @Autowired
    MessageService mesService;

    @Autowired
    RequestManager requestManager;

    private static Set<String> ConvertBackList = new HashSet<String>() {{
        add("application/vnd.oasis.opendocument.text");
        add("application/vnd.oasis.opendocument.spreadsheet");
        add("application/vnd.oasis.opendocument.presentation");
        add("text/plain");
        add("text/csv");
        add("application/rtf");
        add("application/x-rtf");
        add("text/richtext");
    }};

    public String getTargetExt(String ext) {
        List<Format> supportedFormats = Formats.getSupportedFormats();

        for (Format format : supportedFormats) {
            if (format.getName().equals(ext)) {
                switch(format.getType()) {
                    case FORM:
                        if (format.getConvertTo().contains("oform")) return "oform";
                        break;
                    case WORD:
                        if (format.getConvertTo().contains("docx")) return "docx";
                        break;
                    case CELL:
                        if (format.getConvertTo().contains("xlsx")) return "xlsx";
                        break;
                    case SLIDE:
                        if (format.getConvertTo().contains("pptx")) return "pptx";
                        break;
                    default:
                        break;
                }
            }
        }

        return null;
    }

    public boolean shouldConvertBack(String mimeType) {
        return ConvertBackList.contains(mimeType);
    }

    public void transform(NodeRef sourceNodeRef, String srcType, String outType, final ContentWriter writer) throws Exception {
        String key = util.getKey(sourceNodeRef) + "." + srcType;
        logger.info("Received conversion request from " + srcType + " to " + outType);

        try {
            String url = convert(key, srcType, outType, urlManager.getContentUrl(sourceNodeRef), mesService.getLocale().toLanguageTag());

            requestManager.executeRequestToDocumentServer(url, new RequestManager.Callback<Void>() {
                public Void doWork(HttpEntity httpEntity) throws IOException {
                    writer.putContent(httpEntity.getContent());
                    return null;
                }
            });
        } catch (Exception ex) {
            logger.info("Conversion failed: " + ex.getMessage());
            throw ex;
        }
    }

    public String convert(String key, String srcType, String outType, String url, String region) throws Exception {
        JSONObject body = new JSONObject();
        body.put("async", false);
        body.put("embeddedfonts", true);
        body.put("filetype", srcType);
        body.put("outputtype", outType);
        body.put("key", key);
        body.put("url", url);
        body.put("region", region);

        logger.debug("Sending POST to Conversion Service: " + body.toString());
        return requestManager.executeRequestToConversionService(body, new RequestManager.Callback<String>() {
            public String doWork(HttpEntity httpEntity) throws Exception {
                String content = IOUtils.toString(httpEntity.getContent(), "utf-8");

                logger.debug("Conversion Service returned: " + content);
                JSONObject callBackJSon = null;
                try{
                    callBackJSon = new JSONObject(content);
                } catch (Exception e) {
                    throw new Exception("Couldn't convert JSON from docserver: " + e.getMessage());
                }

                if (!callBackJSon.isNull("error") && callBackJSon.getInt("error") == -8) throw new SecurityException();

                if (callBackJSon.isNull("endConvert") || !callBackJSon.getBoolean("endConvert") || callBackJSon.isNull("fileUrl")) {
                    throw new Exception("'endConvert' is false or 'fileUrl' is empty");
                }

                return callBackJSon.getString("fileUrl");
            }
        });
    }
}