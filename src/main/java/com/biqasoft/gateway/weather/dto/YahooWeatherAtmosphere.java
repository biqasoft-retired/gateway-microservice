/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.weather.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

import java.io.Serializable;


public class YahooWeatherAtmosphere implements Serializable {

    @Id
    private String id;

    @JsonProperty(value = "CampaignIDS")
    private int[] campaignIDS;

    @JsonProperty(value = "yahoocurrency")
    private String currency;


}
