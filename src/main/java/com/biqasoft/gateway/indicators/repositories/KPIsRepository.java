/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.indicators.repositories;

import com.biqasoft.auth.CurrentUserContextProvider;
import com.biqasoft.auth.core.UserAccount;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.datasources.IndicatorsDTO;
import com.biqasoft.entity.datasources.SavedDataSource;
import com.biqasoft.entity.datasources.SavedLeadGenKPI;
import com.biqasoft.entity.datasources.history.response.DataSourceAggregatedResponse;
import com.biqasoft.entity.datasources.history.response.DataSourceIntegerAggregatedResponse;
import com.biqasoft.entity.datasources.history.response.DataSourceIntegerAggregatedResponseFull;
import com.biqasoft.entity.datasources.history.response.LeadGenAggregatedResponseFull;
import com.biqasoft.entity.filters.CustomerFilter;
import com.biqasoft.entity.filters.DataSourceKPIsFilter;
import com.biqasoft.entity.filters.LeadGenKPIsFilter;
import com.biqasoft.entity.tasks.Task;
import com.biqasoft.gateway.indicators.ManagerCreatedCustomersEntity;
import com.biqasoft.gateway.indicators.dto.DateGrouped;
import com.biqasoft.gateway.indicators.dto.DateGroupedStatisticsListEntity;
import com.biqasoft.gateway.leadgen.repositories.LeadGenRepository;
import com.biqasoft.microservice.common.MicroserviceUsersRepository;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.apache.commons.lang3.SerializationUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * The type Caller stats repository.
 */
@Service
public class KPIsRepository {

    private final MongoOperations ops;
    private final MicroserviceUsersRepository microserviceUsersRepository;
    private final BiqaObjectFilterService biqaObjectFilterService;
    private final LeadGenRepository leadRepository;

    @Autowired
    public KPIsRepository(LeadGenRepository leadRepository, @TenantDatabase MongoOperations ops, BiqaObjectFilterService biqaObjectFilterService, MicroserviceUsersRepository microserviceUsersRepository) {
        this.leadRepository = leadRepository;
        this.ops = ops;
        this.biqaObjectFilterService = biqaObjectFilterService;
        this.microserviceUsersRepository = microserviceUsersRepository;
    }

    public DataSourceIntegerAggregatedResponseFull getDataSourceHistoryDataForCurrentDomain(String dataSourceId, Date startDate, Date finalDate, DataSourceKPIsFilter builder) {
        return getDataSourceHistoryData(dataSourceId, ops, startDate, finalDate, builder);
    }

    public DataSourceIntegerAggregatedResponseFull getDataSourceHistoryData(String dataSourceId, MongoOperations mongoOperations, Date startDate, Date finalDate, DataSourceKPIsFilter builder) {
        DataSourceIntegerAggregatedResponseFull responseFull = new DataSourceIntegerAggregatedResponseFull();

        Criteria criteria = new Criteria();

        if (startDate != null && finalDate == null) criteria.and("createdInfo.createdDate").gte(startDate);
        if (startDate == null && finalDate != null) criteria.and("createdInfo.createdDate").lte(finalDate);
        if (startDate != null && finalDate != null)
            criteria.and("createdInfo.createdDate").gte(startDate).lte(finalDate);

        criteria.and("dataSourceId").is(dataSourceId);

        Query query = new Query(criteria);
        List<SavedDataSource> sources = ops.find(query, SavedDataSource.class);

        SavedDataSource previousIndicator = null;

        for (SavedDataSource source : sources) {

            // do not include repeated objects to resulted report
            if (previousIndicator != null & source != null && previousIndicator.getValues().equals(source.getValues()) && previousIndicator.getDataSourceId().equals(source.getDataSourceId())
                    && !builder.isIncludeRepeatedData()) {
                continue;
            }

            if (source == null || source.getCreatedInfo() == null) continue;

            DataSourceIntegerAggregatedResponse response = new DataSourceIntegerAggregatedResponse();
            response.setDate(source.getCreatedInfo().getCreatedDate());

            Integer val = source.getValues().getIntVal();

            if (val == null) continue;

            response.setValue(val);
            responseFull.getValues().add(response);

            previousIndicator = source;
        }

        return responseFull;
    }


