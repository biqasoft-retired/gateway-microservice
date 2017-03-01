/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.weather.dto;


import java.io.Serializable;


public class YahooWeatherAstronomy implements Serializable {


    private String sunrise;
    private String sunset;

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }
}
