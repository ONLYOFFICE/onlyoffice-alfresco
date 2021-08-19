package com.parashift.onlyoffice;

/*
    Copyright (c) Ascensio System SIA 2021. All rights reserved.
    http://www.onlyoffice.com
*/

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Component(value = "webscript.onlyoffice.save-as.post")
public class SaveAs extends AbstractWebScript {

    @Autowired
    NodeService nodeService;

    @Autowired
    SearchService searchService;

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    Util util;

    @Autowired
    ContentService contentService;

    @Autowired
    MimetypeService mimetypeService;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        JSONParser parser = new JSONParser();
        try {
            JSONObject json = (JSONObject) parser.parse(request.getContent().getContent());
            org.json.JSONObject responseJson = new org.json.JSONObject();
            String url = json.get("url").toString();
            String title = json.get("title").toString();
            String saveNode = json.get("saveNode").toString();
            String ext = json.get("ext").toString();
            NodeRef saveFolderNode = new NodeRef(saveNode);
            String newName = util.getCorrectName(saveFolderNode, title, ext);
            Map<QName, Serializable> props = new HashMap<>(1);
            props.put(ContentModel.PROP_NAME, newName);
            NodeRef nodeRef = this.nodeService.createNode(saveFolderNode, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newName), ContentModel.TYPE_CONTENT, props)
                    .getChildRef();

            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(mimetypeService.getMimetype(ext));
            URL urlContent = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlContent.openConnection();
            InputStream in = connection.getInputStream();
            writer.putContent(in);
            util.ensureVersioningEnabled(nodeRef);

            responseJson.put("nodeRef", nodeRef.toString());
            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");
            response.getWriter().write(responseJson.toString(3));
        } catch (JSONException e) {
            throw new WebScriptException("Unable to serialize JSON: " + e.getMessage());
        } catch (ParseException e) {
            throw new WebScriptException("Unable to parse JSON: " + e.getMessage());
        }
    }
}
