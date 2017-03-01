/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.weather.dto;


import java.io.Serializable;


public class YahooWeatherRss implements Serializable {


    private String version;
    private String geo;  //url
    private String yweather;


    private YahooWeatherChannel channel;


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }

    public String getYweather() {
        return yweather;
    }

    public void setYweather(String yweather) {
        this.yweather = yweather;
    }

    public YahooWeatherChannel getChannel() {
        return channel;
    }

    public void setChannel(YahooWeatherChannel channel) {
        this.channel = channel;
    }
}
