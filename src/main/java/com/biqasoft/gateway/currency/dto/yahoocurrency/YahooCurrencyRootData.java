/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.dto.yahoocurrency;


import java.io.Serializable;


public class YahooCurrencyRootData implements Serializable {

    private YahooCurrencyQuery query;


    public YahooCurrencyQuery getQuery() {
        return query;
    }

    public void setQuery(YahooCurrencyQuery query) {
        this.query = query;
    }
}
