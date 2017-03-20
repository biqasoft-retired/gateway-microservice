/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.customer.controllers;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.customer.Company;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.filters.CompanyFilter;
import com.biqasoft.gateway.customer.repositories.CompanyRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "Company", description = "company controller, used to control b2b agent, b2b clients, partners etc")
@RestController
@Secured({SYSTEM_ROLES.COMPANY_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@RequestMapping("/v1/company")
public class CompanyController {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Secured({SYSTEM_ROLES.COMPANY_GET_ALL, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get all companies", notes = "preferred to use '/filter' method ")
    @RequestMapping(method = RequestMethod.GET)
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Secured({SYSTEM_ROLES.COMPANY_ADD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation("add new company")
    @RequestMapping(method = RequestMethod.POST)
    public Company addNewCompany(@RequestBody Company customer, HttpServletResponse response) {
        companyRepository.addCompany(customer);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return customer;
    }

    @Secured(value = {SYSTEM_ROLES.COMPANY_EDIT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "update current company")
    @RequestMapping(method = RequestMethod.PUT)
    public Company updateCompany(@RequestBody Company customer) {
       return companyRepository.updateCompany(customer);
    }

    @Secured(value = {SYSTEM_ROLES.COMPANY_DELETE, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "delete company by ID")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void deleteCompanyById(HttpServletResponse response, @PathVariable("id") String id) {
        companyRepository.deleteCompanyById(id);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Secured(value = {SYSTEM_ROLES.COMPANY_EDIT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get company by ID")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public Company findCompanyById(HttpServletResponse response, @PathVariable("id") String id) {
        response.setStatus(HttpServletResponse.SC_OK);
        return companyRepository.findCompanyById(id);
    }

    @Secured(value = {SYSTEM_ROLES.COMPANY_GET_ALL, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "Get all companies with pagination and filters", notes = "get all companies, according to filters and pagination limits")
    @RequestMapping(value = "filter", method = RequestMethod.POST)
    public BiqaPaginationResultList<Company> getCompanyByFilter(@RequestBody CompanyFilter filter, HttpServletResponse response) {
        return companyRepository.getCompanyByFilter(filter);
    }

}
