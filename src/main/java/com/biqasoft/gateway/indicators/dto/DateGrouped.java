/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.gateway.indicators.dto;

import lombok.Data;

@Data
public class DateGrouped {

    private int minute;
    private int hour;
    private int day;
    private int week;
    private int month;
    private int year;
    private int dayOfYear;


}
