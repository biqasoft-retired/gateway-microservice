/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.currency.repositories;


public class CurrencyExchangeResponseDto{

    private double exchangeRate;

    public CurrencyExchangeResponseDto() {
    }

    public CurrencyExchangeResponseDto(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
}
