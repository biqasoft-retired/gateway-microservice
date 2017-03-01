/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.dto.yahoocurrency.csv;


import java.io.Serializable;


public class YahooCurrencyCSVRow implements Serializable {
//    "col0": "RUB",
//            "col1": "45.9965"

    private String col0;
    private String col1;


    public String getCol0() {
        return col0;
    }

    public void setCol0(String col0) {
        this.col0 = col0;
    }

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }
}
