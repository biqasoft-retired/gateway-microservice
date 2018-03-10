/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.admin;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.gateway.admin.dto.ExecuteDatabaseCommandRequestDTO;
import com.biqasoft.gateway.admin.dto.ExecuteDatabaseCommandResultDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * System administration use only
 * PRIVATE API
 * YOU CAN CORRUPT DATA
 */
@Api(value = "System administration use only", description = " VERY secured endpoint", hidden = true)
@ApiIgnore
@Secured(value = {SystemRoles.ROOT_USER})
@RestController
@RequestMapping(value = "/v1/root")
public class RootAdminController {

    private final SystemActionsService systemActionsRepository;

    @Autowired
    public RootAdminController(final SystemActionsService systemActionsRepository) {
        this.systemActionsRepository = systemActionsRepository;
    }

    @ApiOperation(value = "execute mongodb command on any database. BE VERY CAREFUL. YOU CAN DAMAGE ALL DATA")
    @RequestMapping(value = "database/{databaseId}/command/execute", method = RequestMethod.POST)
    public
    ExecuteDatabaseCommandResultDTO executeDatabaseCommandAsRootUser(@RequestBody ExecuteDatabaseCommandRequestDTO dataBaseCredentialsDao,
                                                                     @PathVariable("databaseId") String databaseId) {
        return systemActionsRepository.executeDatabaseCommandAsRootUser(dataBaseCredentialsDao, databaseId);
    }

}
