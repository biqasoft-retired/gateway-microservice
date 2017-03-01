/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.objects.custom.data;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.filters.CustomObjectsDataFilter;
import com.biqasoft.entity.core.objects.CustomObjectData;
import com.biqasoft.gateway.objects.custom.data.dto.PrintableDataContextSaver;
import com.biqasoft.gateway.objects.custom.data.dto.RequestPrintableBuilder;
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

@RestController
@Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_META_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@Api(value = "Custom objects")
@RequestMapping(value = "/v1/objects/custom/data")
public class CustomObjectsDataController {

    private final CustomObjectsDataRepository customObjectsRepository;
    private final CustomObjectDataExcelRepository customObjectDataExcelRepository;
    private final CustomObjectsDataPrintableService customObjectsDataPrintableService;

    @Autowired
    public CustomObjectsDataController(CustomObjectsDataRepository customObjectsRepository, CustomObjectDataExcelRepository customObjectDataExcelRepository, CustomObjectsDataPrintableService customObjectsDataPrintableService) {
        this.customObjectsRepository = customObjectsRepository;
        this.customObjectDataExcelRepository = customObjectDataExcelRepository;
        this.customObjectsDataPrintableService = customObjectsDataPrintableService;
    }

    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_META_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "Get all objects with pagination and filters")
    @RequestMapping(value = "filter", method = RequestMethod.POST)
    public  BiqaPaginationResultList<CustomObjectData> getCustomObjectTemplateByFilter(@RequestBody CustomObjectsDataFilter customerBuilder) {
        return customObjectsRepository.getCustomObjectTemplateFromFilter(customerBuilder);
    }

    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_META_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get one CustomObject by id ")
    @RequestMapping(value = "id/{id}/collection_id/{colId}", method = RequestMethod.GET)
    public  CustomObjectData getCustomObjectById(HttpServletResponse response, @PathVariable("id") String id, @PathVariable("colId") String colId) {
        return customObjectsRepository.findCustomObjectByIdAndCollectionId(id, colId);
    }

    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_META_EDIT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "update CustomObject ", notes = "full updates CustomObject or lead with all new data")
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public  CustomObjectData updateCustomObject(@RequestBody CustomObjectData customer, HttpServletResponse response) {
        return customObjectsRepository.updateCustomObject(customer);
    }

    @ApiOperation(value = "add new CustomObject")
    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_META_ADD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @RequestMapping(method = RequestMethod.POST)
    public  CustomObjectData addNewCustomObject(@RequestBody CustomObjectData customer, HttpServletResponse response) {
        customObjectsRepository.addCustomObjectBlank(customer);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return customer;
    }

    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_META_DELETE, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "delete one CustomObject by id ")
    @RequestMapping(value = "id/{id}/collection_id/{colId}", method = RequestMethod.DELETE)
    public  void deleteCustomObject(HttpServletResponse response, @PathVariable("id") String id, @PathVariable("colId") String colId) {
        customObjectsRepository.deleteCustomObjectWithIdAndCollectionId(id, colId);
    }

    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_META_GET_EXCEL, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "download custom objects in excel")
    @RequestMapping(value = "filter/excel", method = RequestMethod.POST)
    public  ResponseEntity<byte[]> geInEXCEL(@RequestBody CustomObjectsDataFilter builder) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(EXCEL_MIME_TYPE));

        ResponseEntity responseEntity = new ResponseEntity(customObjectDataExcelRepository.printExcel(builder), headers, HttpStatus.ACCEPTED);
        return responseEntity;
    }

    @Secured(value = {SYSTEM_ROLES.CUSTOM_OBJECT_META_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "download custom objects printable")
    @RequestMapping(value = "filter/printable", method = RequestMethod.POST)
    public ResponseEntity<byte[]>  getPrintable(@RequestBody RequestPrintableBuilder builder) {

        if (builder == null || builder.getCustomObjectsDataBuilder() == null || builder.getViewId() == null)
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("custom_object.print.no_builder");

        List<CustomObjectData> customObjectDataList = null;

        customObjectDataList = (List<CustomObjectData>) customObjectsRepository.getCustomObjectTemplateFromFilter(builder.getCustomObjectsDataBuilder()).getResultedObjects();

        if (customObjectDataList == null || customObjectDataList.size() == 0){
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("custom_object.print.no_elements");
        }

        if (customObjectDataList.size() > 1){
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("custom_object.print.allow_only_one");
        }

        CustomObjectData customObjectData = customObjectDataList.get(0);

        PrintableDataContextSaver printableDataContextSaver = customObjectsDataPrintableService.getPrintableData(customObjectData, builder);

        byte[] bytes = printableDataContextSaver.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(printableDataContextSaver.getRequestedMimeType()));

        ResponseEntity responseEntity = new ResponseEntity(bytes, headers, HttpStatus.ACCEPTED);
        return responseEntity;
    }

}
