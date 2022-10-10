package com.parashift.onlyoffice.scripts;

import com.parashift.onlyoffice.util.ConfigManager;
import com.parashift.onlyoffice.util.Util;
import com.parashift.onlyoffice.util.UtilDocConfig;
import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

 /*
    Copyright (c) Ascensio System SIA 2022. All rights reserved.
    http://www.onlyoffice.com
*/

@Component(value = "webscript.onlyoffice.prepareQuickShare.get")
public class PrepareQuickShare extends AbstractWebScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    NodeService nodeService;

    @Autowired
    ConfigManager configManager;

    @Autowired
    MimetypeService mimetypeService;

    @Autowired
    QuickShareService quickShareService;

    @Autowired
    Util util;

    @Autowired
    UtilDocConfig utilDocConfig;

    @Override
    public void execute(final WebScriptRequest request, final WebScriptResponse response) throws IOException {

        final String sharedId = request.getParameter("sharedId");

        if (sharedId != null) {
            try {
                Pair<String, NodeRef> pair = quickShareService.getTenantNodeRefFromSharedId(sharedId);
                final String tenantDomain = pair.getFirst();
                final NodeRef nodeRef = pair.getSecond();

                TenantUtil.runAsSystemTenant(new TenantUtil.TenantRunAsWork<Void>() {
                    public Void doWork() throws Exception {
                        if (!nodeService.getAspects(nodeRef).contains(QuickShareModel.ASPECT_QSHARE)) {
                            throw new InvalidNodeRefException(nodeRef);
                        }

                        response.setContentType("application/json; charset=utf-8");
                        response.setContentEncoding("UTF-8");
                        try {
                            JSONObject responseJson = new JSONObject();

                            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
                            String docTitle = (String) properties.get(ContentModel.PROP_NAME);
                            String docExt = docTitle.substring(docTitle.lastIndexOf(".") + 1).trim().toLowerCase();
                            String documentType = util.getDocType(docExt);

                            if (documentType == null) {
                                responseJson.put("error", "File type is not supported");
                                response.setStatus(500);
                                response.getWriter().write(responseJson.toString(3));
                                return null;
                            }

                            if (((String)configManager.getOrDefault("webpreview", "")).equals("true")) {
                                responseJson.put("previewEnabled", true);
                            } else {
                                responseJson.put("previewEnabled", false);
                                response.getWriter().write(responseJson.toString(3));
                                return null;
                            }

                            JSONObject configJson = utilDocConfig.getConfigJson(nodeRef, sharedId, null, documentType,
                                    docTitle, docExt, true, true);

                            responseJson.put("editorConfig", configJson);
                            responseJson.put("onlyofficeUrl", util.getEditorUrl());
                            responseJson.put("mime", mimetypeService.getMimetype(docExt));

                            logger.debug("Sending JSON prepare object");
                            logger.debug(responseJson.toString(3));

                            response.getWriter().write(responseJson.toString(3));

                        } catch (JSONException ex) {
                            throw new WebScriptException("Unable to serialize JSON: " + ex.getMessage());
                        } catch (Exception ex) {
                            throw new WebScriptException("Unable to create JWT token: " + ex.getMessage());
                        }
                        return null;
                    }
                }, tenantDomain);

            } catch (InvalidSharedIdException ex) {
                logger.error("Unable to find: "+sharedId);
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "Unable to find: "+sharedId);
            } catch (InvalidNodeRefException inre) {
                logger.error("Unable to find: "+sharedId+" ["+inre.getNodeRef()+"]");
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "Unable to find: "+sharedId);
            }
        }
    }
}

