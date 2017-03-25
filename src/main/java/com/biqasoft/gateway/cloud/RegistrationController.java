/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.cloud;

import com.biqasoft.auth.GatewayAuthenticationProvider;
import com.biqasoft.common.exceptions.InvalidRequestException;
import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.dto.useraccount.CreatedUser;
import com.biqasoft.entity.dto.useraccount.UserNameWithPassword;
import com.biqasoft.entity.dto.useraccount.UserRegisterRequest;
import com.biqasoft.gateway.cloud.democonfiguration.CreateTestData;
import com.biqasoft.gateway.cloud.dto.LandingPageRequestDTO;
import com.biqasoft.microservice.common.MicroserviceDomain;
import com.biqasoft.microservice.common.MicroserviceOAuth2User;
import com.biqasoft.microservice.common.MicroserviceUsersRepository;
import com.biqasoft.microservice.common.dto.OAuth2MicroserviceNewCredentialsRequest;
import io.swagger.annotations.Api;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@Api(hidden = true)
@ApiIgnore
@RestController
@RequestMapping(value = "landing_page")
public class RegistrationController {

    private final GatewayAuthenticationProvider gatewayAuthenticationProvider;
    private final CreateTestData createTestData;
    private final MicroserviceDomain microserviceDomain;
    private final MicroserviceUsersRepository microserviceUsersRepository;
    private final MicroserviceOAuth2User microserviceOAuth2User;


    @Autowired
    public RegistrationController(final GatewayAuthenticationProvider gatewayAuthenticationProvider, final CreateTestData createTestData,
                                  MicroserviceDomain microserviceDomain, MicroserviceUsersRepository microserviceUsersRepository,
                                  MicroserviceOAuth2User microserviceOAuth2User) {
        this.gatewayAuthenticationProvider = gatewayAuthenticationProvider;
        this.createTestData = createTestData;
        this.microserviceDomain = microserviceDomain;
        this.microserviceUsersRepository = microserviceUsersRepository;
        this.microserviceOAuth2User = microserviceOAuth2User;
    }

    /**
     * Create new domain and admin account on public registration landing page
     *
     * @param landingPageRequestDao
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public UserNameWithPassword publicRegisterNewUser(@RequestBody LandingPageRequestDTO landingPageRequestDao, HttpServletRequest request) {
        UserNameWithPassword landingPageResponseDao = new UserNameWithPassword();

        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(landingPageRequestDao.getUserAccount(), true, null, null);
        CreatedUser createdUser = microserviceUsersRepository.registerUser(userRegisterRequest);

        if (createdUser == null || createdUser.getUserAccount() == null || StringUtils.isEmpty(createdUser.getUserAccount().getId())) {
            ThrowExceptionHelper.throwExceptionInvalidRequest("Error create user");
            return null;
        }

        MDC.put("Authorization", "Basic " + new String(Base64.encodeBase64((createdUser.getUserAccount().getUsername() + ":" + createdUser.getPassword()).getBytes())));
        MDC.put("currentUserId", createdUser.getUserAccount().getId()); // this will be in logger

        // we need to authenticate user
        // to work with test data
        gatewayAuthenticationProvider.authenticateUser(createdUser.getUserAccount().getUsername(), createdUser.getPassword(), new WebAuthenticationDetails(request));

        try {
            // create a lot of test (DEMO) data
            createTestData.create(createdUser.getUserAccount(), createdUser.getDomain(), landingPageRequestDao.getTimeZoneOffset());
        } catch (Exception e) {

            // if we can not create some data - delete created account and drop database
            microserviceUsersRepository.unsafeDeleteUserById(createdUser.getUserAccount().getId());
            microserviceDomain.unsafeDelete(createdUser.getDomain());

            throw new InvalidRequestException(e.getMessage());
        }

        // return to user api new login & username
        // be default return OAuth2 credentials
        if (landingPageRequestDao.isReturnUserNameAndPassword()) {
            landingPageResponseDao.setPassword(createdUser.getPassword());
            landingPageResponseDao.setUsername(landingPageRequestDao.getUserAccount().getUsername());
        } else {
            OAuth2MicroserviceNewCredentialsRequest credentialsRequest = new OAuth2MicroserviceNewCredentialsRequest();
            credentialsRequest.setExpireDate(null);
            credentialsRequest.setRolesRequested(new ArrayList<>());
            credentialsRequest.setUserAccount(createdUser.getUserAccount());

            landingPageResponseDao = microserviceOAuth2User.createAdditionalUsernameAndPasswordCredentialsOauth(credentialsRequest);
        }

        return landingPageResponseDao;
    }

}
