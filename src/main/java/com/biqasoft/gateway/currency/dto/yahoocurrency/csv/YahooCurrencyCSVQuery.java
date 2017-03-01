/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.dto.yahoocurrency.csv;


import java.io.Serializable;


public class YahooCurrencyCSVQuery implements Serializable {

    private YahooCurrencyCSVResults results;

    public YahooCurrencyCSVResults getResults() {
        return results;
    }

    public void setResults(YahooCurrencyCSVResults results) {
        this.results = results;
    }
}
