package com.parashift.onlyoffice;


import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/*
    Copyright (c) Ascensio System SIA 2021. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.favourite.post")
public class Favourite  extends AbstractWebScript {

    @Autowired
    FavouritesService favouritesService;

    @Autowired
    PersonService personService;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        if(request.getParameter("nodeRef") != null){
            NodeRef nodeRef = new NodeRef(URLDecoder.decode(request.getParameter("nodeRef"), String.valueOf(StandardCharsets.UTF_8)));
            NodeRef person = personService.getPersonOrNull(AuthenticationUtil.getFullyAuthenticatedUser());
            PersonService.PersonInfo personInfo = null;
            String username = null;
            if (person != null) {
                personInfo = personService.getPerson(person);
                username = personInfo.getUserName();
            }
            if(favouritesService.isFavourite(username, nodeRef)){
                favouritesService.removeFavourite(username, nodeRef);
            } else {
                favouritesService.addFavourite(username, nodeRef);
            }
        }
    }
}