    public LeadGenAggregatedResponseFull getAggregatedLeadGenKPIs(boolean isLeadGenMethod, boolean isLeadGenProject, String id, Date startDate, Date finalDate, LeadGenKPIsFilter builder) {

        // response
        LeadGenAggregatedResponseFull dataSourceAggregatedResponseFull = new LeadGenAggregatedResponseFull();

        // list of all lead gen method KPIs
        List<SavedLeadGenKPI> savedLeadGenKPI = new ArrayList<>();

        if (isLeadGenMethod) {
            savedLeadGenKPI = leadRepository.findLeadGenMethodHistoryData(id, startDate, finalDate);
        } else if (isLeadGenProject) {
            savedLeadGenKPI = leadRepository.findLeadGenProjectHistoryData(id, startDate, finalDate);
        }

        IndicatorsDTO previousIndicator = null;

        for (SavedLeadGenKPI kpi : savedLeadGenKPI) {
            IndicatorsDTO indicatorsDAO = kpi.getCachedKPIsData();

            if (indicatorsDAO == null) {
                continue;
            }

            // do not include repeated objects to resulted report
            if (previousIndicator != null && previousIndicator.equals(indicatorsDAO) && !builder.isIncludeRepeatedData()) {
                continue;
            }

            DataSourceAggregatedResponse roi = new DataSourceAggregatedResponse(indicatorsDAO.getROI(), kpi.getCreatedInfo().getCreatedDate());
            DataSourceAggregatedResponse dealsAmounts = new DataSourceAggregatedResponse(indicatorsDAO.getDealsAmounts(), kpi.getCreatedInfo().getCreatedDate());
            DataSourceAggregatedResponse costsAmount = new DataSourceAggregatedResponse(indicatorsDAO.getCostsAmount(), kpi.getCreatedInfo().getCreatedDate());
            DataSourceAggregatedResponse leadsNumber = new DataSourceAggregatedResponse(new BigDecimal(Long.valueOf(indicatorsDAO.getLeadsNumber()).toString()), kpi.getCreatedInfo().getCreatedDate());
            DataSourceAggregatedResponse customersNumber = new DataSourceAggregatedResponse(new BigDecimal(Long.valueOf(indicatorsDAO.getCustomersNumber()).toString()), kpi.getCreatedInfo().getCreatedDate());
            DataSourceAggregatedResponse dealsNumber = new DataSourceAggregatedResponse(new BigDecimal(Long.valueOf(indicatorsDAO.getDealsNumber()).toString()), kpi.getCreatedInfo().getCreatedDate());
            DataSourceAggregatedResponse costsNumber = new DataSourceAggregatedResponse(new BigDecimal(Long.valueOf(indicatorsDAO.getCostsNumber()).toString()), kpi.getCreatedInfo().getCreatedDate());
            DataSourceAggregatedResponse conversionFromLeadToCustomer = new DataSourceAggregatedResponse(indicatorsDAO.getConversionFromLeadToCustomer(), kpi.getCreatedInfo().getCreatedDate());
            DataSourceAggregatedResponse leadCost = new DataSourceAggregatedResponse(indicatorsDAO.getLeadCost(), kpi.getCreatedInfo().getCreatedDate());
            DataSourceAggregatedResponse customerCost = new DataSourceAggregatedResponse(indicatorsDAO.getCustomerCost(), kpi.getCreatedInfo().getCreatedDate());
            DataSourceAggregatedResponse averagePayment = new DataSourceAggregatedResponse(indicatorsDAO.getAveragePayment(), kpi.getCreatedInfo().getCreatedDate());
            DataSourceAggregatedResponse ltv = new DataSourceAggregatedResponse(indicatorsDAO.getLtv(), kpi.getCreatedInfo().getCreatedDate());
            DataSourceAggregatedResponse dealsCycle = new DataSourceAggregatedResponse(indicatorsDAO.getDealsCycle(), kpi.getCreatedInfo().getCreatedDate());

            dataSourceAggregatedResponseFull.getROI().add(roi);
            dataSourceAggregatedResponseFull.getDealsAmounts().add(dealsAmounts);
            dataSourceAggregatedResponseFull.getCostsAmount().add(costsAmount);
            dataSourceAggregatedResponseFull.getLeadsNumber().add(leadsNumber);
            dataSourceAggregatedResponseFull.getCustomersNumber().add(customersNumber);
            dataSourceAggregatedResponseFull.getDealsNumber().add(dealsNumber);
            dataSourceAggregatedResponseFull.getCostsNumber().add(costsNumber);
            dataSourceAggregatedResponseFull.getConversionFromLeadToCustomer().add(conversionFromLeadToCustomer);
            dataSourceAggregatedResponseFull.getLeadCost().add(leadCost);
            dataSourceAggregatedResponseFull.getCustomerCost().add(customerCost);
            dataSourceAggregatedResponseFull.getAveragePayment().add(averagePayment);
            dataSourceAggregatedResponseFull.getLtv().add(ltv);
            dataSourceAggregatedResponseFull.getDealsCycle().add(dealsCycle);

            previousIndicator = indicatorsDAO;
            dataSourceAggregatedResponseFull.setSucceed(true);
        }

        return dataSourceAggregatedResponseFull;
    }

