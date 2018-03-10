/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.customer.controllers;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.customer.SegmentStats;
import com.biqasoft.entity.customer.StaticSegment;
import com.biqasoft.gateway.customer.repositories.SegmentsRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Secured(value = {SystemRoles.SEGMENT_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@Api(value = "Segments - customers & leads ")
@RequestMapping(value = "/v1/segment/static")
public class SegmentsStaticController {

    private final SegmentsRepository segmentsRepository;

    @Autowired
    public SegmentsStaticController(SegmentsRepository segmentsRepository) {
        this.segmentsRepository = segmentsRepository;
    }

    @Secured(value = {SystemRoles.SEGMENT_GET, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "Get all static segments ")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<StaticSegment> getAllStaticSegments() {
        return segmentsRepository.findAllStaticSegments();
    }

    @Secured(value = {SystemRoles.CUSTOMER_GET_ALL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get customers by static by id ")
    @RequestMapping(value = "{id}/customers", method = RequestMethod.GET)
    public List<Customer> getAllCustomersByStaticSegment(@PathVariable("id") String id) {
        return segmentsRepository.getAllCustomersByStaticSegment(id);
    }

    @Secured(value = {SystemRoles.CUSTOMER_GET_ALL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get detailed info by static by id ")
    @RequestMapping(value = "{id}/stats", method = RequestMethod.GET)
    public SegmentStats getStatsByStaticSegment(@PathVariable("id") String id) {
        return segmentsRepository.getStatsByStaticSegmentId(id);
    }

    @Secured(value = {SystemRoles.CUSTOMER_GET_ALL, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get static segment info by id ")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public StaticSegment getStaticSegmentMetaInfoById(@PathVariable("id") String id) {
        return segmentsRepository.findStaticSegmentById(id);
    }

    @ApiOperation(value = "add static segment")
    @Secured(value = {SystemRoles.SEGMENT_ADD, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @RequestMapping(value = "", method = RequestMethod.POST)
    public StaticSegment addStaticSegment(@RequestBody StaticSegment staticSegment, HttpServletResponse response) {
        segmentsRepository.addStaticSegment(staticSegment);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return staticSegment;
    }

    @ApiOperation(value = "update static segment")
    @Secured(value = {SystemRoles.SEGMENT_ADD, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public StaticSegment updateStaticSegment(@RequestBody StaticSegment staticSegment, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_CREATED);
        return segmentsRepository.updateStaticSegment(staticSegment);
    }

}



