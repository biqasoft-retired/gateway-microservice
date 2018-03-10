/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.payments.controllers;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.customer.Opportunity;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.filters.DealsFilter;
import com.biqasoft.entity.payments.CustomerDeal;
import com.biqasoft.gateway.cloud.DateServiceRequestContext;
import com.biqasoft.gateway.customer.repositories.CustomerRepository;
import com.biqasoft.gateway.customer.repositories.OpportunityRepository;
import com.biqasoft.gateway.payments.repositories.PaymentsRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Api(value = "Payment, Deals & Costs")
@Secured(value = {SystemRoles.PAYMENT_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/payments/customer_deal")
public class CustomerDealController {

    private PaymentsRepository paymentsRepository;
    private CustomerRepository customerRepository;
    private OpportunityRepository opportunityRepository;

    @Secured(value = {SystemRoles.PAYMENT_GET_CUSTOMER_DEALS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all customer deals")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<CustomerDeal> getAllCustomerDeal() {
        DealsFilter dealsBuilder = new DealsFilter();
        return paymentsRepository.getDealsByFilter(dealsBuilder).getResultedObjects();
    }

    @Secured(value = {SystemRoles.PAYMENT_EDIT_CUSTOMER_DEALS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "update customer deal")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public CustomerDeal updatedCustomerDeal(@RequestBody CustomerDeal customer) {
        return paymentsRepository.updateCustomerDeal(customer);
    }

    @Secured(value = {SystemRoles.PAYMENT_GET_CUSTOMER_DEALS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get deals by customer ID")
    @RequestMapping(value = "common/customer_id/{id}", method = RequestMethod.GET)
    public List<CustomerDeal> findAllDealsByCustomerId(@PathVariable("id") String id) {
        return paymentsRepository.findAllDealsByCustomerId(id);
    }

    @Secured(value = {SystemRoles.PAYMENT_DELETE_CUSTOMER_DEALS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "delete deal by ID")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void deleteCustomerDealById(@PathVariable("id") String id) {
        paymentsRepository.deleteCustomerDeal(id);
    }

    @Secured(value = {SystemRoles.PAYMENT_ADD_CUSTOMER_DEAL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "add new company deal",
            notes = "leadGenMethod & project for customer deal can be independent from customer but if you not specify - they will be get from customer info")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public CustomerDeal addNewCustomerDeal(@RequestBody CustomerDeal customerDeal, HttpServletResponse response) {

        Customer connectedCustomer = customerRepository.findCustomerOrLeadByID(customerDeal.getConnectedInfo().getConnectedCustomerId());

        if (customerDeal.getLeadGenProjectId() == null || customerDeal.getLeadGenProjectId().equals(""))
            customerDeal.setLeadGenProjectId(connectedCustomer.getLeadGenProject());
        if (customerDeal.getLeadGenMethodId() == null || customerDeal.getLeadGenMethodId().equals(""))
            customerDeal.setLeadGenMethodId(connectedCustomer.getLeadGenMethod());

        paymentsRepository.addCustomerDeal(customerDeal);

        response.setStatus(HttpServletResponse.SC_CREATED);
        return customerDeal;
    }

    @Secured(value = {SystemRoles.PAYMENT_ADD_CUSTOMER_DEAL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "transfer opportunity to deal")
    @RequestMapping(value = "deal_from_opportunity/{id}", method = RequestMethod.POST)
    public CustomerDeal transferOpportunityToDeal(HttpServletResponse response, @PathVariable("id") String id) {
        Opportunity opportunity = opportunityRepository.findOpportunityById(id);
        Customer customer = customerRepository.findCustomerOrLeadByID(opportunity.getConnectedInfo().getConnectedCustomerId());

        CustomerDeal customerDeal = new CustomerDeal();

        customerDeal.setLeadGenMethodId(opportunity.getLeadGenMethodId());
        customerDeal.setLeadGenProjectId(opportunity.getLeadGenProjectId());

        customerDeal.getConnectedInfo().setConnectedCustomerId(customer.getId());
        customerDeal.setName(opportunity.getName());
        customerDeal.setAmount(opportunity.getAmount());
        customerDeal.setOpportunityCreatedDate(opportunity.getCreatedInfo().getCreatedDate());
        customerDeal.setOpportunityCreatedById(opportunity.getCreatedInfo().getCreatedById());

        try {
            customerDeal.setDealsCycle(DateServiceRequestContext.getDateDiff(customerDeal.getOpportunityCreatedDate(), new Date(), TimeUnit.SECONDS));
        } catch (Exception e) {
        }

        paymentsRepository.addCustomerDeal(customerDeal);
        opportunityRepository.deleteOpportunity(opportunity.getId());

        response.setStatus(HttpServletResponse.SC_CREATED);
        return customerDeal;
    }

    @Secured(value = {SystemRoles.PAYMENT_GET_CUSTOMER_DEALS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get customer deals by filter (builder) ")
    @RequestMapping(value = "filter", method = RequestMethod.POST)
    public BiqaPaginationResultList<CustomerDeal> customerDealBuilder(@RequestBody DealsFilter dealsBuilder) {
        return paymentsRepository.getDealsByFilter(dealsBuilder);
    }

    @Autowired
    public void setPaymentsRepository(PaymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    @Autowired
    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Autowired
    public void setOpportunityRepository(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }
}
