/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.system.controller;

import com.biqasoft.audit.object.BiqaClassService;
import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.core.BaseClass;
import com.biqasoft.entity.core.Domain;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.audit.object.customfield.BiqaObjectsCustomFieldProcessorService;
import com.biqasoft.gateway.system.dto.DomainSettingsRequestDTO;
import com.biqasoft.microservice.common.MicroserviceDomain;
import com.biqasoft.microservice.common.MicroserviceDomainSettings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;

@Api(value = "Domain")
@Secured(value = {SystemRoles.DOMAIN_COMPANY_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/domain_settings")
public class DomainSettingsController {

    private final MicroserviceDomainSettings microserviceDomainSettings;
    private final BiqaObjectsCustomFieldProcessorService biqaObjectsCustomFieldProcessorService;
    private final MicroserviceDomain microserviceDomain;
    private final BiqaClassService biqaClassService;

    @Autowired
    public DomainSettingsController(MicroserviceDomainSettings microserviceDomainSettings, BiqaObjectsCustomFieldProcessorService biqaObjectsCustomFieldProcessorService, MicroserviceDomain microserviceDomain, BiqaClassService biqaClassService) {
        this.microserviceDomainSettings = microserviceDomainSettings;
        this.biqaObjectsCustomFieldProcessorService = biqaObjectsCustomFieldProcessorService;
        this.microserviceDomain = microserviceDomain;
        this.biqaClassService = biqaClassService;
    }

    @Secured(value = {SystemRoles.DOMAIN_COMPANY_GET_BASIC, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get my domain SETTINGS info")
    @RequestMapping(value = "my")
    public DomainSettings getMyDomainSettings() {
        DomainSettings domainSetting = microserviceDomainSettings.findDomainSetting();

        // we need to send all base entity(BaseClass) names to client, even they are empty
        Map<String, Class<? extends BaseClass>> classesExtendsBiqaAbstract = biqaClassService.getBiqaClasses();

        for (Map.Entry<String, Class<? extends BaseClass>> stringClassEntry : classesExtendsBiqaAbstract.entrySet()) {
            domainSetting.getDefaultCustomFields().putIfAbsent(stringClassEntry.getKey(), new ArrayList<>());
        }

        return domainSetting;
    }

    @Secured(value = {SystemRoles.DOMAIN_COMPANY_GET_BASIC, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get my domain MAIN info")
    @RequestMapping(value = "info")
    public Domain getMyPrivateDomainInfo() {
        return microserviceDomain.findDomain();
    }

    @Secured(value = {SystemRoles.DOMAIN_COMPANY_UPDATE_SETTINGS, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "update domain settings")
    @RequestMapping(method = RequestMethod.PUT)
    public DomainSettings updateDomainSettings(@RequestBody DomainSettingsRequestDTO domainSettingsRequest) {

        if (domainSettingsRequest.isUpdateCurrentAdditionalFields()) {
            biqaObjectsCustomFieldProcessorService.parseCustomFields(domainSettingsRequest.getDomainSettings());
        }

        return microserviceDomainSettings.updateDomainSettings(domainSettingsRequest.getDomainSettings());
    }

}
