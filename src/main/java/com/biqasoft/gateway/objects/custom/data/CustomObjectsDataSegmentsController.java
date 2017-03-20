/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.objects.custom.data;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.objects.CustomObjectDataSegment;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_DATA_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@Api(value = "Custom objects segments")
@RequestMapping(value = "/v1/objects/custom/data/segments")
public class CustomObjectsDataSegmentsController {

    private final CustomObjectsDataSegmentsRepository customObjectsDataSegmentsRepository;

    @Autowired
    public CustomObjectsDataSegmentsController(CustomObjectsDataSegmentsRepository customObjectsDataSegmentsRepository) {
        this.customObjectsDataSegmentsRepository = customObjectsDataSegmentsRepository;
    }

    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_DATA_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "Get all segments ")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<CustomObjectDataSegment> getAllSegments() {
        return customObjectsDataSegmentsRepository.getAllSegments();
    }

    @ApiOperation(value = "add segment")
    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_DATA_ADD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @RequestMapping(value = "", method = RequestMethod.POST)
    public CustomObjectDataSegment addDynamicSegment(@RequestBody CustomObjectDataSegment dynamicSegment, HttpServletResponse response) {
        if (dynamicSegment.isUsePagination()) {
            dynamicSegment.getCustomObjectsDataBuilder().setUsePagination(true);
        } else {
            dynamicSegment.getCustomObjectsDataBuilder().setUsePagination(false);
        }

        dynamicSegment = customObjectsDataSegmentsRepository.addSegment(dynamicSegment);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return dynamicSegment;
    }

    @ApiOperation(value = "update dynamic segment")
    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_DATA_EDIT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public CustomObjectDataSegment updateDynamicSegment(@RequestBody CustomObjectDataSegment dynamicSegment, HttpServletResponse response) {
        customObjectsDataSegmentsRepository.updateSegment(dynamicSegment);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return dynamicSegment;
    }

    @ApiOperation(value = "delete segment")
    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_DATA_DELETE, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void deleteSegment(@PathVariable("id") String id) {
        customObjectsDataSegmentsRepository.deleteSegment(id);
    }

}
