/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.weather.dto;


import java.io.Serializable;


public class YahooWeatherRootData implements Serializable {

    private YahooWeatherQuery query;

    public YahooWeatherQuery getQuery() {
        return query;
    }

    public void setQuery(YahooWeatherQuery query) {
        this.query = query;
    }
}
