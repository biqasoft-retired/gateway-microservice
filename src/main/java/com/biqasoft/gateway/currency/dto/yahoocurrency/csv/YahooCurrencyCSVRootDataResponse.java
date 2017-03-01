/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.dto.yahoocurrency.csv;


import java.io.Serializable;


public class YahooCurrencyCSVRootDataResponse implements Serializable {

    private float exchangeRate;

    public float getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(float exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
}