    private void pushTaskToHashMap(Task task, Map<DateGrouped, List<Task>> hashMap) {

        // grouped by created date
        DateTime taskCreatedDate = new DateTime(task.getCreatedInfo().getCreatedDate());

        DateGrouped dateGrouped = new DateGrouped();
        dateGrouped.setDay(taskCreatedDate.getDayOfMonth());
        dateGrouped.setMonth(taskCreatedDate.getMonthOfYear());
        dateGrouped.setYear(taskCreatedDate.getYear());
        dateGrouped.setDayOfYear(taskCreatedDate.getDayOfYear());
        dateGrouped.setMinute(taskCreatedDate.getMinuteOfHour());
        dateGrouped.setHour(taskCreatedDate.getHourOfDay());

        DateGrouped dateGroupedFilter = new DateGrouped();
        dateGroupedFilter.setDayOfYear(dateGrouped.getDayOfYear());
        dateGroupedFilter.setYear(dateGrouped.getYear());
        dateGroupedFilter.setMonth(dateGrouped.getMonth());
        dateGroupedFilter.setDay(dateGrouped.getDay());


        if (hashMap.containsKey(dateGroupedFilter)) {
            hashMap.get(dateGroupedFilter).add(task);
        } else {
            List<Task> allTasksInList = new LinkedList<>();
            allTasksInList.add(task);
            hashMap.put(dateGroupedFilter, allTasksInList);
        }
    }

    private Map<DateGrouped, List<Task>> getGroupedTasksByDayMap(List<Task> allTasks) {
        Map<DateGrouped, List<Task>> hashMap = new LinkedHashMap<>();

        for (Task record : allTasks) {
            this.pushTaskToHashMap(record, hashMap);
        }

        return hashMap;
    }

    public Map<DateGrouped, List<Task>> getGroupedTasksByDayMapWithNullDays(List<Task> allTasks, Date startDate, Date finalDate) {

        Map<DateGrouped, List<Task>> hashMap = getGroupedTasksByDayMap(allTasks);

        DateTime startDateJodo = new DateTime(startDate);
        DateTime finalDateJodo = new DateTime(finalDate);

//        Days days =   Days.daysBetween(new DateTime(startDate).toInstant(), new DateTime(finalDate).toInstant());

        // TODO: bug when start and final years are different

        for (int i = startDateJodo.getDayOfYear(); i < finalDateJodo.getDayOfYear(); i++) {
            DateGrouped dateGrouped = new DateGrouped();
            dateGrouped.setDayOfYear(i);

            DateTime dateTime = SerializationUtils.clone(startDateJodo);
            dateTime = dateTime.withDayOfYear(dateGrouped.getDayOfYear());

            dateGrouped.setYear(dateTime.getYear());
            dateGrouped.setMonth(dateTime.getMonthOfYear());
            dateGrouped.setDay(dateTime.getDayOfMonth());

            if (!hashMap.containsKey(dateGrouped)) {
                List<Task> allTasksInList = new LinkedList<>();
                hashMap.put(dateGrouped, allTasksInList);
            }
        }
        return hashMap;
    }

