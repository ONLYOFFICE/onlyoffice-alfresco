/*
    Copyright (c) Ascensio System SIA 2025. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.events;

import com.onlyoffice.model.commandservice.CommandRequest;
import com.onlyoffice.model.commandservice.CommandResponse;
import com.onlyoffice.model.commandservice.commandrequest.Command;
import com.onlyoffice.service.command.CommandService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.parashift.onlyoffice.model.OnlyofficeDocsModel.ASPECT_EDITING_IN_ONLYOFFICE_DOCS;
import static com.parashift.onlyoffice.model.OnlyofficeDocsModel.PROP_DOCUMENT_KEY;


public class NodeEventHandler  implements NodeServicePolicies.OnRemoveAspectPolicy {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NodeService nodeService;
    @Autowired
    private CommandService commandService;

    private PolicyComponent policyComponent;

    public void setPolicyComponent(final PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void registerEventHandlers() {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnRemoveAspectPolicy.QNAME,
                ContentModel.ASPECT_LOCKABLE,
                new JavaBehaviour(
                        this,
                        "onRemoveAspect",
                        Behaviour.NotificationFrequency.FIRST_EVENT
                )
        );
    }

    @Override
    public void onRemoveAspect(final NodeRef nodeRef, final QName qName) {
        if (!qName.equals(ContentModel.ASPECT_LOCKABLE)) {
            return;
        }

        if (!nodeService.exists(nodeRef)) {
            return;
        }

        if (!nodeService.hasAspect(nodeRef, ASPECT_EDITING_IN_ONLYOFFICE_DOCS)) {
            return;
        }

        String key = (String) nodeService.getProperty(nodeRef, PROP_DOCUMENT_KEY);

        nodeService.removeAspect(nodeRef, ASPECT_EDITING_IN_ONLYOFFICE_DOCS);

        List<String> users;
        CommandResponse infoResponse;
        try {
            CommandRequest infoRequest = CommandRequest.builder()
                    .c(Command.INFO)
                    .key(key)
                    .build();

            infoResponse = commandService.processCommand(infoRequest, nodeRef.toString());
            users = infoResponse.getUsers();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            CommandRequest dropRequest = CommandRequest.builder()
                    .c(Command.DROP)
                    .key(key)
                    .users(users)
                    .build();

            commandService.processCommand(dropRequest, nodeRef.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
