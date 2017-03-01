/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.objects.custom.template;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.core.objects.CustomObjectData;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.filters.CustomObjectsFilter;
import com.biqasoft.entity.objects.CustomObjectTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@Api(value = "Custom objects metadata")
@RequestMapping(value = "/v1/objects/custom/metadata")
public class CustomObjectsController {

    private final CustomObjectsRepository customObjectsRepository;

    @Autowired
    public CustomObjectsController(CustomObjectsRepository customObjectsRepository) {
        this.customObjectsRepository = customObjectsRepository;
    }

    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "Get all CustomObject")
    @RequestMapping(method = RequestMethod.GET)
    public List<CustomObjectTemplate> getAllCustomObjects() {
        return customObjectsRepository.findAll();
    }

    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "Get all objects with pagination and filters")
    @RequestMapping(value = "filter", method = RequestMethod.POST)
    public BiqaPaginationResultList<CustomObjectTemplate> getCustomObjectTemplateByFilter(@RequestBody CustomObjectsFilter customerBuilder) {
        return customObjectsRepository.getCustomObjectTemplateFromBuilder(customerBuilder);
    }

    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get one CustomObject by id ")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public CustomObjectTemplate getCustomObjectById(@PathVariable("id") String id) {
        return customObjectsRepository.findCustomObjectById(id);
    }

    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_EDIT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "update CustomObject ", notes = "full updates CustomObject or lead with all new data")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public CustomObjectTemplate updateCustomObject(@RequestBody CustomObjectTemplate customer) {
        return customObjectsRepository.updateCustomObject(customer);
    }

    @ApiOperation(value = "add new CustomObject")
    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_ADD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @RequestMapping(method = RequestMethod.POST)
    public CustomObjectTemplate addNewCustomObject(@RequestBody CustomObjectTemplate customer, HttpServletResponse response) {
        customObjectsRepository.addCustomObject(customer);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return customer;
    }

    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_DELETE, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "delete one CustomObject by id ")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void deleteCustomObject(@PathVariable("id") String id) {
        customObjectsRepository.deleteCustomObject(id);
    }

}
