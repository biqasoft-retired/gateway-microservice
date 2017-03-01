/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.leadgen.controllers;

import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.customer.LeadGenMethod;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.dto.export.excel.ExportLeadGenMethodDTO;
import com.biqasoft.entity.dto.export.excel.ExportLeadGenMethodWithProjects;
import com.biqasoft.entity.filters.LeadGenMethodExcelFilter;
import com.biqasoft.entity.filters.LeadGenProjectFilter;
import com.biqasoft.gateway.customer.repositories.CustomerExcelRepository;
import com.biqasoft.gateway.export.MicroserviceExport;
import com.biqasoft.gateway.leadgen.repositories.LeadGenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 3/16/2016.
 * All Rights Reserved
 */
@Service
public class LeadGenMethodExcelReport {

    private static final Logger logger = LoggerFactory.getLogger(CustomerExcelRepository.class);
    private final LeadGenRepository leadGenRepository;
    private final MicroserviceExport microserviceExport;
    private final CurrentUser currentUser;

    @Autowired
    public LeadGenMethodExcelReport(LeadGenRepository leadGenRepository, MicroserviceExport microserviceExport, CurrentUser currentUser) {
        this.leadGenRepository = leadGenRepository;
        this.microserviceExport = microserviceExport;
        this.currentUser = currentUser;
    }

    public byte[] getResponseEntity(LeadGenMethodExcelFilter leadGenMethodExcelFilter) {
        BiqaPaginationResultList<LeadGenMethod> leadBiqaPaginationResultList = leadGenRepository.getLeadGenMethodByFilter(leadGenMethodExcelFilter);

        ExportLeadGenMethodDTO exportLeadGenMethodDTO = new ExportLeadGenMethodDTO();
        exportLeadGenMethodDTO.setResultedObjects(new ArrayList<>());
        exportLeadGenMethodDTO.setLeadGenMethodBuilder(leadGenMethodExcelFilter);
        exportLeadGenMethodDTO.setDateFormat(currentUser.getDateFormat());

        List<LeadGenMethod> leadGenMethods = leadBiqaPaginationResultList.getResultedObjects();

        for (LeadGenMethod leadGenMethod : leadGenMethods){
            ExportLeadGenMethodWithProjects exportLeadGenMethodWithProjects = new ExportLeadGenMethodWithProjects();
            exportLeadGenMethodWithProjects.setLeadGenMethod(leadGenMethod);

            LeadGenProjectFilter leadGenProjectFilter = new LeadGenProjectFilter();
            leadGenProjectFilter.setLeadGenMethodID(leadGenMethod.getId());

            exportLeadGenMethodWithProjects.setLeadGenProjects(leadGenRepository.getLeadGeProjectByFilter(leadGenProjectFilter).getResultedObjects());
            exportLeadGenMethodDTO.getResultedObjects().add(exportLeadGenMethodWithProjects);
        }

        return microserviceExport.getLeadGenInExcel(exportLeadGenMethodDTO);
    }

}
