/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.dto.yahoocurrency.csv;


import java.io.Serializable;


public class YahooCurrencyCSVResults implements Serializable {


    private YahooCurrencyCSVRow row;

    public YahooCurrencyCSVRow getRow() {
        return row;
    }

    public void setRow(YahooCurrencyCSVRow row) {
        this.row = row;
    }
}
