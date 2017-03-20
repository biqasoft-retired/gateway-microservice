/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.system.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.GitProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Api(value = "Health server")
@RestController
@RequestMapping(value = "/")
public class RootController {

    private final String gitCommitId;
    private final String gitCommitDate;

    // url used by frontend to connect to async API
    private final String asyncGateway;

    @Autowired
    public RootController(GitProperties gitProperties, @Value("${biqa.urls.http.async}") String asyncGateway) {
        this.gitCommitId = gitProperties.getCommitId();
        this.gitCommitDate = gitProperties.getCommitTime().toString();
        this.asyncGateway = asyncGateway;
    }

    @ApiOperation(value = "test server health")
    @RequestMapping(method = RequestMethod.GET)
    public ServerHealth rootHealthCheck() {
        ServerHealth serverHealth = new ServerHealth();
        serverHealth.setStatus("OK");
        serverHealth.setServerVersion(gitCommitId);
        serverHealth.setVersionDate(gitCommitDate);
        serverHealth.setServerDate(new Date());
        serverHealth.setAsyncGateway(asyncGateway);
        return serverHealth;
    }

    class ServerHealth {

        private String status;
        private String serverVersion;
        private String versionDate;
        private Date serverDate;
        private String asyncGateway;

        public String getAsyncGateway() {
            return asyncGateway;
        }

        public void setAsyncGateway(String asyncGateway) {
            this.asyncGateway = asyncGateway;
        }

        public Date getServerDate() {
            return serverDate;
        }

        public void setServerDate(Date serverDate) {
            this.serverDate = serverDate;
        }

        public String getVersionDate() {
            return versionDate;
        }

        public void setVersionDate(String versionDate) {
            this.versionDate = versionDate;
        }

        public String getStatus() {
            return status;
        }

        public String getServerVersion() {
            return serverVersion;
        }

        public void setServerVersion(String serverVersion) {
            this.serverVersion = serverVersion;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

}




