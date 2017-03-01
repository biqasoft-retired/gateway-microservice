/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.dto.yahoocurrency;


import java.io.Serializable;


public class YahooCurrencyQuery implements Serializable {


    private YahooCurrencyResults results;


    private int count;
    private String created;
    private String lang;

    public YahooCurrencyResults getResults() {
        return results;
    }

    public void setResults(YahooCurrencyResults results) {
        this.results = results;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
