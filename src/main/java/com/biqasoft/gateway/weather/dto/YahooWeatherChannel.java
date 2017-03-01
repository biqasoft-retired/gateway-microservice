/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.weather.dto;


import java.io.Serializable;


public class YahooWeatherChannel implements Serializable {

    private String title;
    private String link;
    private String description;

    private String language;
    private String lastBuildDate;
    private String ttl;
    private YahooWeatherLocation location;
    private YahooWeatherUnits units;
    private YahooWeatherWind wind;
    private YahooWeatherAtmosphere atmosphere;
    private YahooWeatherAstronomy astronomy;
    private YahooWeatherImage image;
    private YahooWeatherItem item;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLastBuildDate() {
        return lastBuildDate;
    }

    public void setLastBuildDate(String lastBuildDate) {
        this.lastBuildDate = lastBuildDate;
    }

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

    public YahooWeatherLocation getLocation() {
        return location;
    }

    public void setLocation(YahooWeatherLocation location) {
        this.location = location;
    }

    public YahooWeatherUnits getUnits() {
        return units;
    }

    public void setUnits(YahooWeatherUnits units) {
        this.units = units;
    }

    public YahooWeatherWind getWind() {
        return wind;
    }

    public void setWind(YahooWeatherWind wind) {
        this.wind = wind;
    }

    public YahooWeatherAtmosphere getAtmosphere() {
        return atmosphere;
    }

    public void setAtmosphere(YahooWeatherAtmosphere atmosphere) {
        this.atmosphere = atmosphere;
    }

    public YahooWeatherAstronomy getAstronomy() {
        return astronomy;
    }

    public void setAstronomy(YahooWeatherAstronomy astronomy) {
        this.astronomy = astronomy;
    }

    public YahooWeatherImage getImage() {
        return image;
    }

    public void setImage(YahooWeatherImage image) {
        this.image = image;
    }

    public YahooWeatherItem getItem() {
        return item;
    }

    public void setItem(YahooWeatherItem item) {
        this.item = item;
    }
}
