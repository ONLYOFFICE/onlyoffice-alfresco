package com.parashift.onlyoffice.scripts;

import java.io.IOException;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/*
    Copyright (c) Ascensio System SIA 2023. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.convertertest.get")
public class ConverterTest extends AbstractWebScript {

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        char[] array = {'1','2','3'};

        response.setHeader("Content-Disposition", "attachment; filename=test.txt");
        response.setContentType("text/plain");
        response.setContentEncoding("utf-8");
        response.setHeader("Content-Length", Integer.toString(array.length));

        response.getWriter().write(array);
    }
}

