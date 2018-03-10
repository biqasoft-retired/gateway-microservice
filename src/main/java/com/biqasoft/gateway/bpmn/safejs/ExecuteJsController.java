/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.bpmn.safejs;

import com.biqasoft.bpmn.safejs.ExecutorCodeService;
import com.biqasoft.bpmn.safejs.entity.ExecuteJsRequest;
import com.biqasoft.bpmn.safejs.entity.ExecuteJsResponse;
import com.biqasoft.entity.constants.SystemRoles;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.Map;

@Api(value = "BPMN")
@ApiIgnore
@RestController
@Secured(value = {SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@RequestMapping(value = "/v1/bpmn/safejs")
public class ExecuteJsController {

    private final ExecutorCodeService executorCodeService;

    @Autowired
    public ExecuteJsController(final ExecutorCodeService executorCodeService) {
        this.executorCodeService = executorCodeService;
    }

    @Secured(value = {SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "")
    @RequestMapping(value = "execute", method = RequestMethod.POST)
    public ExecuteJsResponse jsCode(@RequestBody ExecuteJsRequest executeJsRequest) {
        return executorCodeService.executeCode(executeJsRequest);
    }

    @Secured(value = {SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "execute/raw")
    @RequestMapping(value = "execute/raw", method = RequestMethod.POST)
    public ExecuteJsResponse executeRawCode(@RequestBody String code) {
        ExecuteJsRequest executeJsRequest = new ExecuteJsRequest();
        executeJsRequest.setJsCode(code);

        Map<String, Object> stringObjectMap = new HashMap<>();
        return executorCodeService.executeCode(executeJsRequest, stringObjectMap);
    }

}
