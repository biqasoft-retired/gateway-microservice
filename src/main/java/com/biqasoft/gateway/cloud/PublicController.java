/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.cloud;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.analytics.CustomerWebAnalytics;
import com.biqasoft.entity.analytics.RaCookieId;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.customer.LeadGenProject;
import com.biqasoft.gateway.analytics.PublicLeadPost;
import com.biqasoft.gateway.analytics.WebSDKSendCustomer;
import com.biqasoft.gateway.analytics.repositories.AnalyticsRepository;
import com.biqasoft.gateway.customer.repositories.CustomerRepository;
import com.biqasoft.gateway.leadgen.repositories.LeadGenRepository;
import com.biqasoft.gateway.useraccount.MicroserviceUsersPasswordReset;
import com.biqasoft.gateway.useraccount.dto.ResetPasswordTokenDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Api(value = "controller for cloud version", hidden = true)
@ApiIgnore
@RestController
@RequestMapping(value = "/v1/public")
public class PublicController {

    private final LeadGenRepository leadRepository;
    private final CustomerRepository customerRepository;
    private final AnalyticsRepository analyticsRepository;
    private final MicroserviceUsersPasswordReset microserviceUsersPasswordReset;
    private final String analyticCookieDomain;


    @Autowired
    public PublicController(LeadGenRepository leadRepository, CustomerRepository customerRepository, AnalyticsRepository analyticsRepository,
                            @Value("${biqa.analytics.cookie.domain}") String analyticCookieDomain, MicroserviceUsersPasswordReset microserviceUsersPasswordReset) {
        this.leadRepository = leadRepository;
        this.customerRepository = customerRepository;
        this.analyticsRepository = analyticsRepository;
        this.analyticCookieDomain = analyticCookieDomain;
        this.microserviceUsersPasswordReset = microserviceUsersPasswordReset;
    }

    @ApiOperation("Reset user password by secret token")
    @RequestMapping(value = "reset_password", method = RequestMethod.POST)
    public void resetUserPasswordOperation(@RequestBody ResetPasswordTokenDTO resetPasswordTokenDao) {
        microserviceUsersPasswordReset.removeAndResetPasswordTokenDao(resetPasswordTokenDao);
    }

    @ApiOperation("Request to reset user password")
    @RequestMapping(value = "reset_password_request", method = RequestMethod.POST)
    public void resetPasswordRequest(@RequestBody ResetPasswordTokenDTO resetPasswordTokenDao) {
        microserviceUsersPasswordReset.create(resetPasswordTokenDao);
    }

    @RequestMapping(value = "generate_new_user_analytics_id", method = RequestMethod.GET)
    public RaCookieId generateNewUserAnalyticsId(HttpServletResponse response) {

        RaCookieId cookieId = new RaCookieId();

        Cookie cookie = new Cookie("userCookieId", cookieId.getId());
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        cookie.setDomain(analyticCookieDomain);
        cookie.setPath("/");
        cookie.setMaxAge(200000000);

        response.setHeader("analytics-Id", cookieId.getId());
        response.addCookie(cookie);

        return new RaCookieId();
    }

    @ApiOperation("Post lead with marketing score")
    @RequestMapping(value = "post_lead", method = RequestMethod.POST)
    public Customer postLeadWithMarketingScore(@RequestBody PublicLeadPost publicLeadPost, HttpServletResponse response) {

        if (analyticsRepository.findWebAnalyticsCounter(publicLeadPost.getAnalyticRecord().getCounterId()) == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("analytics.no_such_counter");
        }

        WebSDKSendCustomer lead = publicLeadPost.getLead();
        lead.setDomain(analyticsRepository.findWebAnalyticsCounter(publicLeadPost.getAnalyticRecord().getCounterId()).getDomain());
        lead.setLead(true);
        lead.setActive(true);

        publicLeadPost.getAnalyticRecord().setDomain(lead.getDomain());
        publicLeadPost.getAnalyticRecord().setCreatedDate(new Date());

        analyticsRepository.addAnalyticRecord(publicLeadPost.getAnalyticRecord());

        CustomerWebAnalytics customerWebAnalytics = new CustomerWebAnalytics();
        List<String> userCookieWebAnalyticsIds = new ArrayList<>();
        userCookieWebAnalyticsIds.add(publicLeadPost.getAnalyticRecord().getUserCookieId());
        customerWebAnalytics.setCookiesIds(userCookieWebAnalyticsIds);

        lead.setCustomerWebAnalytics(customerWebAnalytics);

        LeadGenProject leadGenProject = leadRepository.findLeadGenProjectByUTMMetrics(lead.getDomain(), publicLeadPost.getAnalyticRecord().getUtm());

        lead.setLeadGenProject(leadGenProject.getId());
        lead.setLeadGenMethod(leadGenProject.getLeadGenMethodId());
        lead.setCreatedInfo(new CreatedInfo(new Date()));

        customerRepository.addWebSdkLead(lead, leadRepository, lead.getDomain());

        response.setStatus(HttpServletResponse.SC_CREATED);
        return lead;
    }

}

