/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.datasources.controllers;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.datasources.DataSource;
import com.biqasoft.entity.datasources.SavedDataSource;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.filters.DataSourceFilter;
import com.biqasoft.gateway.datasources.repositories.DataSourceRepository;
import com.biqasoft.gateway.datasources.repositories.KPIsExcelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.biqasoft.entity.constants.SYSTEM_CONSTS.EXCEL_MIME_TYPE;

@Api(value = "Data Sources")
@Secured(value = {SYSTEM_ROLES.DATA_SOURCES_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/data_source")
public class DataSourceController {

    private final DataSourceRepository dataSourceAllData;
    private final KPIsExcelService kpIsExcelService;

    @Autowired
    public DataSourceController(DataSourceRepository dataSourceAllData, KPIsExcelService kpIsExcelService) {
        this.dataSourceAllData = dataSourceAllData;
        this.kpIsExcelService = kpIsExcelService;
    }

    @Secured(value = {SYSTEM_ROLES.DATA_SOURCES_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get data source")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public DataSource getResolvedDataSource(@PathVariable("id") String id) {
        return dataSourceAllData.findDataSourceSavedDataById(id);
    }

    @Secured(value = {SYSTEM_ROLES.DATA_SOURCES_DELETE, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "delete data source by ID")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void deleteDataSource(HttpServletResponse response, @PathVariable("id") String id) {
        dataSourceAllData.deleteDataSourceSavedData(dataSourceAllData.findDataSourceSavedDataById(id));
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Secured(value = {SYSTEM_ROLES.DATA_SOURCES_EDIT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "update data source")
    @RequestMapping(method = RequestMethod.PUT)
    public DataSource updateDataSource(@RequestBody DataSource data) {
        return dataSourceAllData.updateDataSourceSavedData(data);
    }

    @Secured(value = {SYSTEM_ROLES.DATA_SOURCES_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get all datasources", notes = "NOT RESOLVED DATA !")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<DataSource> getAllDataSources() {
        return dataSourceAllData.findAllDataSourceSavedData();
    }

    @Secured(value = {SYSTEM_ROLES.DATA_SOURCES_ADD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "add new data source")
    @RequestMapping(method = RequestMethod.POST)
    public DataSource addNewDataSource(@RequestBody DataSource lead, HttpServletResponse response) {
        dataSourceAllData.addNewDataSourceSavedData(lead);

        response.setStatus(HttpServletResponse.SC_CREATED);
        return lead;
    }

    @Secured(value = {SYSTEM_ROLES.DATA_SOURCES_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get data source by builder")
    @RequestMapping(value = "filter", method = RequestMethod.POST)
    public BiqaPaginationResultList<DataSource> getDataSourceByFilter(@RequestBody DataSourceFilter builder) {
        return dataSourceAllData.getDataSourceByFilter(builder);
    }

    @Secured(value = {SYSTEM_ROLES.DATA_SOURCES_ADD_METRIC_MANUALLY, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "manually add data source metric in timeline")
    @RequestMapping(value = "metrics/save_data_source_value", method = RequestMethod.POST)
    public SavedDataSource getDataSourceByFilter(@RequestBody SavedDataSource builder) {
        return dataSourceAllData.saveDataSourceMetric(builder);
    }

    @Secured(value = {SYSTEM_ROLES.DATA_SOURCES_EXCEL, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get kpis in excel by criteria (builder)",
            notes = ", if you have a lot of kpis, it can take up to 2 minutes")
    @RequestMapping(value = "filter/excel", method = RequestMethod.POST)
    public ResponseEntity<byte[]> getKPIsInEXCEL(@RequestBody DataSourceFilter dataSourceBuilder) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(EXCEL_MIME_TYPE));

        return new ResponseEntity<>(kpIsExcelService.getKPisInEXCEL(dataSourceBuilder), headers, HttpStatus.ACCEPTED);
    }

}
