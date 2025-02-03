/*
    Copyright (c) Ascensio System SIA 2025. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.sdk.manager.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.manager.request.RequestManager;
import com.onlyoffice.manager.security.JwtManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.manager.url.UrlManager;
import com.onlyoffice.model.common.RequestEntity;
import com.onlyoffice.model.common.RequestedService;
import com.onlyoffice.model.settings.HttpClientSettings;
import com.onlyoffice.model.settings.security.Security;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class RequestManagerImpl implements RequestManager {
    private final UrlManager urlManager;
    private final JwtManager jwtManager;
    private final SettingsManager settingsManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public RequestManagerImpl(final UrlManager urlManager, final JwtManager jwtManager,
                              final SettingsManager settingsManager) {
        this.urlManager = urlManager;
        this.jwtManager = jwtManager;
        this.settingsManager = settingsManager;
    }

    @Override
    public <R> R executeGetRequest(final String url, final Callback<R> callback) throws Exception {
        HttpClientSettings httpClientSettings = HttpClientSettings.builder()
                .ignoreSSLCertificate(settingsManager.isIgnoreSSLCertificate())
                .build();

        return executeGetRequest(url, httpClientSettings, callback);
    }

    @Override
    public <R> R executeGetRequest(final String url, final HttpClientSettings httpClientSettings,
                                   final Callback<R> callback)
            throws Exception {
        HttpGet httpGet = new HttpGet(url);

        return executeRequest(httpGet, httpClientSettings, callback);
    }

    @Override
    public <R> R executePostRequest(final RequestedService requestedService, final RequestEntity requestEntity,
                                    final Callback<R> callback) throws Exception {
        Security security = Security.builder()
                .key(settingsManager.getSecurityKey())
                .header(settingsManager.getSecurityHeader())
                .prefix(settingsManager.getSecurityPrefix())
                .build();

        String url = urlManager.getServiceUrl(requestedService);

        return executePostRequest(url, requestEntity, security, null, callback);
    }

    @Override
    public <R> R executePostRequest(final RequestedService requestedService, final RequestEntity requestEntity,
                                    final HttpClientSettings httpClientSettings, final Callback<R> callback)
            throws Exception {
        Security security = Security.builder()
                .key(settingsManager.getSecurityKey())
                .header(settingsManager.getSecurityHeader())
                .prefix(settingsManager.getSecurityPrefix())
                .build();

        String url = urlManager.getServiceUrl(requestedService);

        return executePostRequest(url, requestEntity, security, httpClientSettings, callback);
    }

    @Override
    public <R> R executePostRequest(final String url, final RequestEntity requestEntity, final Security security,
                                    final HttpClientSettings httpClientSettings, final Callback<R> callback)
            throws Exception {
        HttpPost request = createPostRequest(url, requestEntity, security);

        return executeRequest(request, httpClientSettings, callback);
    }

    private <R> R executeRequest(final HttpUriRequest request, final HttpClientSettings httpClientSettings,
                                 final Callback<R> callback)
            throws Exception {
        try (CloseableHttpClient httpClient = getHttpClient(httpClientSettings)) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity == null) {
                    throw new ClientProtocolException(
                            settingsManager.getDocsIntegrationSdkProperties().getProduct().getName()
                                    + " URL: " + request.getUri() + " did not return content.\n"
                                    + "Request: " + request.toString() + "\n"
                                    + "Response: " + response
                    );
                }

                int statusCode = response.getCode();
                if (statusCode != HttpStatus.SC_OK) {
                    throw new ClientProtocolException(
                            settingsManager.getDocsIntegrationSdkProperties().getProduct().getName()
                                    + " URL: " + request.getUri() + " return unexpected response.\n"
                                    + "Request: " + request.toString() + "\n"
                                    + "Response: " + response.toString()
                    );
                }

                R result = callback.doWork(resEntity);
                EntityUtils.consume(resEntity);

                return result;
            }
        }
    }

    private HttpPost createPostRequest(final String url, final RequestEntity requestEntity,
                                       final Security security) throws JsonProcessingException, URISyntaxException {
        URI uri = URI.create(url);
        if (requestEntity.getKey() != null && !requestEntity.getKey().isEmpty()) {
            uri = new URIBuilder(url).addParameter("shardkey", requestEntity.getKey()).build();
        }

        HttpPost request = new HttpPost(uri);

        if (security.getKey() != null && !security.getKey().isEmpty()) {
            Map<String, RequestEntity> payloadMap = new HashMap<>();
            payloadMap.put("payload", requestEntity);

            String headerToken = jwtManager.createToken(
                    objectMapper.convertValue(payloadMap, Map.class),
                    security.getKey()
            );
            request.setHeader(security.getHeader(), security.getPrefix() + headerToken);

            String bodyToken = jwtManager.createToken(requestEntity, security.getKey());
            requestEntity.setToken(bodyToken);
        }

        StringEntity entity = new StringEntity(
                objectMapper.writeValueAsString(requestEntity),
                ContentType.APPLICATION_JSON
        );

        request.setEntity(entity);
        request.setHeader("Accept", "application/json");

        return request;
    }

    private CloseableHttpClient getHttpClient(final HttpClientSettings httpClientSettings)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        Boolean ignoreSSLCertificate = settingsManager.isIgnoreSSLCertificate();

        Integer connectionTimeout = (int) TimeUnit.SECONDS.toMillis(
                settingsManager.getDocsIntegrationSdkProperties()
                        .getHttpClient()
                        .getConnectionTimeout()
        );

        Integer connectionRequestTimeout = (int) TimeUnit.SECONDS.toMillis(
                settingsManager.getDocsIntegrationSdkProperties()
                        .getHttpClient()
                        .getConnectionRequestTimeout()
        );

        Integer socketTimeout = (int) TimeUnit.SECONDS.toMillis(
                settingsManager.getDocsIntegrationSdkProperties()
                        .getHttpClient()
                        .getSocketTimeout()
        );

        if (httpClientSettings != null) {
            if (httpClientSettings.getConnectionTimeout() != null) {
                connectionTimeout = httpClientSettings.getConnectionTimeout();
            }

            if (httpClientSettings.getConnectionRequestTimeout() != null) {
                connectionRequestTimeout = httpClientSettings.getConnectionRequestTimeout();
            }


            if (httpClientSettings.getSocketTimeout() != null) {
                socketTimeout = httpClientSettings.getSocketTimeout();
            }

            if (httpClientSettings.getIgnoreSSLCertificate() != null) {
                ignoreSSLCertificate = httpClientSettings.getIgnoreSSLCertificate();
            }
        }

        PoolingHttpClientConnectionManagerBuilder poolingHttpClientConnectionManagerBuilder =
                PoolingHttpClientConnectionManagerBuilder.create()
                        .setDefaultConnectionConfig(ConnectionConfig.custom()
                                .setConnectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                                .setSocketTimeout(socketTimeout, TimeUnit.MILLISECONDS)
                                .build()
                        );

        if (ignoreSSLCertificate) {
            SSLContextBuilder builder = new SSLContextBuilder();

            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(final X509Certificate[] chain, final String authType)
                        throws CertificateException {
                    return true;
                }
            });

            SSLConnectionSocketFactory sslConnectionSocketFactory =
                    new SSLConnectionSocketFactory(builder.build(), new HostnameVerifier() {
                        @Override
                        public boolean verify(final String hostname, final SSLSession session) {
                            return true;
                        }
                    });

            poolingHttpClientConnectionManagerBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
        }

        return HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(connectionRequestTimeout, TimeUnit.MILLISECONDS)
                        .build()
                )
                .setConnectionManager(poolingHttpClientConnectionManagerBuilder.build())
                .build();
    }
}
