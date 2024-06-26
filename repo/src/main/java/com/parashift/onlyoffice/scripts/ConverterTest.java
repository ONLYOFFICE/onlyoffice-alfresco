package com.parashift.onlyoffice.scripts;

import java.io.IOException;

import com.onlyoffice.manager.security.JwtManager;
import com.onlyoffice.manager.settings.SettingsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.convertertest.get")
public class ConverterTest extends AbstractWebScript {

    @Autowired
    SettingsManager settingsManager;

    @Autowired
    JwtManager jwtManager;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        if (settingsManager.isSecurityEnabled() ) {
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

        char[] array = {'1','2','3'};

        response.setHeader("Content-Disposition", "attachment; filename=test.txt");
        response.setContentType("text/plain");
        response.setContentEncoding("utf-8");
        response.setHeader("Content-Length", Integer.toString(array.length));

        response.getWriter().write(array);
    }
}

