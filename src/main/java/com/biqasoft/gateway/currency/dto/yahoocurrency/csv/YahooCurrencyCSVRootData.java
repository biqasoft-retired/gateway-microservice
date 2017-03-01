/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.dto.yahoocurrency.csv;


import java.io.Serializable;


public class YahooCurrencyCSVRootData implements Serializable {

    private YahooCurrencyCSVQuery query;


    public YahooCurrencyCSVQuery getQuery() {
        return query;
    }

    public void setQuery(YahooCurrencyCSVQuery query) {
        this.query = query;
    }
}
