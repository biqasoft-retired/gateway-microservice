/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.datasources.repositories;

import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.datasources.DataSource;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.dto.export.excel.ExportKPIDTO;
import com.biqasoft.entity.filters.DataSourceFilter;
import com.biqasoft.gateway.customer.repositories.CustomerExcelRepository;
import com.biqasoft.gateway.export.MicroserviceExportExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Write KPIs to excel
 */
@Service
public class KPIsExcelService {

    private final DataSourceRepository dataSourceRepository;
    private static final Logger logger = LoggerFactory.getLogger(CustomerExcelRepository.class);
    private final MicroserviceExportExcel microserviceExport;
    private final CurrentUser currentUser;

    @Autowired
    public KPIsExcelService(DataSourceRepository dataSourceRepository, MicroserviceExportExcel microserviceExport, CurrentUser currentUser) {
        this.dataSourceRepository = dataSourceRepository;
        this.microserviceExport = microserviceExport;
        this.currentUser = currentUser;
    }

    public byte[] getKPisInEXCEL(DataSourceFilter dataSourceBuilder) {
        BiqaPaginationResultList<DataSource> resultList = dataSourceRepository.getDataSourceByFilter(dataSourceBuilder);

        ExportKPIDTO exportKPIDTO = new ExportKPIDTO();
        exportKPIDTO.setDataSourceFilter(dataSourceBuilder);
        exportKPIDTO.setEntityNumber(resultList.getEntityNumber());
        exportKPIDTO.setResultedObjects(resultList.getResultedObjects());
        exportKPIDTO.setDateFormat(currentUser.getDateFormat());

        return microserviceExport.getKPIInExcel(exportKPIDTO);
    }

}
