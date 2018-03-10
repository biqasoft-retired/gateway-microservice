/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.analytics.controllers;

import com.biqasoft.entity.analytics.AnalyticRecord;
import com.biqasoft.entity.analytics.WebAnalyticsCounter;
import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.gateway.analytics.repositories.AnalyticsRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Secured(value = {SystemRoles.ANALYTIC_PRIVATE_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@Api(value = "Private analytics",
        description = "analytics for company staff, aggregation data etc")
@RequestMapping(value = "/v1/analytics/web")
public class AnalyticsPrivateController {

    private final AnalyticsRepository analyticsRepository;

    @Autowired
    public AnalyticsPrivateController(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @Secured(value = {SystemRoles.ANALYTIC_ADD_COUNTER, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "add new counter")
    @RequestMapping(value = "/counter", method = RequestMethod.POST)
    public WebAnalyticsCounter addNewAnalyticCounter(HttpServletResponse response) {
        WebAnalyticsCounter webAnalyticsCounter = new WebAnalyticsCounter();
        analyticsRepository.addAnalyticCounter(webAnalyticsCounter);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return webAnalyticsCounter;
    }

    @Secured(value = {SystemRoles.ANALYTIC_ADD_COUNTER, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "update counter")
    @RequestMapping(value = "/counter", method = RequestMethod.PUT)
    public WebAnalyticsCounter updateAnalyticCounter(@RequestBody WebAnalyticsCounter webAnalyticsCounter, HttpServletResponse response) {
        analyticsRepository.updateAnalyticsCounter(webAnalyticsCounter);
        response.setStatus(HttpServletResponse.SC_OK);
        return webAnalyticsCounter;
    }

    @Secured(value = {SystemRoles.ANALYTIC_GET_COUNTERS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get web counter by id")
    @RequestMapping(value = "/counter/{id}", method = RequestMethod.GET)
    public WebAnalyticsCounter getCounterById(@PathVariable("id") String id) {
        return analyticsRepository.findWebAnalyticsCounter(id);
    }

    @Secured(value = {SystemRoles.ANALYTIC_GET_COUNTERS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all web counters")
    @RequestMapping(value = "/counters", method = RequestMethod.GET)
    public List<WebAnalyticsCounter> getAllWebAnalyticsCounters() {
        return analyticsRepository.findAllAnalyticsCounters();
    }

    @Secured(value = {SystemRoles.ANALYTIC_RECORDS_MAP_REDUCE_FILTERS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all analytic records form some counter by id")
    @RequestMapping(value = "/records_by_counter_id/all/{id}", method = RequestMethod.GET)
    public List<AnalyticRecord> getAllAnalyticsRecordsByCounterId(@PathVariable("id") String id) {
        return analyticsRepository.findAllAnalyticRecordsByCounterId(id);
    }

    @Secured(value = {SystemRoles.ANALYTIC_RECORDS_MAP_REDUCE_FILTERS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all analytic records by some user(client/customer/lead) ID (cookie, email, telephone etc)")
    @RequestMapping(value = "/analytics_record/by_cookies_ids", method = RequestMethod.POST)
    public List<AnalyticRecord> getAnalyticsRecordsByUserId(@RequestBody List<String> userCookies) {
        return analyticsRepository.findAllAnalyticRecordsByCookiesIds(userCookies);
    }

}
