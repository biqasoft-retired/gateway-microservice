/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.payments.controllers;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.customer.Opportunity;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.filters.OpportunityFilter;
import com.biqasoft.gateway.customer.repositories.OpportunityRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Api(value = "Opportunity")
@Secured(value = {SystemRoles.OPPORTUNITY_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/opportunity")
public class OpportunityController {

    private final OpportunityRepository opportunityRepository;

    @Autowired
    public OpportunityController(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }

    @Secured(value = {SystemRoles.OPPORTUNITY_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all opportunities")
    @RequestMapping(method = RequestMethod.GET)
    public List<Opportunity> getAllOpportunities() {
        return opportunityRepository.findAll();
    }

    @Secured(value = {SystemRoles.OPPORTUNITY_GET, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all opportunities by customer or lead id")
    @RequestMapping(value = "common/all/customer_id/{id}", method = RequestMethod.GET)
    public List<Opportunity> findOpportunitiesByCustomerId(@PathVariable("id") String id) {
        return opportunityRepository.findOpportunitieByCustomerId(id);
    }

    @Secured(value = {SystemRoles.OPPORTUNITY_GET, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get only active opportunities")
    @RequestMapping(value = "common/all/customer_id/{id}/active", method = RequestMethod.GET)
    public List<Opportunity> findOpportunitiesByCustomerIdAndActive(@PathVariable("id") String id) {
        return opportunityRepository.findAllActiveByCustomerId(id);
    }

    @Secured(value = {SystemRoles.OPPORTUNITY_EDIT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "update current opportunity")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public Opportunity updateOpportunityById(@RequestBody Opportunity opportunity) {
        return opportunityRepository.updateOpportunity(opportunity);
    }

    @Secured(value = {SystemRoles.OPPORTUNITY_DELETE, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "delete opportunity")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void deleteOpportunityById(HttpServletResponse response, @PathVariable("id") String id) {
        opportunityRepository.deleteOpportunity(id);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Secured(value = {SystemRoles.OPPORTUNITY_GET, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get one opportunity by ID")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public Opportunity detailedOpportunityInfo(@PathVariable("id") String id) {
        return opportunityRepository.findOpportunityById(id);
    }

    @Secured(value = {SystemRoles.OPPORTUNITY_ADD, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "add new opportunity",
            notes = "leadGenMethod & project can be independent from customer but if you not specify - they will be get from customer info")
    @RequestMapping(method = RequestMethod.POST)
    public Opportunity addOneOpportunity(@RequestBody Opportunity opportunity, HttpServletResponse response) {
        opportunity.setActive(true);

        opportunityRepository.addOpportunity(opportunity);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return opportunity;
    }

    @Secured(value = {SystemRoles.OPPORTUNITY_ADD, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "add list of opportunities")
    @RequestMapping(value = "all", method = RequestMethod.POST)
    public List<Opportunity> getListOpportunities(@RequestBody List<Opportunity> opportunities, HttpServletResponse response) {
        List<Opportunity> opportunityList = new ArrayList<>();

        for (Opportunity opportunity : opportunities) {
            opportunityRepository.addOpportunity(opportunity);
            opportunityList.add(opportunity);
        }

        response.setStatus(HttpServletResponse.SC_CREATED);
        return opportunityList;
    }

    @Secured(value = {SystemRoles.PAYMENT_GET_CUSTOMER_DEALS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get opportunities  by filter (builder) ")
    @RequestMapping(value = "filter", method = RequestMethod.POST)
    public BiqaPaginationResultList<Opportunity> customerOpportunity(@RequestBody OpportunityFilter opportunityBuilder) {
        return opportunityRepository.getOpportunitysByFilter(opportunityBuilder);
    }

}
