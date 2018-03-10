/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.storage.controllers;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.dto.httpresponse.LinkFieldDataResponse;
import com.biqasoft.entity.system.ExternalServiceToken;
import com.biqasoft.gateway.storage.repositories.GoogleDriveStorageRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "External common")
@Secured(value = {SystemRoles.EXTERNAL_SERVICES_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@RequestMapping(value = "v1/token/gdrive")
@ConditionalOnProperty({"google.drive.CLIENT_ID_KEY", "biqa.REQUIRE_ALL"})
public class GoogleDriveController {

    private final GoogleDriveStorageRepository gDriveRepository;
    private final String redirectLink;

    @Autowired
    public GoogleDriveController(GoogleDriveStorageRepository gDriveRepository, @Value("${google.drive.auth.redirect.url}") String redirectLink) {
        this.gDriveRepository = gDriveRepository;
        this.redirectLink = redirectLink;
    }

    @Secured(value = {SystemRoles.EXTERNAL_SERVICES_GET_ALL_ACCOUNTS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "first step to connect new account", notes = "return string ( url ) where user in browser should be redirected")
    @RequestMapping(value = "redirect_link", method = RequestMethod.GET)
    public  LinkFieldDataResponse getAuthLinkToConnectNewAccount() {
        LinkFieldDataResponse linkFieldDataResponse = new LinkFieldDataResponse();
        String authorizeUrl = redirectLink;
        linkFieldDataResponse.setUrl(authorizeUrl);
        return linkFieldDataResponse;
    }

    @Secured(value = {SystemRoles.EXTERNAL_SERVICES_ADD_ACCOUNTS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "resolve in server aces token ")
    @RequestMapping(value = "oauth2/code/", method = RequestMethod.GET)
    public ExternalServiceToken getAccessCode(@RequestParam("code") String code) {
        ExternalServiceToken externalServiceToken = gDriveRepository.obtainCodeToToken(code);
        return externalServiceToken;
    }

}

