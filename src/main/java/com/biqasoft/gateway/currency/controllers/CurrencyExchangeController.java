/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.controllers;

import com.biqasoft.gateway.currency.repositories.CurrencyExchangeResponseDto;
import com.biqasoft.gateway.currency.repositories.YahooCurrencyExchangeService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;

@Api(hidden = true)
@ApiIgnore
@RestController
@RequestMapping(value = "/v1/currency")
public class CurrencyExchangeController {

    private YahooCurrencyExchangeService yahooCurrencyExchangeService;

    @Autowired
    public CurrencyExchangeController(YahooCurrencyExchangeService yahooCurrencyExchangeService) {
        this.yahooCurrencyExchangeService = yahooCurrencyExchangeService;
    }

    @ApiOperation(value = "exchange USD to all other")
    @RequestMapping(value = "USD", method = RequestMethod.GET)
    public JsonNode convertUSDtoOtherCurrencies(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        return yahooCurrencyExchangeService.getAllCurriencies();
    }

    @RequestMapping(value = "{from}/{to}", method = RequestMethod.GET)
    public CurrencyExchangeResponseDto convertOneCurrencyToAnother(HttpServletResponse response, @PathVariable("from") String from, @PathVariable("to") String to) {
        response.setStatus(HttpServletResponse.SC_OK);
        return new CurrencyExchangeResponseDto(yahooCurrencyExchangeService.fromTo(from, to));
    }

}
