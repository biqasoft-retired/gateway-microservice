/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.indicators.statistics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document
@ApiModel("Object, representing summary info about all account (domain)")
public class BasicStatsDTO {

    @ApiModelProperty("Number of all account")
    private long allStaffCount;

    @ApiModelProperty("Number of all acive account")
    private long allActiveStaffCount;

    @ApiModelProperty("Number of all leads")
    private long allLeadsCount;

    @ApiModelProperty("Number of all active leads")
    private long activeLeadsNumber;

    @ApiModelProperty("Number of all customers")
    private long allCustomersCount;

    @ApiModelProperty("Number of all deals")
    private long allDealsCount;

    @ApiModelProperty("Amount of all deals")
    private BigDecimal allDealsAmount;

    @ApiModelProperty("Number of all opportunities")
    private long allOpportunitiesCount;

    @ApiModelProperty("Amount of all opportunities")
    private BigDecimal allOpportunitiesAmount;

    @ApiModelProperty("Number of all summarised costs")
    private long allCompanyCostCount;

    @ApiModelProperty("Amount of all costs")
    private BigDecimal allCompanyCostAmount;

    @ApiModelProperty("Number of all active tasks")
    private long allActiveTasksCount;

    @ApiModelProperty("All tasks number, included completed")
    private long allTasksCount;

    @ApiModelProperty("Number of active customer")
    private long activeCustomersNumber;

    @ApiModelProperty("Number of active customer where current user is responsible")
    private long currentUserResponsibleAndActiveCustomersNumber;

    @ApiModelProperty("Number of companies (contragent)")
    private long companiesNumber;

    @ApiModelProperty("Number of static segment")
    private long staticSegmentsNumber;

    @ApiModelProperty("Number of dynamic segment")
    private long dynamicSegmentsNumber;

    @ApiModelProperty("Number of documents (files) ")
    private long documentsNumber;

    @ApiModelProperty("Number of cold calls ")
    private long coldCallProjectNumber;

    @ApiModelProperty("Number of web counters (Web SDK) ")
    private long webCounterNumber;

    @ApiModelProperty("Number of leadGenMethods ")
    private long leadGenMethodsNumber;

    @ApiModelProperty("Number of leadGenProjects ")
    private long leadGenProjectsNumber;

    @ApiModelProperty("Number of data sources ")
    private long dataSourcesNumber;

}
