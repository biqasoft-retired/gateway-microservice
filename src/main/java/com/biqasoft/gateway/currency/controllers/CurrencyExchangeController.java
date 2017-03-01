/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.controllers;

import com.biqasoft.gateway.currency.dto.yahoocurrency.YahooCurrencyResources;
import com.biqasoft.gateway.currency.dto.yahoocurrency.csv.YahooCurrencyCSVRootDataResponse;
import com.biqasoft.gateway.currency.repositories.YahooCurrencyExchangeRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;

@Api(hidden = true)
@ApiIgnore
@RestController
@RequestMapping(value = "/v1/currency")
public class CurrencyExchangeController {

    private YahooCurrencyExchangeRepository yahooCurrencyExchangeRepository;

    @Autowired
    public CurrencyExchangeController(YahooCurrencyExchangeRepository yahooCurrencyExchangeRepository) {
        this.yahooCurrencyExchangeRepository = yahooCurrencyExchangeRepository;
    }

    // here only USD TO
    @RequestMapping(value = "USD", method = RequestMethod.GET)
    public YahooCurrencyResources convertUSDtoOtherCurrencies(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        return yahooCurrencyExchangeRepository.getAllCurriencies();
    }

    @RequestMapping(value = "{from}/{to}", method = RequestMethod.GET)
    public YahooCurrencyCSVRootDataResponse convertOneCurrencyToAnother(HttpServletResponse response, @PathVariable("from") String from, @PathVariable("to") String to) {
        response.setStatus(HttpServletResponse.SC_OK);

        YahooCurrencyCSVRootDataResponse resule = new YahooCurrencyCSVRootDataResponse();
        resule.setExchangeRate(yahooCurrencyExchangeRepository.fromTo(from, to));

        return resule;
    }

}
