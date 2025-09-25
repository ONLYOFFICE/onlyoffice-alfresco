/*
    Copyright (c) Ascensio System SIA 2025. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.sdk.client;

import com.onlyoffice.client.ApacheHttpclientDocumentServerClient;
import com.onlyoffice.client.DocumentServerClientSettings;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.manager.url.UrlManager;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class ApacheHttpclientDocumentServerClientImpl extends ApacheHttpclientDocumentServerClient
        implements DocumentServerClient {
    public ApacheHttpclientDocumentServerClientImpl(final DocumentServerClientSettings documentServerClientSettings) {
        super(documentServerClientSettings);
    }

    public ApacheHttpclientDocumentServerClientImpl(final SettingsManager settingsManager,
                                                    final UrlManager urlManager) {
        super(settingsManager, urlManager);
    }

    @Override
    public int getFileWithoutCloseOutputStream(final String fileUrl, final OutputStream outputStream) {
        URI uri;
        try {
            uri = new URI(fileUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        ClassicHttpRequest request = ClassicRequestBuilder.get(getBaseUrl())
                .setPath(uri.getRawPath() + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : ""))
                .build();

        return executeRequestWithoutCloseOutputStream(request, outputStream);
    }

    protected int executeRequestWithoutCloseOutputStream(final ClassicHttpRequest classicHttpRequest,
                                                         final OutputStream outputStream) {
        prepareHttpClient();

        return executeRequest(this.getHttpClient(), classicHttpRequest, inputStream -> {
            return IOUtils.copy(inputStream, outputStream);
        });
    }

}
