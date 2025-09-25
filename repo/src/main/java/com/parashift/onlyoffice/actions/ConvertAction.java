/*
    Copyright (c) Ascensio System SIA 2025. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.actions;

import com.onlyoffice.client.DocumentServerClient;
import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.model.convertservice.ConvertRequest;
import com.onlyoffice.model.convertservice.ConvertResponse;
import com.onlyoffice.service.convert.ConvertService;
import com.parashift.onlyoffice.util.Util;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConvertAction extends ActionExecuterAbstractBase {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private MimetypeService mimetypeService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private Util util;

    @Autowired
    private CheckOutCheckInService checkOutCheckInService;

    @Autowired
    private DocumentManager documentManager;

    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private ConvertService convertService;

    @Autowired
    private MessageService mesService;

    @Autowired
    private DocumentServerClient documentServerClient;

    @Override
    protected void executeImpl(final Action action, final NodeRef actionedUponNodeRef) {
        if (nodeService.exists(actionedUponNodeRef)) {
            if (permissionService.hasPermission(actionedUponNodeRef, PermissionService.READ) == AccessStatus.ALLOWED) {
                if (!checkOutCheckInService.isCheckedOut(actionedUponNodeRef)
                        && !checkOutCheckInService.isWorkingCopy(actionedUponNodeRef)) {
                    String fileName = documentManager.getDocumentName(actionedUponNodeRef.toString());

                    String title = documentManager.getBaseName(fileName);
                    String srcExt = documentManager.getExtension(fileName);

                    String targetExt = documentManager.getDefaultConvertExtension(fileName);
                    if (targetExt == null) {
                        logger.debug("Files of " + srcExt + " format cannot be converted");
                        return;
                    }

                    ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(actionedUponNodeRef);
                    if (parentAssoc == null || parentAssoc.getParentRef() == null) {
                        logger.debug("Couldn't find parent folder");
                        return;
                    }

                    NodeRef nodeFolder = parentAssoc.getParentRef();
                    String newName = util.getCorrectName(nodeFolder, title, targetExt);

                    NodeRef writeNode = null;
                    Boolean deleteNode = false;
                    Boolean checkoutNode = false;

                    if (settingsManager.getSettingBoolean("convertOriginal", false)
                            && !targetExt.equals("pdf")) {
                        logger.debug("Updating node");
                        if (permissionService.hasPermission(actionedUponNodeRef, PermissionService.WRITE)
                                == AccessStatus.ALLOWED) {
                            util.ensureVersioningEnabled(actionedUponNodeRef);
                            checkoutNode = true;
                            writeNode = checkOutCheckInService.checkout(actionedUponNodeRef);
                            nodeService.setProperty(writeNode, ContentModel.PROP_NAME, newName);
                        } else {
                            throw new SecurityException("User don't have the permissions to update this node");
                        }
                    } else {
                        logger.debug("Creating new node");
                        if (permissionService.hasPermission(nodeFolder, PermissionService.CREATE_CHILDREN)
                                == AccessStatus.ALLOWED) {
                            Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                            props.put(ContentModel.PROP_NAME, newName);
                            deleteNode = true;
                            writeNode = this.nodeService.createNode(
                                    nodeFolder,
                                    ContentModel.ASSOC_CONTAINS,
                                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newName),
                                    ContentModel.TYPE_CONTENT,
                                    props
                            ).getChildRef();

                            util.ensureVersioningEnabled(writeNode);
                        } else {
                            throw new SecurityException("User don't have the permissions to create child node");
                        }
                    }

                    ContentWriter writer = null;

                    try {
                        logger.debug("Invoking .transform()");
                        writer = this.contentService.getWriter(writeNode, ContentModel.PROP_CONTENT, true);
                        writer.setMimetype(mimetypeService.getMimetype(targetExt));

                        ConvertRequest convertRequest = ConvertRequest.builder()
                                .region(mesService.getLocale().toLanguageTag())
                                .build();

                        ConvertResponse convertResponse = convertService.processConvert(convertRequest,
                                actionedUponNodeRef.toString());

                        if (convertResponse.getError() != null
                                && convertResponse.getError().equals(ConvertResponse.Error.TOKEN)) {
                            throw new SecurityException();
                        }

                        if (convertResponse.getEndConvert() == null || !convertResponse.getEndConvert()
                                || convertResponse.getFileUrl() == null || convertResponse.getFileUrl().isEmpty()) {
                            throw new Exception("'endConvert' is false or 'fileUrl' is empty");
                        }

                        documentServerClient.getFile(convertResponse.getFileUrl(), writer.getContentOutputStream());

                        if (checkoutNode) {
                            logger.debug("Checking in node");
                            checkOutCheckInService.checkin(writeNode, null);
                        }
                    } catch (Exception ex) {
                        if (!writer.isClosed()) {
                            try {
                                writer.getContentOutputStream().close();
                            } catch (Exception e) {
                                logger.error("Error close stream", e);
                            }
                        }

                        if (deleteNode && nodeService.exists(writeNode)) {
                            logger.debug("Deleting created node");
                            nodeService.deleteNode(writeNode);
                        }

                        throw new AlfrescoRuntimeException("Conversion failed", ex);
                    } finally {
                        if (nodeService.exists(writeNode) && checkOutCheckInService.isCheckedOut(writeNode)) {
                            logger.debug("Finally: cancelCheckout()");
                            checkOutCheckInService.cancelCheckout(writeNode);
                        }
                    }
                } else {
                    throw new AlfrescoRuntimeException("The node is already locked");
                }
            } else {
                throw new SecurityException("User don't have the permissions to read this node");
            }
        }
    }

    @Override
    protected void addParameterDefinitions(final List<ParameterDefinition> paramList) {
    }
}

