/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.repositories;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

@Service
public class YahooCurrencyExchangeService {

    private final YahooCurrencyExchange yahooCurrencyExchange;

    public YahooCurrencyExchangeService(YahooCurrencyExchange yahooCurrencyExchange) {
        this.yahooCurrencyExchange = yahooCurrencyExchange;
    }

    public JsonNode getAllCurriencies() {
        return yahooCurrencyExchange.getAllCurrencies().get("query").get("results").get("list").get("resources");
    }

    public double fromTo(String from, String to) {
        return yahooCurrencyExchange.getExchangeRate(from, to).get("query").get("results").get("row").get("col1").asDouble();
    }

}
