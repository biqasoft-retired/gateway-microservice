/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.storage.controllers;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.constants.TOKEN_TYPES;
import com.biqasoft.entity.dto.httpresponse.LinkFieldDataResponse;
import com.biqasoft.entity.system.ExternalServiceToken;
import com.biqasoft.gateway.storage.repositories.DropboxStorageRepository;
import com.biqasoft.gateway.externalservice.ExternalServiceTokenRepository;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWebAuth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Api(value = "External common")
@Secured(value = {SYSTEM_ROLES.EXTERNAL_SERVICES_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@RequestMapping(value = "v1//token/dropbox")
@ConditionalOnProperty({"dropbox.app.key", "biqa.REQUIRE_ALL"})
public class DropboxController {

    private final ExternalServiceTokenRepository externalServiceTokenRepository;
    private final DropboxStorageRepository dropboxStorageRepository;

    @Autowired
    public DropboxController(ExternalServiceTokenRepository externalServiceTokenRepository, DropboxStorageRepository dropboxStorageRepository) {
        this.externalServiceTokenRepository = externalServiceTokenRepository;
        this.dropboxStorageRepository = dropboxStorageRepository;
    }

    @Secured(value = {SYSTEM_ROLES.EXTERNAL_SERVICES_GET_ALL_ACCOUNTS, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "first step to connect new account", notes = "return string ( url ) where user in browser should be redirected")
    @RequestMapping(value = "oauth2/request_to_connect_new_account", method = RequestMethod.GET)
    public LinkFieldDataResponse dropboxRequestToConnectNewAccount() {
        DbxWebAuth webAuth = dropboxStorageRepository.getDbxWebAuth();

        LinkFieldDataResponse linkFieldDataResponse = new LinkFieldDataResponse();
        String authorizeUrl = webAuth.start();
        linkFieldDataResponse.setUrl(authorizeUrl);
        return linkFieldDataResponse;
    }

    @Secured(value = {SYSTEM_ROLES.EXTERNAL_SERVICES_ADD_ACCOUNTS, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "resolve in server aces token in yandex direct from code")
    @RequestMapping(value = "oauth2/code/{code}/state/{state}", method = RequestMethod.GET)
    public ExternalServiceToken getAccessCode(@PathVariable("code") String code, @PathVariable("state") String state) {
        DbxWebAuth webAuth = dropboxStorageRepository.getDbxWebAuth();
        ExternalServiceToken externalServiceToken = null;
        boolean updateToken = false;
        DbxAuthFinish authFinish = null;

        Map<String, String[]> stringMap = new HashMap<>();

        String[] stateArray = {dropboxStorageRepository.getDropboxCSRF()};
        stringMap.put("state", stateArray);

        String[] codeArray = {code};
        stringMap.put("code", codeArray);

        try {
            authFinish = webAuth.finish(stringMap);
        } catch (DbxWebAuth.BadRequestException | DbxWebAuth.NotApprovedException | DbxWebAuth.CsrfException | DbxException | DbxWebAuth.BadStateException | DbxWebAuth.ProviderException e) {
            throw new RuntimeException(e.getMessage());
        }

        // check - if we have account with the same ID
        // we will not create new - we will update
        ExternalServiceToken existingWithSameID = externalServiceTokenRepository.findExternalServiceTokenByLoginAndTypeIgnoreExpired(authFinish.userId, TOKEN_TYPES.DROPBOX);

        if (existingWithSameID == null) {
            externalServiceToken = new ExternalServiceToken();
        } else {
            externalServiceToken = existingWithSameID;
            updateToken = true;
        }

        DbxClient client = new DbxClient(dropboxStorageRepository.getConfig(), authFinish.accessToken);

        externalServiceToken.setToken(authFinish.accessToken);
        externalServiceToken.setLogin(authFinish.userId);
        externalServiceToken.setType(TOKEN_TYPES.DROPBOX);

        try {
            externalServiceToken.setName(client.getAccountInfo().displayName);
        } catch (DbxException e) {
            throw new RuntimeException(e.getMessage());
        }

        if (updateToken) {
            externalServiceTokenRepository.updateExternalServiceToken(externalServiceToken);
        } else {
            externalServiceTokenRepository.addExternalServiceToken(externalServiceToken);
        }

        return externalServiceToken;
    }

}

