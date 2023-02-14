package com.parashift.onlyoffice.scripts;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PublicServiceAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/*
    Copyright (c) Ascensio System SIA 2022. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.copy-permissions.get")
public class CopyPermissions extends AbstractWebScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("checkOutCheckInService")
    CheckOutCheckInService cociService;

    @Autowired
    PublicServiceAccessService publicServiceAccessService;

    @Autowired
    PermissionService permissionService;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) {
        String nodeRefString = request.getParameter("nodeRef");

        NodeRef sourceNodeRef = new NodeRef(nodeRefString);
        NodeRef destinationNodeRef = cociService.getWorkingCopy(sourceNodeRef);

        if ((publicServiceAccessService.hasAccess("PermissionService", "getAllSetPermissions", sourceNodeRef) != AccessStatus.ALLOWED) ||
                (publicServiceAccessService.hasAccess("PermissionService", "getInheritParentPermissions", sourceNodeRef) != AccessStatus.ALLOWED)) {
            throw new AccessDeniedException("Access denied. You do not have the appropriate permissions to perform this operation");
        }

        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(sourceNodeRef);
        boolean includeInherited = permissionService.getInheritParentPermissions(sourceNodeRef);

        if ((publicServiceAccessService.hasAccess("PermissionService", "setPermission", destinationNodeRef, "dummyAuth", "dummyPermission", true) != AccessStatus.ALLOWED) ||
                (publicServiceAccessService.hasAccess("PermissionService", "setInheritParentPermissions", destinationNodeRef, includeInherited) != AccessStatus.ALLOWED)) {
            throw new AccessDeniedException("Access denied. You do not have the appropriate permissions to perform this operation");
        }

        permissionService.deletePermissions(destinationNodeRef);

        for (AccessPermission permission : permissions) {
            if (permission.isSetDirectly()) {
                permissionService.setPermission(
                        destinationNodeRef,
                        permission.getAuthority(),
                        permission.getPermission(),
                        permission.getAccessStatus().equals(AccessStatus.ALLOWED)
                );
            }
        }

        permissionService.setInheritParentPermissions(destinationNodeRef, includeInherited);

        response.setStatus(200);
    }
}
