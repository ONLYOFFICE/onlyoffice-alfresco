package com.parashift.onlyoffice.util;

import com.parashift.onlyoffice.constants.Format;
import com.parashift.onlyoffice.constants.Formats;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.repository.*;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ContentType;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/*
    Copyright (c) Ascensio System SIA 2022. All rights reserved.
    http://www.onlyoffice.com
*/
@Service
public class ConvertManager {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ConfigManager configManager;

    @Autowired
    JwtManager jwtManager;

    @Autowired
    Util util;

    @Autowired
    UrlManager urlManager;

    @Autowired
    MessageService mesService;

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

    public void transform(NodeRef sourceNodeRef, String srcType, String outType, ContentWriter writer) throws Exception {
        String key = util.getKey(sourceNodeRef) + "." + srcType;
        logger.info("Received conversion request from " + srcType + " to " + outType);

        try {
            String url = convert(key, srcType, outType, urlManager.getContentUrl(sourceNodeRef), mesService.getLocale().toLanguageTag());
            saveFromUrl(url, writer);
        } catch (Exception ex) {
            logger.info("Conversion failed: " + ex.getMessage());
            throw ex;
        }
    }

    public String convert(String key, String srcType, String outType, String url, String region) throws SecurityException, Exception {
        try (CloseableHttpClient httpClient = GetClient()) {
            JSONObject body = new JSONObject();
            body.put("async", false);
            body.put("embeddedfonts", true);
            body.put("filetype", srcType);
            body.put("outputtype", outType);
            body.put("key", key);
            body.put("url", url);
            body.put("region", region);

            StringEntity requestEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
            HttpPost request = new HttpPost(urlManager.getEditorInnerUrl() + "ConvertService.ashx");
            request.setEntity(requestEntity);
            request.setHeader("Accept", "application/json");

            if (jwtManager.jwtEnabled()) {
                String token = jwtManager.createToken(body);
                JSONObject payloadBody = new JSONObject();
                payloadBody.put("payload", body);
                String headerToken = jwtManager.createToken(body);
                body.put("token", token);
                request.setHeader(jwtManager.getJwtHeader(), "Bearer " + headerToken);
            }

            logger.debug("Sending POST to Docserver: " + body.toString());
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                int status = response.getStatusLine().getStatusCode();

                if(status != HttpStatus.SC_OK) {
                    throw new HttpException("Docserver returned code " + status);
                } else {
                    String content = IOUtils.toString(response.getEntity().getContent(), "utf-8");

                    logger.debug("Docserver returned: " + content);
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
            }
        }
    }

    private void saveFromUrl(String fileUrl, ContentWriter writer) throws Exception {
        try (CloseableHttpClient httpClient = GetClient()) {
            logger.debug("Getting file from " + fileUrl);
            HttpGet request = new HttpGet(fileUrl);

            try(CloseableHttpResponse response = httpClient.execute(request)) {
                int status = response.getStatusLine().getStatusCode();

                if(status != HttpStatus.SC_OK) {
                    throw new HttpException("Server returned " + status);
                } else {
                    writer.putContent(response.getEntity().getContent());
                }
            }
        }
    }

    private CloseableHttpClient GetClient() throws Exception {
        CloseableHttpClient httpClient;

        String cert = (String) configManager.getOrDefault("cert", "no");
        if (cert.equals("true")) {
            logger.debug("Ignoring SSL");
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } else {
            httpClient = HttpClients.createDefault();
        }

        return httpClient;
    }
}