/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.dto.yahoocurrency;


import java.io.Serializable;


public class YahooCurrencyResources implements Serializable {


    private int count;
    private int start;
    private YahooCurrencyResourcesResource[] resource;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public YahooCurrencyResourcesResource[] getResource() {
        return resource;
    }

    public void setResource(YahooCurrencyResourcesResource[] resource) {
        this.resource = resource;
    }
}
