/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.externalservice;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.constants.TOKEN_TYPES;
import com.biqasoft.entity.system.ExternalServiceToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.biqasoft.entity.constants.SystemRoles.*;

@RestController
@Api(value = "External common")
@Secured(value = {EXTERNAL_SERVICES_ROOT, ALLOW_ALL_DOMAIN_BASED, ROLE_ADMIN})
@RequestMapping(value = "/v1/token")
public class ExternalServiceTokenController {

    private final ExternalServiceTokenRepository externalServiceTokenRepository;

    public ExternalServiceTokenController(ExternalServiceTokenRepository externalServiceTokenRepository) {
        this.externalServiceTokenRepository = externalServiceTokenRepository;
    }

    @Secured(value = {EXTERNAL_SERVICES_GET_ALL_ACCOUNTS, ALLOW_ALL_DOMAIN_BASED, ROLE_ADMIN})
    @ApiOperation(value = "get all external accounts")
    @GetMapping(value = "accounts")
    public  List<ExternalServiceToken> getAllExternalAccounts() {
        return externalServiceTokenRepository.findAll();
    }

    @Secured(value = {EXTERNAL_SERVICES_GET_ALL_ACCOUNTS, ALLOW_ALL_DOMAIN_BASED, ROLE_ADMIN})
    @ApiOperation(value = "get all yandex direct accounts")
    @GetMapping(value = "accounts/yandex/direct")
    public  List<ExternalServiceToken> getAllYandexDirectAccounts() {
        return externalServiceTokenRepository.findExternalServiceTokensByType(TOKEN_TYPES.YANDEX_DIRECT);
    }

    @Secured(value = {EXTERNAL_SERVICES_GET_ALL_ACCOUNTS, ALLOW_ALL_DOMAIN_BASED, ROLE_ADMIN})
    @ApiOperation(value = "get external account by ID")
    @GetMapping(value = "{id}")
    public  ExternalServiceToken findExternalServiceTokenById(@PathVariable("id") String id) {
        return externalServiceTokenRepository.findExternalServiceTokenById(id);
    }

    @Secured(value = {SystemRoles.EXTERNAL_SERVICES_EDIT_ACCOUNTS, ALLOW_ALL_DOMAIN_BASED, ROLE_ADMIN})
    @ApiOperation(value = "update external account")
    @PutMapping
    public  ExternalServiceToken updateExternalServiceToken(@RequestBody ExternalServiceToken token) {
        token = externalServiceTokenRepository.updateExternalServiceTokenForUser(token);
        return token;
    }

    @Secured(value = {EXTERNAL_SERVICES_DELETE_ACCOUNTS, ALLOW_ALL_DOMAIN_BASED, ROLE_ADMIN})
    @ApiOperation(value = "delete external account")
    @DeleteMapping(value = "{id}")
    public  void updateExternalServiceToken(HttpServletResponse response, @PathVariable("id") String id) {
        externalServiceTokenRepository.deleteExternalServiceTokenById(id);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Secured(value = {EXTERNAL_SERVICES_ADD_ACCOUNTS, ALLOW_ALL_DOMAIN_BASED, ROLE_ADMIN})
    @ApiOperation(value = "add new external account")
    @PostMapping
    public void addNewToken(@RequestBody ExternalServiceToken token, HttpServletResponse response) {
        externalServiceTokenRepository.addExternalServiceToken(token);
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

}

