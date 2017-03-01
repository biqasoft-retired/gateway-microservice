/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.useraccount.controllers;

import com.biqasoft.microservice.common.MicroserviceOAuth2Applications;
import com.biqasoft.microservice.common.dto.UserAccountOAuth2;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(value = "oauth2")
@RestController
@RequestMapping(value = "/oauth")
public class ObtainOAuth2CodeController {

    private final MicroserviceOAuth2Applications microserviceOAuth2Applications;

    @Autowired
    public ObtainOAuth2CodeController(MicroserviceOAuth2Applications microserviceOAuth2Applications) {
        this.microserviceOAuth2Applications = microserviceOAuth2Applications;
    }

    @ApiOperation(value = "obtain_access_code",
            notes = "in response you will get `userName` - this is username in basic auth and `accessToken` - this is password in basic auth. " +
                    "With this username and password you will eligible to make request with roles regards field `roles`." +
                    " Your must send JSON object will following fields {id : YOUR_APPLICATION_ID, secretCode: SECRET_CODE_OF_YOUR_APPLICATION }" +
                    " in URL, you should send code, for example `obtain_access_code/12fe34rew5` ")
    @RequestMapping(value = "obtain_access_code/{code}", method = RequestMethod.POST)
    public UserAccountOAuth2 obtainAccessCode(@RequestBody ObtainAccessCodeRequest request, @PathVariable("code") String code){
        return microserviceOAuth2Applications.obtainAccessCode(request.getId(), code, request.getSecretCode() );
    }

}

class ObtainAccessCodeRequest {

    private String id;
    private String secretCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }
}
