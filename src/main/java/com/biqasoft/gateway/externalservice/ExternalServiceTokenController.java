/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.externalservice;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.constants.TOKEN_TYPES;
import com.biqasoft.entity.system.ExternalServiceToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Api(value = "External common")
@Secured(value = {SYSTEM_ROLES.EXTERNAL_SERVICES_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@RequestMapping(value = "/v1/token")
public class ExternalServiceTokenController {

    private final ExternalServiceTokenRepository externalServiceTokenRepository;

    @Autowired
    public ExternalServiceTokenController(ExternalServiceTokenRepository externalServiceTokenRepository) {
        this.externalServiceTokenRepository = externalServiceTokenRepository;
    }

    @Secured(value = {SYSTEM_ROLES.EXTERNAL_SERVICES_GET_ALL_ACCOUNTS, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get all external accounts")
    @RequestMapping(value = "accounts", method = RequestMethod.GET)
    public  List<ExternalServiceToken> getAllExternalAccounts(HttpServletResponse response) {
        return externalServiceTokenRepository.findAll();
    }

    @Secured(value = {SYSTEM_ROLES.EXTERNAL_SERVICES_GET_ALL_ACCOUNTS, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get all yandex direct accounts")
    @RequestMapping(value = "accounts/yandex/direct", method = RequestMethod.GET)
    public  List<ExternalServiceToken> getAllYandexDirectAccounts(HttpServletResponse response) {
        return externalServiceTokenRepository.findExternalServiceTokensByType(TOKEN_TYPES.YANDEX_DIRECT);
    }

    @Secured(value = {SYSTEM_ROLES.EXTERNAL_SERVICES_GET_ALL_ACCOUNTS, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get external account by ID")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public  ExternalServiceToken findExternalServiceTokenById(HttpServletResponse response, @PathVariable("id") String id) {
        return externalServiceTokenRepository.findExternalServiceTokenById(id);
    }

    @Secured(value = {SYSTEM_ROLES.EXTERNAL_SERVICES_EDIT_ACCOUNTS, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "update external account")
    @RequestMapping(method = RequestMethod.PUT)
    public  ExternalServiceToken updateExternalServiceToken(@RequestBody ExternalServiceToken token) {
        token = externalServiceTokenRepository.updateExternalServiceTokenForUser(token);
        return token;
    }

    @Secured(value = {SYSTEM_ROLES.EXTERNAL_SERVICES_DELETE_ACCOUNTS, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "delete external account")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public  void updateExternalServiceToken(HttpServletResponse response, @PathVariable("id") String id) {
        externalServiceTokenRepository.deleteExternalServiceTokenById(id);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Secured(value = {SYSTEM_ROLES.EXTERNAL_SERVICES_ADD_ACCOUNTS, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "add new external account")
    @RequestMapping(method = RequestMethod.POST)
    public void addNewToken(@RequestBody ExternalServiceToken token, HttpServletResponse response) {
        externalServiceTokenRepository.addExternalServiceToken(token);
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

}

