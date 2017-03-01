/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.weather.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;


public class YahooWeatherItem implements Serializable {


    private String title;
    private String lat;

    @JsonProperty(value = "long")
    private String longItem;
    private String pubDate;


    private YahooWeatherCondition condition;

    private String description;

    private YahooWeatherForecast[] forecast;

    private YahooWeatherGuid guid;


    public YahooWeatherCondition getCondition() {
        return condition;
    }

    public void setCondition(YahooWeatherCondition condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public YahooWeatherForecast[] getForecast() {
        return forecast;
    }

    public void setForecast(YahooWeatherForecast[] forecast) {
        this.forecast = forecast;
    }

    public YahooWeatherGuid getGuid() {
        return guid;
    }

    public void setGuid(YahooWeatherGuid guid) {
        this.guid = guid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLongItem() {
        return longItem;
    }

    public void setLongItem(String longItem) {
        this.longItem = longItem;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }
}
