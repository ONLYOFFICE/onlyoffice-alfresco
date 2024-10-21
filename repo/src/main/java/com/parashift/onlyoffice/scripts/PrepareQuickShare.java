package com.parashift.onlyoffice.scripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.model.documenteditor.Config;
import com.onlyoffice.model.documenteditor.config.document.DocumentType;
import com.onlyoffice.model.documenteditor.config.document.Type;
import com.onlyoffice.model.documenteditor.config.editorconfig.Mode;
import com.onlyoffice.service.documenteditor.config.ConfigService;
import com.parashift.onlyoffice.sdk.manager.url.UrlManager;
import org.alfresco.model.QuickShareModel;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

 /*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

@Component(value = "webscript.onlyoffice.prepareQuickShare.get")
public class PrepareQuickShare extends AbstractWebScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NodeService nodeService;

    @Autowired
    private MimetypeService mimetypeService;

    @Autowired
    private QuickShareService quickShareService;

    @Autowired
    private UrlManager urlManager;

    @Autowired
    private ConfigService configService;

    @Autowired
    private DocumentManager documentManager;

    @Autowired
    private SettingsManager settingsManager;

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

                            String fileName = documentManager.getDocumentName(nodeRef.toString());
                            String fileExtension = documentManager.getExtension(fileName);
                            DocumentType documentType = documentManager.getDocumentType(fileName);

                            if (documentType == null) {
                                responseJson.put("error", "File type is not supported");
                                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                                response.getWriter().write(responseJson.toString());
                                return null;
                            }

                            if (settingsManager.getSettingBoolean("webpreview", false)) {
                                responseJson.put("previewEnabled", true);
                            } else {
                                responseJson.put("previewEnabled", false);
                                response.getWriter().write(responseJson.toString());
                                return null;
                            }

                            Config config = configService.createConfig(nodeRef.toString(), Mode.VIEW, Type.EMBEDDED);

                            config.getEditorConfig().getEmbedded().setSaveUrl(
                                    urlManager.getEmbeddedSaveUrl(nodeRef.toString(), sharedId)
                            );

                            config.getEditorConfig().getCustomization().setGoback(null);

                            ObjectMapper mapper = new ObjectMapper();

                            responseJson.put("editorConfig", new JSONObject(mapper.writeValueAsString(config)));
                            responseJson.put("onlyofficeUrl", urlManager.getDocumentServerUrl() + "/");
                            responseJson.put("mime", mimetypeService.getMimetype(fileExtension));

                            logger.debug("Sending JSON prepare object");
                            logger.debug(responseJson.toString());

                            response.getWriter().write(responseJson.toString());

                        } catch (JSONException ex) {
                            throw new WebScriptException("Unable to serialize JSON: " + ex.getMessage());
                        } catch (Exception ex) {
                            throw new WebScriptException("Unable to create JWT token: " + ex.getMessage());
                        }
                        return null;
                    }
                }, tenantDomain);

            } catch (InvalidSharedIdException ex) {
                logger.error("Unable to find: " + sharedId);
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "Unable to find: " + sharedId);
            } catch (InvalidNodeRefException inre) {
                logger.error("Unable to find: " + sharedId + " [" + inre.getNodeRef() + "]");
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "Unable to find: " + sharedId);
            }
        }
    }
}

