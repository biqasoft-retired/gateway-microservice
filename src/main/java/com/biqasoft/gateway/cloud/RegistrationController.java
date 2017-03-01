/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.cloud;

import com.biqasoft.auth.GatewayAuthenticationProvider;
import com.biqasoft.common.exceptions.InvalidRequestException;
import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.dto.useraccount.UserNameWithPassword;
import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.dto.useraccount.CreatedUser;
import com.biqasoft.entity.dto.useraccount.UserRegisterRequest;
import com.biqasoft.entity.core.Domain;
import com.biqasoft.entity.core.useraccount.UserAccount;
import com.biqasoft.gateway.cloud.democonfiguration.CreateTestData;
import com.biqasoft.gateway.cloud.dto.LandingPageRequestDTO;
import com.biqasoft.microservice.common.MicroserviceDomain;
import com.biqasoft.microservice.common.MicroserviceOAuth2User;
import com.biqasoft.microservice.common.MicroserviceUsersRepository;
import com.biqasoft.microservice.common.dto.OAuth2MicroserviceNewCredentialsRequest;
import io.swagger.annotations.Api;
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
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

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
     * @param response
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public UserNameWithPassword publicRegisterNewUser(@RequestBody LandingPageRequestDTO landingPageRequestDao, HttpServletRequest request, HttpServletResponse response) {
        UserNameWithPassword landingPageResponseDao = new UserNameWithPassword();

        // user with same email already exist
        if (microserviceUsersRepository.unsafeFindByUsernameOrOAuthToken(landingPageRequestDao.getUserAccount().getEmail()) != null) {
            throw new InvalidRequestException("user with this email already exist");
        }

        Domain domain = new Domain();
        domain = microserviceDomain.create(domain);

        // create new admin account
        UserAccount user = new UserAccount();

        user.setTelephone(landingPageRequestDao.getUserAccount().getTelephone());
        user.setUsername(landingPageRequestDao.getUserAccount().getEmail());
        user.setFirstname(landingPageRequestDao.getUserAccount().getFirstname());
        user.setLastname(landingPageRequestDao.getUserAccount().getLastname());
        user.setEmail(landingPageRequestDao.getUserAccount().getEmail());

        List<String> roles = new ArrayList<>();
        roles.add(SYSTEM_ROLES.ROLE_ADMIN);
        roles.add(SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED);
        user.setRoles(roles);
        //

        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(user, true, domain.getDomain(), null);
        CreatedUser createdUser = microserviceUsersRepository.registerUser(userRegisterRequest);

        if (createdUser == null || createdUser.getUserAccount() == null || StringUtils.isEmpty(createdUser.getUserAccount().getId())) {
            ThrowExceptionHelper.throwExceptionInvalidRequest("Error create user");
            return null;
        }

        MDC.put("Authorization", new String(org.apache.tomcat.util.codec.binary.Base64.encodeBase64( (createdUser.getUserAccount().getUsername() + ":" + createdUser.getPassword()).getBytes())));
        MDC.put("currentUserId", createdUser.getUserAccount().getId()); // this will be in logger

        // we need to authenticate user
        // to work with test data
        gatewayAuthenticationProvider.authenticateUser(createdUser.getUserAccount().getUsername(), createdUser.getPassword(), new WebAuthenticationDetails(request));

        try {
            // create a lot of test (DEMO) data
            createTestData.create(user, domain.getDomain(), landingPageRequestDao.getTimeZoneOffset());
        } catch (Exception e) {

            // if we can not create some data - delete created account and drop database
            microserviceUsersRepository.unsafeDeleteUserById(user.getId());
            microserviceDomain.unsafeDelete(domain.getDomain());

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
            credentialsRequest.setUserAccount(user);

            landingPageResponseDao = microserviceOAuth2User.createAdditionalUsernameAndPasswordCredentialsOauth(credentialsRequest);
        }

        return landingPageResponseDao;
    }

}
