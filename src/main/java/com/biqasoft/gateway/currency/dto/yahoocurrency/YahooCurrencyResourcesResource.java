/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.dto.yahoocurrency;


import java.io.Serializable;


public class YahooCurrencyResourcesResource implements Serializable {


    private YahooCurrencyResourcesResourceField[] field;
    private String classname;


    public YahooCurrencyResourcesResourceField[] getField() {
        return field;
    }

    public void setField(YahooCurrencyResourcesResourceField[] field) {
        this.field = field;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }
}
