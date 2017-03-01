/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.dto.yahoocurrency;


import java.io.Serializable;


public class YahooCurrencyResults implements Serializable {


    private YahooCurrencyList list;

    public YahooCurrencyList getList() {
        return list;
    }

    public void setList(YahooCurrencyList list) {
        this.list = list;
    }
}
