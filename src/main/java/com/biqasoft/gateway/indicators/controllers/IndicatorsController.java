/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.indicators.controllers;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.constants.DATE_CONSTS;
import com.biqasoft.entity.constants.SYSTEM_FIELDS_CONST;
import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.datasources.history.response.DataSourceIntegerAggregatedResponseFull;
import com.biqasoft.entity.datasources.history.response.LeadGenAggregatedResponseFull;
import com.biqasoft.entity.filters.CustomerFilter;
import com.biqasoft.entity.filters.DataSourceKPIsFilter;
import com.biqasoft.entity.filters.LeadGenKPIsFilter;
import com.biqasoft.entity.filters.PaymentDealsFilter;
import com.biqasoft.entity.indicators.dto.DateGroupedStatisticsListEntity;
import com.biqasoft.entity.indicators.dto.ManagerCreatedCustomersEntity;
import com.biqasoft.entity.indicators.dto.ManagerPaymentEntity;
import com.biqasoft.gateway.indicators.repositories.KPIsPaymentsRepository;
import com.biqasoft.gateway.indicators.repositories.KPIsRepository;
import com.biqasoft.gateway.indicators.statistics.BasicStatsDTO;
import com.biqasoft.gateway.indicators.statistics.DomainStatisticsRepository;
import com.biqasoft.gateway.tasks.repositories.TaskRepository;
import com.biqasoft.persistence.base.DateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "KPIs")
@Secured(value = {SYSTEM_ROLES.KPI_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/indicators")
public class IndicatorsController {

    private final TaskRepository taskRepository;
    private final KPIsRepository kpIsRepository;
    private final KPIsPaymentsRepository kpIsPaymentsRepository;
    private final DateService dateServiceRequestContext;
    private final DomainStatisticsRepository domainStatisticsRepository;

    @Autowired
    public IndicatorsController(DomainStatisticsRepository domainStatisticsRepository, DateService dateServiceRequestContext, TaskRepository taskRepository, KPIsRepository kpIsRepository, KPIsPaymentsRepository kpIsPaymentsRepository) {
        this.domainStatisticsRepository = domainStatisticsRepository;
        this.dateServiceRequestContext = dateServiceRequestContext;
        this.taskRepository = taskRepository;
        this.kpIsRepository = kpIsRepository;
        this.kpIsPaymentsRepository = kpIsPaymentsRepository;
    }

    @Secured(value = {SYSTEM_ROLES.KPI_LEAD_GEN_METHOD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get main KPIs of lead gen method or project")
    @RequestMapping(value = "filter/lead_gen_method/kpi", method = RequestMethod.POST)
    public LeadGenAggregatedResponseFull getAggregatedLeadGenKPIs(@RequestBody LeadGenKPIsFilter builder) {
        boolean isLeadGenMethod = false;
        boolean isLeadGenProject = false;
        String id = null;
        if (builder.getLeadGenMethodId() != null && !builder.getLeadGenMethodId().equals(SYSTEM_FIELDS_CONST.ANY)) {
            isLeadGenMethod = true;
            id = builder.getLeadGenMethodId();
        }

        if (builder.getLeadGenProjectId() != null && !builder.getLeadGenProjectId().equals(SYSTEM_FIELDS_CONST.ANY)) {
            isLeadGenProject = true;
            id = builder.getLeadGenProjectId();
        }

        if (isLeadGenMethod && isLeadGenProject) {
            ThrowExceptionHelper.throwExceptionInvalidRequest("you cant set method and project at the same time");
        }

        return kpIsRepository.getAggregatedLeadGenKPIs(isLeadGenMethod, isLeadGenProject, id,
                dateServiceRequestContext.parseDateExpression(builder.getStartDate()), dateServiceRequestContext.parseDateExpression(builder.getFinalDate()), builder);
    }

    @Secured(value = {SYSTEM_ROLES.KPI_LEAD_GEN_METHOD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get main KPIs of data source")
    @RequestMapping(value = "filter/data_source/kpi/integer", method = RequestMethod.POST)
    public DataSourceIntegerAggregatedResponseFull getAggregatedLeadGenKPIs(@RequestBody DataSourceKPIsFilter builder){

        if (builder.getDataSourceId() == null) {
            ThrowExceptionHelper.throwExceptionInvalidRequest("you cant set `dataSourceId` to null");
        }

        return kpIsRepository.getDataSourceHistoryDataForCurrentDomain(builder.getDataSourceId(),
                dateServiceRequestContext.parseDateExpression(builder.getStartDate()), dateServiceRequestContext.parseDateExpression(builder.getFinalDate()), builder);
    }

    @Secured(value = {SYSTEM_ROLES.KPI_TASK, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get main KPIs of tasks")
    @RequestMapping(value = "tasks/today", method = RequestMethod.GET)
    public List<DateGroupedStatisticsListEntity> getDateGroupedStatisticsListEntity() {
        return kpIsRepository.getGroupedTasksByDay(
                taskRepository.findAll(),
                dateServiceRequestContext.parseDateExpression(DATE_CONSTS.CURRENT_MONTH_START),
                dateServiceRequestContext.parseDateExpression(DATE_CONSTS.CURRENT_MONTH_END)
        );
    }

    @Secured(value = {SYSTEM_ROLES.KPI_SALES_MANAGER, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get created info of customer and leads by CustomerBuilder", notes = "no side affect")
    @RequestMapping(value = "manager/kpis/created_leads_and_customers", method = RequestMethod.POST)
    public List<ManagerCreatedCustomersEntity> getCustomerAndLeadsCreatedByManager(@RequestBody CustomerFilter customerCreatedBuilder) {
        return kpIsRepository.getCustomerAndLeadsCreatedByManager(customerCreatedBuilder);
    }

    @Secured(value = {SYSTEM_ROLES.KPI_SALES_MANAGER, SYSTEM_ROLES.ROLE_SELLER_MOTIVATION_DESK, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get payments by PaymentDealsBuilder. With motivation desk")
    @RequestMapping(value = "manager/payment/filter", method = RequestMethod.POST)
    public List<ManagerPaymentEntity> paymentDealsByManagers(@RequestBody PaymentDealsFilter paymentDealsBuilder) {
        return kpIsPaymentsRepository.getAllManagerPaymentDealsKPIs(paymentDealsBuilder);
    }

    @Secured(value = {SYSTEM_ROLES.STATS_DOMAIN_BASIC, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @RequestMapping(value = "basic_stats", method = RequestMethod.GET)
    public BasicStatsDTO getBasicStats() {
        return domainStatisticsRepository.getBasicStats();
    }

}
