/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.objects.custom.data;

import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.dto.export.excel.ExportCustomObjectDTO;
import com.biqasoft.entity.filters.CustomObjectsDataFilter;
import com.biqasoft.entity.core.objects.CustomObjectData;
import com.biqasoft.entity.objects.CustomObjectTemplate;
import com.biqasoft.gateway.customer.repositories.CustomerExcelRepository;
import com.biqasoft.gateway.export.MicroserviceExportExcel;
import com.biqasoft.gateway.objects.custom.template.CustomObjectsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 1/29/2016.
 * All Rights Reserved
 */
@Service
public class CustomObjectDataExcelRepository {

    private final CustomObjectsDataRepository customObjectsDataRepository;
    private final CustomObjectsRepository customObjectsRepository;
    private static final Logger logger = LoggerFactory.getLogger(CustomerExcelRepository.class);
    private final MicroserviceExportExcel microserviceExport;
    private final CurrentUser currentUser;

    @Autowired
    public CustomObjectDataExcelRepository(CustomObjectsRepository customObjectsRepository, CustomObjectsDataRepository customObjectsDataRepository,
                                           MicroserviceExportExcel microserviceExport, CurrentUser currentUser) {
        this.customObjectsRepository = customObjectsRepository;
        this.customObjectsDataRepository = customObjectsDataRepository;
        this.microserviceExport = microserviceExport;
        this.currentUser = currentUser;
    }

    public byte[] printExcel(CustomObjectsDataFilter filter) {
        CustomObjectTemplate template = customObjectsRepository.findCustomObjectById(filter.getCollectionId());
        return printExcel(template, filter);
    }

    public byte[] printExcel(CustomObjectTemplate customObjectTemplate, CustomObjectsDataFilter filter) {
        List<CustomObjectData> list = customObjectsDataRepository.getCustomObjectTemplateFromFilter(filter).getResultedObjects();

        ExportCustomObjectDTO requestPayload = new ExportCustomObjectDTO();
        requestPayload.setBuilder(filter);
        requestPayload.setList(list);
        requestPayload.setCustomObjectTemplate(customObjectTemplate);
        requestPayload.setDateFormat(currentUser.getDateFormat());

        return microserviceExport.getCustomObjectInExcel(requestPayload);
    }

}
