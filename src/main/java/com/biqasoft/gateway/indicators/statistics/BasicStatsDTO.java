/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.indicators.statistics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;

@Document
@ApiModel("Object, representing summary info about all account (domain)")
public class BasicStatsDTO implements Serializable {

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


    public long getAllStaffCount() {
        return allStaffCount;
    }

    public void setAllStaffCount(long allStaffCount) {
        this.allStaffCount = allStaffCount;
    }

    public long getAllActiveStaffCount() {
        return allActiveStaffCount;
    }

    public void setAllActiveStaffCount(long allActiveStaffCount) {
        this.allActiveStaffCount = allActiveStaffCount;
    }

    public long getAllLeadsCount() {
        return allLeadsCount;
    }

    public void setAllLeadsCount(long allLeadsCount) {
        this.allLeadsCount = allLeadsCount;
    }

    public long getActiveLeadsNumber() {
        return activeLeadsNumber;
    }

    public void setActiveLeadsNumber(long activeLeadsNumber) {
        this.activeLeadsNumber = activeLeadsNumber;
    }

    public long getAllCustomersCount() {
        return allCustomersCount;
    }

    public void setAllCustomersCount(long allCustomersCount) {
        this.allCustomersCount = allCustomersCount;
    }

    public long getAllDealsCount() {
        return allDealsCount;
    }

    public void setAllDealsCount(long allDealsCount) {
        this.allDealsCount = allDealsCount;
    }

    public BigDecimal getAllDealsAmount() {
        return allDealsAmount;
    }

    public void setAllDealsAmount(BigDecimal allDealsAmount) {
        this.allDealsAmount = allDealsAmount;
    }

    public long getAllOpportunitiesCount() {
        return allOpportunitiesCount;
    }

    public void setAllOpportunitiesCount(long allOpportunitiesCount) {
        this.allOpportunitiesCount = allOpportunitiesCount;
    }

    public BigDecimal getAllOpportunitiesAmount() {
        return allOpportunitiesAmount;
    }

    public void setAllOpportunitiesAmount(BigDecimal allOpportunitiesAmount) {
        this.allOpportunitiesAmount = allOpportunitiesAmount;
    }

    public long getAllCompanyCostCount() {
        return allCompanyCostCount;
    }

    public void setAllCompanyCostCount(long allCompanyCostCount) {
        this.allCompanyCostCount = allCompanyCostCount;
    }

    public BigDecimal getAllCompanyCostAmount() {
        return allCompanyCostAmount;
    }

    public void setAllCompanyCostAmount(BigDecimal allCompanyCostAmount) {
        this.allCompanyCostAmount = allCompanyCostAmount;
    }

    public long getAllActiveTasksCount() {
        return allActiveTasksCount;
    }

    public void setAllActiveTasksCount(long allActiveTasksCount) {
        this.allActiveTasksCount = allActiveTasksCount;
    }

    public long getAllTasksCount() {
        return allTasksCount;
    }

    public void setAllTasksCount(long allTasksCount) {
        this.allTasksCount = allTasksCount;
    }

    public long getActiveCustomersNumber() {
        return activeCustomersNumber;
    }

    public void setActiveCustomersNumber(long activeCustomersNumber) {
        this.activeCustomersNumber = activeCustomersNumber;
    }

    public long getCurrentUserResponsibleAndActiveCustomersNumber() {
        return currentUserResponsibleAndActiveCustomersNumber;
    }

    public void setCurrentUserResponsibleAndActiveCustomersNumber(long currentUserResponsibleAndActiveCustomersNumber) {
        this.currentUserResponsibleAndActiveCustomersNumber = currentUserResponsibleAndActiveCustomersNumber;
    }

    public long getCompaniesNumber() {
        return companiesNumber;
    }

    public void setCompaniesNumber(long companiesNumber) {
        this.companiesNumber = companiesNumber;
    }

    public long getStaticSegmentsNumber() {
        return staticSegmentsNumber;
    }

    public void setStaticSegmentsNumber(long staticSegmentsNumber) {
        this.staticSegmentsNumber = staticSegmentsNumber;
    }

    public long getDynamicSegmentsNumber() {
        return dynamicSegmentsNumber;
    }

    public void setDynamicSegmentsNumber(long dynamicSegmentsNumber) {
        this.dynamicSegmentsNumber = dynamicSegmentsNumber;
    }

    public long getDocumentsNumber() {
        return documentsNumber;
    }

    public void setDocumentsNumber(long documentsNumber) {
        this.documentsNumber = documentsNumber;
    }

    public long getColdCallProjectNumber() {
        return coldCallProjectNumber;
    }

    public void setColdCallProjectNumber(long coldCallProjectNumber) {
        this.coldCallProjectNumber = coldCallProjectNumber;
    }

    public long getWebCounterNumber() {
        return webCounterNumber;
    }

    public void setWebCounterNumber(long webCounterNumber) {
        this.webCounterNumber = webCounterNumber;
    }

    public long getLeadGenMethodsNumber() {
        return leadGenMethodsNumber;
    }

    public void setLeadGenMethodsNumber(long leadGenMethodsNumber) {
        this.leadGenMethodsNumber = leadGenMethodsNumber;
    }

    public long getLeadGenProjectsNumber() {
        return leadGenProjectsNumber;
    }

    public void setLeadGenProjectsNumber(long leadGenProjectsNumber) {
        this.leadGenProjectsNumber = leadGenProjectsNumber;
    }

    public long getDataSourcesNumber() {
        return dataSourcesNumber;
    }

    public void setDataSourcesNumber(long dataSourcesNumber) {
        this.dataSourcesNumber = dataSourcesNumber;
    }
}
