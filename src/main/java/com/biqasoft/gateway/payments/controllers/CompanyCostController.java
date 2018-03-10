/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.payments.controllers;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.filters.CostsFilter;
import com.biqasoft.entity.payments.CompanyCost;
import com.biqasoft.gateway.payments.repositories.PaymentsRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "Payment, Deals & Costs")
@Secured(value = {SystemRoles.PAYMENT_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/payments/company_cost")
public class CompanyCostController {

    private final PaymentsRepository paymentsRepository;

    @Autowired
    public CompanyCostController(PaymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    @Secured(value = {SystemRoles.PAYMENT_GET_CUSTOMER_DEALS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all customer deals by sales project ID")
    @RequestMapping(value = "common/all/lead_gen_project_id/{id}", method = RequestMethod.GET)
    public  List<CompanyCost> getAllCompanyCostsByLeadGenProjectId(@PathVariable("id") String id) {
        return paymentsRepository.findAllCompanyCostByLeadGenProjectId(id);
    }

    @Secured(value = {SystemRoles.PAYMENT_GET_COMPANY_COST, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all company costs")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public  List<CompanyCost> getAllCompanyCosts() {
        return paymentsRepository.findAllCompanyCost();
    }

    @Secured(value = {SystemRoles.PAYMENT_EDIT_COMPANY_COST, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "update company cost")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public  CompanyCost updatedCompanyCost(@RequestBody CompanyCost customer) {
        return paymentsRepository.updateCompanyCost(customer);
    }

    @Secured(value = {SystemRoles.PAYMENT_ADD_COMPANY_COST, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "add new company cost")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public CompanyCost addNewCompanyCost(@RequestBody CompanyCost customer, HttpServletResponse response) {
        paymentsRepository.addCompanyCost(customer);

        response.setStatus(HttpServletResponse.SC_CREATED);
        return customer;
    }

    @Secured(value = {SystemRoles.PAYMENT_GET_COMPANY_COST, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "costs by filter (builder) ")
    @RequestMapping(value = "filter", method = RequestMethod.POST)
    public  BiqaPaginationResultList<CompanyCost> costsBuilder(@RequestBody CostsFilter costsBuilder) {
        return paymentsRepository.getCostsByFilter(costsBuilder);
    }

}