    public List<DateGroupedStatisticsListEntity> getGroupedTasksByDay(List<Task> allTasks, Date startDate, Date finalDate, CurrentUserContextProvider currentUser) {
        Map<DateGrouped, List<Task>> hashMap = getGroupedTasksByDayMapWithNullDays(allTasks, startDate, finalDate);

        List<DateGroupedStatisticsListEntity> groupedStatisticsListEntities = new ArrayList<>();

        for (Map.Entry<DateGrouped, List<Task>> currentElement : hashMap.entrySet()) {
            DateGroupedStatisticsListEntity dateGroupedStatisticsListEntity = new DateGroupedStatisticsListEntity();
            dateGroupedStatisticsListEntity.setDateGrouped(currentElement.getKey());
            dateGroupedStatisticsListEntity.getDateGroupedStatistics().setTaskList(currentElement.getValue());
            dateGroupedStatisticsListEntity.getDateGroupedStatistics().setAllEntities(dateGroupedStatisticsListEntity.getDateGroupedStatistics().getTaskList().size());

            dateGroupedStatisticsListEntity.getDateGroupedStatistics().setActiveTasks((int) (long) currentElement.getValue().stream().filter(p -> !p.isCompleted()).count());
            dateGroupedStatisticsListEntity.getDateGroupedStatistics().setDoneTasks((int) (long) currentElement.getValue().stream().filter(p -> p.isCompleted()).count());

            dateGroupedStatisticsListEntity.getDateGroupedStatistics().setCreatedTask((int) (long) currentElement.getValue().stream()
                    .filter(p -> !p.getCreatedInfo().getCreatedById().equals(currentUser.getUserAccount().getId())).count());

            groupedStatisticsListEntities.add(dateGroupedStatisticsListEntity);
        }
        return groupedStatisticsListEntities;
    }

    private Map<UserAccount, ManagerCreatedCustomersEntity> pushToManagerCustomerEntityHashMap(Map<UserAccount, ManagerCreatedCustomersEntity> hashmap, List<Customer> customers) {
        for (Customer customer : customers) {
            UserAccount responsibleManager = microserviceUsersRepository.findByUserId(customer.getResponsibleManagerID());

            if (hashmap.containsKey(responsibleManager)) {
            } else {
                hashmap.put(responsibleManager, new ManagerCreatedCustomersEntity());
            }

            if (customer.isLead()) {
                hashmap.get(responsibleManager).getLeads().add(customer);
            }

            if (customer.isCustomer()) {
                hashmap.get(responsibleManager).getCustomers().add(customer);
            }
        }
        return hashmap;
    }


    public List<ManagerCreatedCustomersEntity> getCustomerAndLeadsCreatedByManager(CustomerFilter customerCreatedBuilder) {

        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(customerCreatedBuilder);

        Query query = biqaObjectFilterService.getQueryFromFilter(customerCreatedBuilder, criteria);

        List<Customer> allCustomerAndLeads = ops.find(query, Customer.class);

        Map<UserAccount, ManagerCreatedCustomersEntity> hashMap = pushToManagerCustomerEntityHashMap(new HashMap<>(), allCustomerAndLeads
        );

        List<ManagerCreatedCustomersEntity> managerCreatedCustomersEntity = new ArrayList<>();

        for (Map.Entry<UserAccount, ManagerCreatedCustomersEntity> currentElement : hashMap.entrySet()) {
            currentElement.getValue().setUserAccount(currentElement.getKey());
            managerCreatedCustomersEntity.add(currentElement.getValue());
            currentElement.getValue().setLeadsNumber(currentElement.getValue().getLeads().size());
            currentElement.getValue().setCustomersNumber(currentElement.getValue().getCustomers().size());
        }
        return managerCreatedCustomersEntity;
    }
}
