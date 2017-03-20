/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.constants;

import com.biqasoft.entity.dto.httpresponse.SampleDataResponse;
import com.biqasoft.gateway.cloud.dto.DateResponseDTO;
import com.biqasoft.persistence.base.DateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Api(value = "System constants", description = "constants for using in requests and responses such as data sources types, roles, widget types, date and time formats etc")
@RequestMapping(value = "/v1/constants")
public class ConstantsController {

    private final ConstantsService constantsService;
    private final DateService dateServiceRequestContext;

    @Autowired
    public ConstantsController(ConstantsService constantsService, DateService dateServiceRequestContext) {
        this.constantsService = constantsService;
        this.dateServiceRequestContext = dateServiceRequestContext;
    }

    @ApiOperation(value = "get all constants")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Map<String, List<String>> getAllConstants() {
        return constantsService.getAllConstants();
    }

    @ApiOperation(value = "get constants by name; see /constants/ for names")
    @RequestMapping(value = "name/{name}", method = RequestMethod.GET)
    public List<String> getConstantsByName(@PathVariable("name") String name) {
        return constantsService.getConstantsByName(name);
    }

    @ApiOperation(value = "parse Date Expression")
    @RequestMapping(value = "parse_date/{date}", method = RequestMethod.GET)
    public DateResponseDTO parseDateExpression(@PathVariable("date") String date) {
        return new DateResponseDTO(dateServiceRequestContext.parseDateExpression(date));
    }

    @ApiOperation(value = "generate BSON object Id")
    @RequestMapping(value = "generate_id", method = RequestMethod.GET)
    public SampleDataResponse generateId() {
        return new SampleDataResponse(new ObjectId().toString());
    }

}
