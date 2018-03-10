/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.useraccount.controllers;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.gateway.useraccount.dto.OAuthNewTokenResponse;
import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.dto.useraccount.UserNameWithPassword;
import com.biqasoft.entity.core.useraccount.PersonalSettings;
import com.biqasoft.entity.core.useraccount.UserAccount;
import com.biqasoft.entity.core.useraccount.oauth2.OAuth2Application;
import com.biqasoft.gateway.useraccount.dto.NewCredentialsRequestDTO;
import com.biqasoft.gateway.useraccount.dto.OAuthTokenModificationRequestDTO;
import com.biqasoft.microservice.common.MicroserviceOAuth2Applications;
import com.biqasoft.microservice.common.MicroserviceOAuth2User;
import com.biqasoft.microservice.common.MicroserviceUsersRepository;
import com.biqasoft.microservice.common.dto.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;

@Api(value = "Current User Account")
@RestController
@RequestMapping(value = "/v1/myaccount")
public class MyUserAccountController {

    private final MicroserviceUsersRepository microserviceUsersRepository;
    private final CurrentUser currentUser;
    private final MicroserviceOAuth2User microserviceOAuth2User;
    private final MicroserviceOAuth2Applications microserviceOAuth2Applications;

    @Autowired
    public MyUserAccountController(CurrentUser currentUser,
                                   MicroserviceUsersRepository microserviceUsersRepository, MicroserviceOAuth2User microserviceOAuth2User,
                                   MicroserviceOAuth2Applications microserviceOAuth2Applications) {
        this.currentUser = currentUser;
        this.microserviceUsersRepository = microserviceUsersRepository;
        this.microserviceOAuth2User = microserviceOAuth2User;
        this.microserviceOAuth2Applications = microserviceOAuth2Applications;
    }

    @ApiOperation(value = "get current user account info")
    @RequestMapping(method = RequestMethod.GET)
    public UserAccount getMyUserAccount() {
        return currentUser.getCurrentUser();
    }

    @ApiOperation(value = "set online current user")
    @RequestMapping(value = "set_online", method = RequestMethod.GET)
    public void setOnline() {
        microserviceUsersRepository.setCurrentUserOnline();
    }

    @ApiOperation(value = "request secret code(may be rendered as QR code in UI) but do not enable auth")
    @RequestMapping(value = "2step/request_secret_code", method = RequestMethod.POST)
    public SecondFactorResponse twoStepAuthrequestSecretCode() {
      return  microserviceUsersRepository.tryToAdd2StepAuth("");
    }

    @ApiOperation(value = "enable two step auth for current user")
    @RequestMapping(value = "2step/enable", method = RequestMethod.POST)
    public void enableTwoStepAuth(@RequestBody TwoStepModifyRequest twoStepModifyRequest) {
        microserviceUsersRepository.modifyTwoStepAuth(true, twoStepModifyRequest.getCode());
    }
    @ApiOperation(value = "disable two step auth for current user")
    @RequestMapping(value = "2step/disable", method = RequestMethod.POST)
    public void disableTwoStepAuth() {
        microserviceUsersRepository.modifyTwoStepAuth(false, null);
    }

    @ApiOperation(value = "set personal settings of current user")
    @RequestMapping(value = "personal_settings", method = RequestMethod.PUT)
    public PersonalSettings updatePersonalSettingsMyUserAccount(@RequestBody PersonalSettings personalSettings) {
        microserviceUsersRepository.setCurrentUserPersonalSettings(personalSettings);
        return personalSettings;
    }

    @ApiOperation(value = "authenticate current user and get some additional credentials with which user can be authenticated",
            notes = "This method generate NEW ADDITIONAL username and password using system oauth application")
    @RequestMapping(value = "oauth/create_new_credentials", method = RequestMethod.POST)
    public UserNameWithPassword createAdditionalUsernameAndPasswordCredentialsOauth(@RequestBody NewCredentialsRequestDTO requestDto) {
        OAuth2MicroserviceNewCredentialsRequest request = new OAuth2MicroserviceNewCredentialsRequest();
        request.setUserAccount(currentUser.getCurrentUser());
        request.setRolesRequested(requestDto.getRoles());
        request.setExpireDate(requestDto.getExpire());

        return microserviceOAuth2User.createAdditionalUsernameAndPasswordCredentialsOauth(request);
    }

    @ApiOperation(value = "first step, when user give access another application to your account")
    @RequestMapping(value = "oauth/get_new_token", method = RequestMethod.POST)
    public OAuthNewTokenResponse createOAuthToken(@RequestBody OAuth2NewTokenRequest userPosted) {
        OAuth2MicroserviceNewTokenRequest request = new OAuth2MicroserviceNewTokenRequest();
        request.setUserAccount(currentUser.getCurrentUser());
        request.setoAuth2Application(microserviceOAuth2Applications.findOAuth2ApplicationById(userPosted.getClientApplicationID()));
        request.setRequest(userPosted);

        UserAccountOAuth2 auth2 = microserviceOAuth2User.createNewOAuthToken(request);
        OAuth2Application oAuth2Application = microserviceOAuth2Applications.findOAuth2ApplicationById(auth2.getClientApplicationID());

        OAuthNewTokenResponse response = new OAuthNewTokenResponse();
        response.setClientApplicationID(auth2.getClientApplicationID());
        response.setAccessCode(auth2.getAccessCode());

        URIBuilder uriBuilder = null;

        String redirect_uri = oAuth2Application.getRedirect_uri();

        if (StringUtils.isEmpty(redirect_uri)) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("oauth.app_has_not_redirect");
        }

        try {
            uriBuilder = new URIBuilder(redirect_uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        }
        uriBuilder.addParameter("code", auth2.getAccessCode());

        response.setRedirectUri(uriBuilder.toString());
        return response;
    }

    @Secured(value = {SystemRoles.UPDATE_MYACCOUNT, SystemRoles.USER_ACCOUNT_EDIT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "update current user")
    @RequestMapping(method = RequestMethod.PUT)
    public UserAccount updateMyUserAccount(@RequestBody UserAccount userPosted){
        // don't allow to change username
        // and password, roles... etc
        UserAccount oldUserAccount = currentUser.getCurrentUser();

        oldUserAccount.setFirstname(userPosted.getFirstname());
        oldUserAccount.setLastname(userPosted.getLastname());
        oldUserAccount.setAvatarUrl(userPosted.getAvatarUrl());
        oldUserAccount.setPosition(userPosted.getPosition());
        oldUserAccount.setPatronymic(userPosted.getPatronymic());
        oldUserAccount.setEmail(userPosted.getEmail());
        oldUserAccount.setLanguage(userPosted.getLanguage());

        microserviceUsersRepository.updateUserAccount(oldUserAccount);
        return userPosted;
    }

    @ApiOperation(value = "get current user account oauth tokens")
    @RequestMapping(value = "oauth/tokens", method = RequestMethod.GET)
    public List<UserAccountOAuth2> getAllTokens() {
       return microserviceOAuth2User.getAllMyOAuthTokens();
    }

    @ApiOperation(value = "delete user account oauth tokens")
    @RequestMapping(value = "oauth/tokens/delete", method = RequestMethod.POST)
    public void deleteTokens(@RequestBody OAuthTokenModificationRequestDTO requestDto) {
        microserviceOAuth2User.deleteOauthTokenFromUserAccountById(currentUser.getCurrentUser().getId(), requestDto.getUsername());
    }

}


class TwoStepModifyRequest {
    private boolean enabled;
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
