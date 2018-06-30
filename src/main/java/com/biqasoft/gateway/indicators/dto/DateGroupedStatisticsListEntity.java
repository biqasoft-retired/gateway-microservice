/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.gateway.indicators.dto;

import lombok.Data;

@Data
public class DateGroupedStatisticsListEntity {

    private DateGrouped dateGrouped = new DateGrouped();
    private DateGroupedStatistics dateGroupedStatistics = new DateGroupedStatistics();

}
