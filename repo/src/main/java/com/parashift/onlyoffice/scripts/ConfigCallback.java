package com.parashift.onlyoffice.scripts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.parashift.onlyoffice.util.*;
import org.alfresco.repo.i18n.MessageService;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/*
    Copyright (c) Ascensio System SIA 2022. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.onlyoffice-config.post")
public class ConfigCallback extends AbstractWebScript {

    @Autowired
    ConfigManager configManager;

    @Autowired
    @Qualifier("global-properties")
    Properties globalProp;

    @Autowired
    ConvertManager converter;

    @Autowired
    UrlManager urlManager;

    @Autowired
    MessageService mesService;

    @Autowired
    RequestManager requestManager;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {

        logger.debug("Received new configuration");
        try {
            JSONObject data = new JSONObject(request.getContent().getContent());

            logger.debug(data.toString(3));

            String docUrl = AppendSlash(data.getString("url").trim());
            String docInnerUrl = AppendSlash(data.getString("innerurl").trim());
            String alfUrl = AppendSlash(data.getString("alfurl").trim());
            String jwtSecret = data.getString("jwtsecret").trim();

            configManager.selectDemo(data.getBoolean("demo"));

            if (configManager.demoActive()) {
                docUrl = configManager.getDemo("url");
            } else {
                configManager.set("url", docUrl);
                configManager.set("innerurl", docInnerUrl);
                configManager.set("jwtsecret", jwtSecret);
            }

            configManager.set("alfurl", alfUrl);
            configManager.set("cert", data.getString("cert"));
            configManager.set("forcesave", data.getString("forcesave"));
            configManager.set("webpreview", data.getString("webpreview"));
            configManager.set("convertOriginal", data.getString("convertOriginal"));

            configManager.set("chat", data.getString("chat"));
            configManager.set("help", data.getString("help"));
            configManager.set("compactHeader", data.getString("compactHeader"));
            configManager.set("toolbarNoTabs", data.getString("toolbarNoTabs"));
            configManager.set("feedback", data.getString("feedback"));
            configManager.set("reviewDisplay", data.getString("reviewDisplay"));

            JSONObject formats = (JSONObject) data.get("formats");
            configManager.set("formatODT", formats.getString("odt"));
            configManager.set("formatODS", formats.getString("ods"));
            configManager.set("formatODP", formats.getString("odp"));
            configManager.set("formatCSV", formats.getString("csv"));
            configManager.set("formatTXT", formats.getString("txt"));
            configManager.set("formatRTF", formats.getString("rtf"));

            String alfrescoProto = (String) globalProp.getOrDefault("alfresco.protocol", "http");

            if (alfrescoProto == "https" && docUrl.toLowerCase().startsWith("http://")) {
                response.getWriter().write("{\"success\": false, \"message\": \"mixedcontent\"}");
                return;
            }

            logger.debug("Checking docserv url");
            if (!CheckDocServUrl()) {
                response.getWriter().write("{\"success\": false, \"message\": \"docservunreachable\"}");
                return;
            }

            try {
                logger.debug("Checking docserv commandservice");
                if (!CheckDocServCommandService()) {
                    response.getWriter().write("{\"success\": false, \"message\": \"docservcommand\"}");
                    return;
                }

                logger.debug("Checking docserv convert");
                if (!CheckDocServConvert()) {
                    response.getWriter().write("{\"success\": false, \"message\": \"docservconvert\"}");
                    return;
                }
            } catch (SecurityException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"jwterror\"}");
                return;
            }

            response.getWriter().write("{\"success\": true}");
        } catch (JSONException ex) {
            String msg = "Unable to deserialize JSON: " + ex.getMessage();
            logger.debug(msg);
            response.getWriter().write("{\"success\": false, \"message\": \"jsonparse\"}");
        }
    }

    private String AppendSlash(String url) {
        if (!url.isEmpty() && !url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    private Boolean CheckDocServUrl() {
        String url = urlManager.getEditorInnerUrl() + "healthcheck";

        logger.debug("Sending GET to Document Server healthcheck");
        return requestManager.executeRequestToDocumentServer(url, new RequestManager.Callback<Boolean>() {
            public Boolean doWork(HttpEntity httpEntity) throws IOException {
                String content = IOUtils.toString(httpEntity.getContent(), "utf-8").trim();
                return content.equalsIgnoreCase("true");
            }
        });
    }

    private Boolean CheckDocServCommandService() throws SecurityException, JsonProcessingException, JSONException {
        JSONObject body = new JSONObject();
        body.put("c", "version");

        logger.debug("Sending POST to Command Service: " + body.toString());
        return requestManager.executeRequestToCommandService(body, new RequestManager.Callback<Boolean>() {
            public Boolean doWork(HttpEntity httpEntity) throws IOException, JSONException {
                String content = IOUtils.toString(httpEntity.getContent(), "utf-8");

                logger.debug("/CommandService content: " + content);

                JSONObject callBackJson = new JSONObject(content);

                if (callBackJson.isNull("error")) {
                    return false;
                }

                Integer errorCode = callBackJson.getInt("error");

                if (errorCode == 6) {
                    throw new SecurityException();
                } else if (errorCode != 0) {
                    return false;
                } else {
                    return true;
                }
            }
        });
    }

    private Boolean CheckDocServConvert() throws SecurityException {
        String key = new SimpleDateFormat("MMddyyyyHHmmss").format(new Date());

        try {
            String newFileUrl = converter.convert(key, "txt", "docx", urlManager.getTestConversionUrl(), mesService.getLocale().toLanguageTag());
            logger.debug("/ConvertService url: " + newFileUrl);

            if (newFileUrl == null || newFileUrl.isEmpty()) {
                return false;
            }
        } catch (Exception e) {
            if (e instanceof SecurityException) {
                throw (SecurityException)e;
            }
            logger.debug("/ConvertService error: " + e.getMessage());
            return false;
        }

        return true;
    }
}

