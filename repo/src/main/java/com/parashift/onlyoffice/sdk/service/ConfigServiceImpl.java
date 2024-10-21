package com.parashift.onlyoffice.sdk.service;

import com.onlyoffice.manager.document.DocumentManager;
import com.onlyoffice.manager.security.JwtManager;
import com.onlyoffice.manager.settings.SettingsManager;
import com.onlyoffice.model.common.User;
import com.onlyoffice.model.documenteditor.config.document.DocumentType;
import com.onlyoffice.model.documenteditor.config.document.Info;
import com.onlyoffice.model.documenteditor.config.document.Permissions;
import com.onlyoffice.model.documenteditor.config.editorconfig.Embedded;
import com.onlyoffice.model.documenteditor.config.editorconfig.Template;
import com.onlyoffice.service.documenteditor.config.DefaultConfigService;
import com.parashift.onlyoffice.sdk.manager.url.UrlManager;
import com.parashift.onlyoffice.util.Util;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
    Copyright (c) Ascensio System SIA 2024. All rights reserved.
    http://www.onlyoffice.com
*/

public class ConfigServiceImpl extends DefaultConfigService {
    @Autowired
    private PersonService personService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private FavouritesService favouritesService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private Util util;
    @Autowired
    private UrlManager urlManager;

    public ConfigServiceImpl(final DocumentManager documentManager, final UrlManager urlManager,
                             final JwtManager jwtManager, final SettingsManager settingsManager) {
        super(documentManager, urlManager, jwtManager, settingsManager);
    }

    @Override
    public Info getInfo(final String fileId) {
        NodeRef nodeRef = new NodeRef(fileId);
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();

        if (userName != null) {
            try {
                return Info.builder()
                        .favorite(favouritesService.isFavourite(userName, nodeRef))
                        .build();
            } catch (Exception e) { }
        }

        return null;
    }

    @Override
    public Permissions getPermissions(final String fileId) {
        NodeRef nodeRef = new NodeRef(fileId);
        String fileName = super.getDocumentManager().getDocumentName(fileId);

        Boolean editPermission = permissionService.hasPermission(nodeRef, PermissionService.WRITE)
                == AccessStatus.ALLOWED;
        Boolean isEditable = super.getDocumentManager().isEditable(fileName);
        Boolean isFillable =  super.getDocumentManager().isFillable(fileName);

        return Permissions.builder()
                .edit(editPermission && isEditable)
                .fillForms(editPermission && isFillable)
                .build();
    }

    @Override
    public User getUser() {
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();

        NodeRef person = personService.getPersonOrNull(userName);
        PersonService.PersonInfo personInfo = null;
        if (person != null) {
            personInfo = personService.getPerson(person);
        }

        User user = User.builder()
                .id(userName)
                .build();

        if (personInfo == null) {
            user.setName(userName);
        } else {
            user.setName(personInfo.getFirstName() + " " + personInfo.getLastName());
        }

        return user;
    }

    @Override
    public List<Template> getTemplates(final String fileId) {
        //Todo: check if user have access create new document in current folder
        List<Template> templates = new ArrayList<>();
        NodeRef templatesNodeRef = util.getNodeByPath("/app:company_home/app:dictionary/app:node_templates");
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(templatesNodeRef);
        String title = super.getDocumentManager().getDocumentName(fileId);
        DocumentType docType = super.getDocumentManager().getDocumentType(title);
        List<String> templateExtList = Arrays.asList("docx", "pptx", "xlsx");
        for (ChildAssociationRef assoc : assocs) {
            String templateTitle = super.getDocumentManager().getDocumentName(assoc.getChildRef().toString());
            String templateExt = super.getDocumentManager().getExtension(templateTitle);
            DocumentType templateType = super.getDocumentManager().getDocumentType(templateTitle);
            if (docType.equals(templateType) && templateExtList.contains(templateExt)) {
                String image = urlManager.getShareUrl() + "res/components/images/filetypes/"
                        + docType.name().toLowerCase() + ".svg";
                String url = urlManager.getCreateUrl(fileId) + "&templateNodeRef=" + assoc.getChildRef();

                Template template = Template.builder()
                                .image(image)
                                .title(templateTitle)
                                .url(url)
                                .build();

                templates.add(template);
            }
        }
        return templates;
    }

    @Override
    public Embedded getEmbedded(final String fileId) {
        return Embedded.builder()
                .saveUrl(urlManager.getEmbeddedSaveUrl(fileId))
                .build();
    }

}
