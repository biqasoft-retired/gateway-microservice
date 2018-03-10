/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.leadgen.controllers;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.customer.LeadGenProject;
import com.biqasoft.gateway.leadgen.repositories.LeadGenRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Api(value = "customer and Leads Sales Methods & Projects")
@Secured(value = {SystemRoles.LEAD_GEN_METHOD_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/lead_gen_method/lead_gen_project")
public class LeadGenProjectController {

    private final LeadGenRepository leadRepository;

    @Autowired
    public LeadGenProjectController(LeadGenRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Secured(value = {SystemRoles.LEAD_GEN_METHOD_GET_ALL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get LeadGenProject by promoCode")
    @RequestMapping(value = "/promo_codes/{id}", method = RequestMethod.GET)
    public LeadGenProject findLeadGenProjectByPromoCode(@PathVariable("id") String id) {
        return leadRepository.findLeadGenProjectByPromoCode(id);
    }

    @Secured(value = {SystemRoles.LEAD_GEN_METHOD_GET_ALL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all promo codes in system", notes = "from all sales methods (channels)")
    @RequestMapping(value = "/promo_codes", method = RequestMethod.GET)
    public List<String> findAllLeadGenProjectPromoCodes() {
        List<String> allPromoCodes = new ArrayList<>();
        List<LeadGenProject> leadGenProjects = leadRepository.findAllLeadGenProject();
        for (LeadGenProject leadGenProject : leadGenProjects) {
            allPromoCodes.addAll(leadGenProject.getPromoCodes());
        }

        return allPromoCodes;
    }

    @Secured(value = {SystemRoles.LEAD_GEN_METHOD_EDIT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "update current sale project")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public LeadGenProject updateLeadGeProject(@RequestBody LeadGenProject leadGenProject, HttpServletResponse response) {
        leadRepository.updateLeadGenProject(leadGenProject);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return leadGenProject;
    }

    @Secured(value = {SystemRoles.LEAD_GEN_METHOD_ADD, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "add new current sale project")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public LeadGenProject addNewLeadGenProject(@RequestBody LeadGenProject role, HttpServletResponse response) {
        LeadGenProject leadGenProject = new LeadGenProject();
        leadGenProject.setName(role.getName());

        leadGenProject.setLeadGenMethodId(role.getLeadGenMethodId());
        leadRepository.addLeadGenProject(leadGenProject);

        response.setStatus(HttpServletResponse.SC_CREATED);
        return leadGenProject;
    }

    @Secured(value = {SystemRoles.LEAD_GEN_METHOD_GET_ALL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all sales projects")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<LeadGenProject> getAllLeadGenProjects() {
        return leadRepository.findAllLeadGenProject();
    }

    @Secured(value = {SystemRoles.LEAD_GEN_METHOD_GET_ALL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get sale project by ID")
    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public LeadGenProject getLeadGenProjectById(@PathVariable("id") String id) {
        return leadRepository.findLeadGenProjectById(id);
    }

}
