package com.parashift.onlyoffice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.IOException;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/*
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
    http://www.onlyoffice.com
*/
@Service
public class RequestManager {

    @Autowired
    ConfigManager configManager;

    @Autowired
    UrlManager urlManager;

    @Autowired
    JwtManager jwtManager;

    public static final int REQUEST_TIMEOUT = 60;
    public <R> R executeRequestToDocumentServer(String url, RequestManager.Callback<R> callback) {
        HttpGet request = new HttpGet(urlManager.replaceDocEditorURLToInternal(url));

        return executeRequest(request, "Document Server", callback);
    }

    public <R> R executeRequestToCommandService(JSONObject body, RequestManager.Callback<R> callback) throws JsonProcessingException, JSONException {
        HttpPost request = new HttpPost(urlManager.getEditorInnerUrl() + "coauthoring/CommandService.ashx");

        return executeRequestPost(request, body, "Command Service", callback);
    }

    public <R> R executeRequestToConversionService(JSONObject body, RequestManager.Callback<R> callback) throws JsonProcessingException, JSONException {
        HttpPost request = new HttpPost(urlManager.getEditorInnerUrl() + "ConvertService.ashx");
        return executeRequestPost(request, body, "Conversion Service", callback);
    }

    private  <R> R executeRequestPost(HttpPost request, JSONObject body, String name, RequestManager.Callback<R> callback) throws JsonProcessingException, JSONException {
        if (jwtManager.jwtEnabled()) {
            String token = jwtManager.createToken(body);

            JSONObject payloadBody = new JSONObject();
            payloadBody.put("payload", body);

            String headerToken = jwtManager.createToken(payloadBody);

            body.put("token", token);
            request.setHeader(jwtManager.getJwtHeader(), "Bearer " + headerToken);
        }

        StringEntity requestEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);

        request.setEntity(requestEntity);
        request.setHeader("Accept", "application/json");
        return executeRequest(request, name, callback);
    }

    private <R> R executeRequest(HttpUriRequest request, String name, RequestManager.Callback<R> callback) {
        try (CloseableHttpClient httpClient = getHttpClient()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                StatusLine statusLine = response.getStatusLine();
                if (statusLine == null) {
                    throw new AlfrescoRuntimeException(name + " returned no status " + request.getURI().toString());
                }

                HttpEntity resEntity = response.getEntity();
                if (resEntity == null) {
                    throw new AlfrescoRuntimeException(name + " did not return an entity " + request.getURI().toString());
                }

                int statusCode = statusLine.getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    throw new AlfrescoRuntimeException(name + " returned a " + statusCode + " status " + getErrorMessage(resEntity) + ' ' + request.getURI().toString());
                }

                R result = callback.doWork(resEntity);
                EntityUtils.consume(resEntity);

                return result;
            } catch (IOException e) {
                throw new AlfrescoRuntimeException(name + " failed to connect or to read the response", e);
            }
        } catch (Exception e) {
            throw new AlfrescoRuntimeException(name + " failed to create an HttpClient", e);
        }
    }

    private CloseableHttpClient getHttpClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        Integer timeout = REQUEST_TIMEOUT * 1000;
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build();

        CloseableHttpClient httpClient;
        if (configManager.getAsBoolean("cert", false) && !configManager.demoActive()) {
            SSLContextBuilder builder = new SSLContextBuilder();

            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });

            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                    builder.build(),
                    new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }
            );

            httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).setDefaultRequestConfig(config).build();
        } else {
            httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        }

        return httpClient;
    }

    private String getErrorMessage(HttpEntity resEntity) throws IOException {
        String message = "";
        String content = EntityUtils.toString(resEntity);
        int i = content.indexOf("\"message\":\"");
        if (i != -1) {
            int j = content.indexOf("\",\"path\":", i);
            if (j != -1) {
                message = content.substring(i+11, j);
            }
        }
        return message;
    }

    public interface Callback<Result> {
        Result doWork(HttpEntity httpEntity) throws Exception;
    }
}
