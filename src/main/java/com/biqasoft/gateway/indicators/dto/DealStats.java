/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.gateway.indicators.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DealStats implements Serializable {

    @ApiModelProperty(notes = "ID of useraccount created deal")
    @Indexed
    private String responsibleManagerID;

    private String customerDealID;
    private BigDecimal customerDealAmount;

    private long customerDealCycle;

}
