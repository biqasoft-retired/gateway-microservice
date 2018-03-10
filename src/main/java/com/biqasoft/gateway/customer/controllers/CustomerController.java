/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.customer.controllers;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.filters.CustomerFilter;
import com.biqasoft.gateway.customer.dto.CustomerDetails;
import com.biqasoft.gateway.customer.repositories.CustomerFilterRequestContextService;
import com.biqasoft.gateway.customer.repositories.CustomerExcelRepository;
import com.biqasoft.gateway.customer.repositories.CustomerRepository;
import com.biqasoft.gateway.customer.repositories.OpportunityRepository;
import com.biqasoft.gateway.payments.repositories.PaymentsRepository;
import com.biqasoft.gateway.tasks.repositories.TaskRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.biqasoft.entity.constants.SYSTEM_CONSTS.EXCEL_MIME_TYPE;

@RestController
@Secured(value = {SystemRoles.CUSTOMER_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@Api(value = "Customers & Leads ")
@RequestMapping(value = "/v1/customer")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final TaskRepository taskRepository;
    private final PaymentsRepository paymentsRepository;
    private final OpportunityRepository opportunityRepository;
    private final CustomerFilterRequestContextService customerFilterRequestContextService;
    private final CustomerExcelRepository customerExcelRepository;

    @Autowired
    public CustomerController(CustomerRepository customerRepository, TaskRepository taskRepository,
                              PaymentsRepository paymentsRepository, OpportunityRepository opportunityRepository,
                              CustomerFilterRequestContextService customerFilterRequestContextService,
                              CustomerExcelRepository customerExcelRepository) {
        this.customerRepository = customerRepository;
        this.taskRepository = taskRepository;
        this.paymentsRepository = paymentsRepository;
        this.opportunityRepository = opportunityRepository;
        this.customerFilterRequestContextService = customerFilterRequestContextService;
        this.customerExcelRepository = customerExcelRepository;
    }

    @Secured(value = {SystemRoles.CUSTOMER_GET_ALL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "Get all customers", notes = "get all customers, without any pagination. if you have a lot of customers, the response can be big ")
    @RequestMapping(method = RequestMethod.GET)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllCustomers();
    }

    @Secured(value = {SystemRoles.CUSTOMER_GET_ALL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "Get all customers with pagination and filters", notes = "get all customers (or leads), according to filters and pagination limits")
    @RequestMapping(value = "filter", method = RequestMethod.POST)
    public BiqaPaginationResultList<Customer> getCustomerByFilter(@RequestBody CustomerFilter filter) {
        return customerFilterRequestContextService.getCustomersByFilter(filter);
    }

    @Secured(value = {SystemRoles.CUSTOMER_GET_ALL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get one customer or lead by id ")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public Customer getCustomerOrLeadById(HttpServletResponse response, @PathVariable("id") String id) {
        return customerRepository.findCustomerOrLeadByID(id);
    }

    @Secured(value = {SystemRoles.CUSTOMER_GET_ALL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get one customer or lead by id with deals, opportunities and tasks")
    @RequestMapping(value = "details/{id}", method = RequestMethod.GET)
    public CustomerDetails getDetailedCustomerOrLeadByIdDetails(@PathVariable("id") String id) {
        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setCustomer(customerRepository.findCustomerOrLeadByID(id));
        customerDetails.setTasks(taskRepository.findTasksForCustomer(id));
        customerDetails.setOpportunities(opportunityRepository.findOpportunitieByCustomerId(id));
        customerDetails.setCustomerDeals(paymentsRepository.findAllDealsByCustomerId(id));

        if (customerDetails.getCustomer() == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("invalid.request.no_customer");
        }

        return customerDetails;
    }

    @Secured(value = {SystemRoles.CUSTOMER_EDIT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "update customer ", notes = "full updates customer or lead with all new data")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public Customer updateCustomer(@RequestBody Customer customer) {
        return customerRepository.updateCustomerForController(customer);
    }

    @ApiOperation(value = "add one new customer or lead")
    @Secured(value = {SystemRoles.CUSTOMER_ADD, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @RequestMapping(method = RequestMethod.POST)
    public Customer addNewCustomer(@RequestBody Customer customer, HttpServletResponse response) {
        customerRepository.addCustomer(customer);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return customer;
    }

    @Secured({SystemRoles.CUSTOMER_ADD, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "add list (array) of new customers or leads ",
            notes = "add new and update existing related on ID of customer")
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public List<Customer> massListCustomer(@RequestBody List<Customer> customers, HttpServletResponse response) {
        customerRepository.addListCustomers(customers);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return customers;
    }

    @Secured({SystemRoles.CUSTOMER_DOWNLOAD_EXCEL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get customer in excel by criteria (builder)",
            notes = ", if you have a lot of customers, it can take up to 2 minutes")
    @RequestMapping(value = "filter/excel", method = RequestMethod.POST)
    public ResponseEntity<byte[]> getCustomerInEXCEL(@RequestBody CustomerFilter customerBuilder, HttpServletResponse response, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(EXCEL_MIME_TYPE));

        return new ResponseEntity<>(customerExcelRepository.getCustomerInEXCEL(customerBuilder), headers, HttpStatus.ACCEPTED);
    }

//    @Secured(value = {SystemRoles.CUSTOMER_ADD, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
//    @ApiOperation(value = "parse XLSX to array of customers",
//            notes = " . If you want to update already existing customer/lead you MUST specify customer ID," +
//                    "otherwise, new customer/lead will be created ")
//    @RequestMapping(value = "/parse_excel_to_json", method = RequestMethod.POST)
//    public
//    @ResponseBody
//    List<Customer> getParsedJsonFromUploadedXLSXForCustomers(@RequestParam("file") MultipartFile[] fileSource, HttpServletResponse response) {
//        return customerExcelRepository.getParsedJsonFromUploadedXLSXForCustomers(fileSource);
//    }

}
