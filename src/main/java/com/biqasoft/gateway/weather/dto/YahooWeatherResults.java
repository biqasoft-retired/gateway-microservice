/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.weather.dto;


import java.io.Serializable;


public class YahooWeatherResults implements Serializable {

    private YahooWeatherRss rss;


    public YahooWeatherRss getRss() {
        return rss;
    }

    public void setRss(YahooWeatherRss rss) {
        this.rss = rss;
    }
}
