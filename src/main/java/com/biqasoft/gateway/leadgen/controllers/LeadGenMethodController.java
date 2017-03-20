/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.leadgen.controllers;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.customer.LeadGenMethod;
import com.biqasoft.entity.customer.LeadGenProject;
import com.biqasoft.entity.filters.LeadGenMethodExcelFilter;
import com.biqasoft.gateway.leadgen.repositories.LeadGenRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.biqasoft.entity.constants.SYSTEM_CONSTS.EXCEL_MIME_TYPE;

@Api(value = "customer and Leads Sales Methods & Projects")
@Secured(value = {SYSTEM_ROLES.LEAD_GEN_METHOD_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/lead_gen_method")
public class LeadGenMethodController {

    private final LeadGenRepository leadRepository;
    private final LeadGenMethodExcelReport leadGenMethodsExcelReport;

    @Autowired
    public LeadGenMethodController(LeadGenMethodExcelReport leadGenMethodsExcelReport, LeadGenRepository leadRepository) {
        this.leadGenMethodsExcelReport = leadGenMethodsExcelReport;
        this.leadRepository = leadRepository;
    }

    @Secured(value = {SYSTEM_ROLES.LEAD_GEN_METHOD_ADD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "add new sale method")
    @RequestMapping(method = RequestMethod.POST)
    public LeadGenMethod addNewLeadGenMethod(@RequestBody LeadGenMethod role, HttpServletResponse response) {
        LeadGenMethod leadGenMethod = new LeadGenMethod();
        leadGenMethod.setName(role.getName());

        leadRepository.addLeadGenMethod(leadGenMethod);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return leadGenMethod;
    }

    @Secured(value = {SYSTEM_ROLES.LEAD_GEN_METHOD_GET_ALL, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get sales method by promo code")
    @RequestMapping(value = "/promo_codes/{id}", method = RequestMethod.GET)
    public LeadGenMethod findLeadGenMethodByPromoCode(@PathVariable("id") String id) {
        LeadGenProject leadGenProject = leadRepository.findLeadGenProjectByPromoCode(id);
        LeadGenMethod l = leadRepository.findLeadGenMethodById(leadGenProject.getLeadGenMethodId());
        return l;
    }

    @Secured(value = {SYSTEM_ROLES.LEAD_GEN_METHOD_EDIT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "update current sale methods")
    @RequestMapping(method = RequestMethod.PUT)
    public LeadGenMethod updateLeadGenMethod(@RequestBody LeadGenMethod leadGenMethod, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_CREATED);
        return leadRepository.updateLeadGenMethod(leadGenMethod);
    }

    @Secured(value = {SYSTEM_ROLES.LEAD_GEN_METHOD_GET_ALL, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get all lead gen methods")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<LeadGenMethod> getAllLeadGenMethod() {
        List<LeadGenMethod> methods;
        methods = leadRepository.findAllLeadGenMethod();
        return methods;
    }

    @Secured(value = {SYSTEM_ROLES.LEAD_GEN_METHOD_GET_ALL, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get sale method by ID and resolve sales funnel data sources")
    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public LeadGenMethod getLeadGenMethodWithResolvedSalesFunnelStatuses(@PathVariable("id") String id) {
        return leadRepository.findLeadGenMethodById(id);
    }

    @Secured(value = {SYSTEM_ROLES.LEAD_GEN_METHOD_EXCEL, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get lead gen methods with KPIs in excel by criteria (builder)", notes = ", if you have a lot of methods, it can take up to 2 minutes")
    @RequestMapping(value = "filter/excel", method = RequestMethod.POST)
    public ResponseEntity<byte[]> getLeadGenMethodExcelFilter(@RequestBody LeadGenMethodExcelFilter leadGenMethodBuilder) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(EXCEL_MIME_TYPE));

        ResponseEntity responseEntity = new ResponseEntity(leadGenMethodsExcelReport.getResponseEntity(leadGenMethodBuilder), headers, HttpStatus.ACCEPTED);
        return responseEntity;
    }

}
