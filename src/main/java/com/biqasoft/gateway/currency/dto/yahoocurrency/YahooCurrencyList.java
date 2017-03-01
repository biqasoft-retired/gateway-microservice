/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.dto.yahoocurrency;


import java.io.Serializable;


public class YahooCurrencyList implements Serializable {


    private YahooCurrencyResources resources;
    private YahooCurrencyMeta meta;
    private String version;


    public YahooCurrencyResources getResources() {
        return resources;
    }

    public void setResources(YahooCurrencyResources resources) {
        this.resources = resources;
    }

    public YahooCurrencyMeta getMeta() {
        return meta;
    }

    public void setMeta(YahooCurrencyMeta meta) {
        this.meta = meta;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
