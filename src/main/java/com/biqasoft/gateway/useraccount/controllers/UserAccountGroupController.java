/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.useraccount.controllers;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.core.useraccount.UserAccountGroup;
import com.biqasoft.gateway.useraccount.MicroserviceUserAccountGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "User Accounts Groups")
@Secured(value = {SystemRoles.USER_GROUP_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/account/groups")
public class UserAccountGroupController {

    private final MicroserviceUserAccountGroup microserviceUserAccountGroup;

    @Autowired
    public UserAccountGroupController(MicroserviceUserAccountGroup microserviceUserAccountGroup) {
        this.microserviceUserAccountGroup = microserviceUserAccountGroup;
    }

    @Secured(value = {SystemRoles.USER_GROUP_GET, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all users groups in current domain")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<UserAccountGroup> getAllUserAccountGroups() {
        return microserviceUserAccountGroup.findAll();
    }

    @Secured(value = {SystemRoles.USER_GROUP_GET, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get group by id")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public UserAccountGroup getUserAccountGroupById(@PathVariable("id") String id) {
        return microserviceUserAccountGroup.findById(id);
    }

    @Secured(value = {SystemRoles.USER_GROUP_EDIT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "update user account")
    @RequestMapping(method = RequestMethod.PUT)
    public UserAccountGroup updateUserAccountGroup(@RequestBody UserAccountGroup accountGroup) {
        microserviceUserAccountGroup.update(accountGroup);
        return accountGroup;
    }

    @Secured(value = {SystemRoles.USER_GROUP_ADD, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "create user account")
    @RequestMapping(method = RequestMethod.POST)
    public UserAccountGroup addNewUserAccountGroup(@RequestBody UserAccountGroup accountGroup) {
        microserviceUserAccountGroup.create(accountGroup);
        return accountGroup;
    }

    @Secured(value = {SystemRoles.USER_GROUP_DELETE, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "delete group by id")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void deleteUserAccountGroupById(@PathVariable("id") String id) {
        microserviceUserAccountGroup.delete(id);
    }

}
