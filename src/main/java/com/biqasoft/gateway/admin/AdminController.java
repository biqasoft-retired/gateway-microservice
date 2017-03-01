/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.admin;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.gateway.admin.dto.ExecuteDatabaseCommandRequestDTO;
import com.biqasoft.gateway.admin.dto.ExecuteDatabaseCommandResultDTO;
import com.biqasoft.gateway.system.dto.DataBaseCredentialsDao;
import com.mongodb.CommandResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * Domain admin functions. for developers.
 * Public API
 */
@Api(value = "Domain admin", description = " High potential security endpoint. Allowed only for admin in domain.")
@Secured(value = {SYSTEM_ROLES.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/admin")
public class AdminController {

    private final SystemActionsService systemActionsRepository;

    @Autowired
    public AdminController(final SystemActionsService systemActionsRepository) {
        this.systemActionsRepository = systemActionsRepository;
    }

    @ApiOperation(value = "create new credentials for database in current domain")
    @RequestMapping(value = "database/credentials", method = RequestMethod.POST)
    public
    DataBaseCredentialsDao getDatabaseCredentials(@RequestBody DataBaseCredentialsDao dataBaseCredentialsDao, HttpServletResponse response) {
        systemActionsRepository.createNewDataBaseUser(dataBaseCredentialsDao);
        return dataBaseCredentialsDao;
    }

    @ApiOperation(value = "get users in database")
    @RequestMapping(value = "database/users", method = RequestMethod.GET)
    public
    CommandResult getAllUsersInDomainDataBase(HttpServletResponse response) {
        return systemActionsRepository.getAllUsersInDomainDataBase();
    }

    @ApiOperation(value = "delete user in database")
    @RequestMapping(value = "database/users/{id}", method = RequestMethod.DELETE)
    public
    CommandResult dropUserInDataBase(HttpServletResponse response, @PathVariable("id") String id) {
        return systemActionsRepository.dropUserInDomainDataBase(id);
    }

    @ApiOperation(value = "execute mongodb command for database in current domain")
    @RequestMapping(value = "database/command/execute", method = RequestMethod.POST)
    public
    ExecuteDatabaseCommandResultDTO executeDatabaseCommand(@RequestBody ExecuteDatabaseCommandRequestDTO dataBaseCredentialsDao, HttpServletResponse response) {
        return systemActionsRepository.executeDatabaseCommandAsUserAdmin(dataBaseCredentialsDao);
    }

}
