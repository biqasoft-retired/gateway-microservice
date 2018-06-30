/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.useraccount.controllers;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.dto.httpresponse.SampleDataResponse;
import com.biqasoft.users.domain.useraccount.oauth2.OAuth2Application;
import com.biqasoft.microservice.common.MicroserviceOAuth2Applications;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "oauth2")
@RestController
@RequestMapping(value = "/v1/oauth/application")
public class OAuth2ApplicationController {

    private final CurrentUser currentUser;
    private final MicroserviceOAuth2Applications microserviceOAuth2Applications;

    @Autowired
    public OAuth2ApplicationController(CurrentUser currentUser, MicroserviceOAuth2Applications microserviceOAuth2Applications) {
        this.currentUser = currentUser;
        this.microserviceOAuth2Applications = microserviceOAuth2Applications;
    }

    @ApiOperation(value = "find All Oauth Applications In Domain")
    @RequestMapping(value = "list/domain", method = RequestMethod.GET)
    public List<OAuth2Application> findAllOauthApplicationsInDomain() {
        return microserviceOAuth2Applications.findOAuth2ApplicationInDomain();
    }

    @ApiOperation(value = "find All Public Oauth Applications")
    @RequestMapping(value = "list/public", method = RequestMethod.GET)
    public List<OAuth2Application> findAllPublicOauthApplications() {
        return microserviceOAuth2Applications.findOAuth2ApplicationInPublic();
    }

    @ApiOperation(value = "get meta info by oauth application by id")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public OAuth2Application findOauthApplicationById(@PathVariable("id") String id) {
        return microserviceOAuth2Applications.findOAuth2ApplicationById(id);
    }

    @ApiOperation(value = "delete oauth application by id")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void deleteOauthApplicationById(@PathVariable("id") String id){
        microserviceOAuth2Applications.deleteOAuth2ApplicationById(id);
    }

    @Secured(value = {SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "create New Application")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public OAuth2Application createNewApplication(@RequestBody OAuth2Application application) {
        application.setGiveAccessWithoutPrompt(false);
        application.setDomain(currentUser.getDomain().getDomain());
        return microserviceOAuth2Applications.create(application);
    }

    @Secured(value = {SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "update oauth application")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public OAuth2Application updateApplication(@RequestBody OAuth2Application application){
        return microserviceOAuth2Applications.updateApplication(application);
    }

    @ApiOperation(value = "get application secret code by oauth application by id", notes = "Only user who create application can access this operation")
    @RequestMapping(value = "{id}/secret_code", method = RequestMethod.GET)
    public SampleDataResponse getSecretCodeForOAuthApplication(@PathVariable("id") String id){
        return microserviceOAuth2Applications.getSecretCodeForOAuthApplication(id);
    }

}
