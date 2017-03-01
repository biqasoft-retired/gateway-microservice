/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.weather.dto;


import java.io.Serializable;


public class YahooWeatherGuid implements Serializable {


    private String isPermaLink;
    private String content;


    public String getIsPermaLink() {
        return isPermaLink;
    }

    public void setIsPermaLink(String isPermaLink) {
        this.isPermaLink = isPermaLink;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
