/*
    Copyright (c) Ascensio System SIA 2026. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.scripts;

import com.onlyoffice.manager.security.JwtManager;
import com.onlyoffice.manager.settings.SettingsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component(value = "webscript.onlyoffice.convertertest.get")
public class ConverterTest extends AbstractWebScript {

    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private JwtManager jwtManager;

    @Override
    public void execute(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
        if (settingsManager.isSecurityEnabled()) {
            String jwth = settingsManager.getSecurityHeader();
            String header = request.getHeader(jwth);
            String authorizationPrefix = settingsManager.getSecurityPrefix();
            String token = (header != null && header.startsWith(authorizationPrefix))
                    ? header.substring(authorizationPrefix.length()) : header;

            if (token == null || token == "") {
                throw new SecurityException("Expected JWT");
            }

            try {
                String payload = jwtManager.verify(token);
            } catch (Exception e) {
                throw new SecurityException("JWT verification failed!");
            }
        }

        char[] array = {'1', '2', '3'};

        response.setHeader("Content-Disposition", "attachment; filename=test.txt");
        response.setContentType("text/plain");
        response.setContentEncoding("utf-8");
        response.setHeader("Content-Length", Integer.toString(array.length));

        response.getWriter().write(array);
    }
}

