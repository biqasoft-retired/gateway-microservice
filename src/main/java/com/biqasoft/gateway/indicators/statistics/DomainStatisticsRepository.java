/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.indicators.statistics;

import com.biqasoft.entity.customer.Opportunity;
import com.biqasoft.entity.filters.*;
import com.biqasoft.entity.payments.CompanyCost;
import com.biqasoft.entity.payments.CustomerDeal;
import com.biqasoft.users.domain.useraccount.UserAccount;
import com.biqasoft.gateway.analytics.repositories.AnalyticsRepository;
import com.biqasoft.gateway.customer.repositories.CompanyRepository;
import com.biqasoft.gateway.customer.repositories.CustomerFilterRequestContextService;
import com.biqasoft.gateway.customer.repositories.OpportunityRepository;
import com.biqasoft.gateway.customer.repositories.SegmentsRepository;
import com.biqasoft.gateway.datasources.repositories.DataSourceRepository;
import com.biqasoft.gateway.leadgen.repositories.LeadGenRepository;
import com.biqasoft.gateway.payments.repositories.PaymentsRepository;
import com.biqasoft.gateway.tasks.repositories.TaskRepository;
import com.biqasoft.storage.DefaultStorageService;
import com.biqasoft.microservice.common.MicroserviceUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DomainStatisticsRepository {

    @Autowired
    private PaymentsRepository paymentsRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private MicroserviceUsersRepository microserviceUsersRepository;

    @Autowired
    private CustomerFilterRequestContextService customerRepository;

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private LeadGenRepository leadRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DataSourceRepository allData;

    @Autowired
    private SegmentsRepository segmentsRepository;

    @Autowired
    private CustomerFilterRequestContextService customerFilterRequestContextService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private DefaultStorageService defaultStorageService;

    public long getActiveClientsNumber() {
        CustomerFilter customerBuilder = new CustomerFilter();
        customerBuilder.setCustomer(true);
        customerBuilder.setActive(true);
        customerBuilder.setOnlyCount(true);

        return customerFilterRequestContextService.getCustomersByFilter(customerBuilder).getEntityNumber();
    }

    public long getCurrentUserResponsibleClientsNumber() {
        CustomerFilter customerBuilder = new CustomerFilter();
        customerBuilder.setCustomer(true);
        customerBuilder.setActive(true);
        customerBuilder.setShowOnlyWhenIamResponsible(true);
        customerBuilder.setOnlyCount(true);

        return customerFilterRequestContextService.getCustomersByFilter(customerBuilder).getEntityNumber();
    }

    public long getActiveLeadsNumber() {
        CustomerFilter customerBuilder = new CustomerFilter();
        customerBuilder.setLead(true);
        customerBuilder.setActive(true);
        customerBuilder.setOnlyCount(true);

        return customerFilterRequestContextService.getCustomersByFilter(customerBuilder).getEntityNumber();
    }

    //TODO: optimise to response from mongoDB only length, not data
    public BasicStatsDTO getBasicStats() {
        BasicStatsDTO basicStats = new BasicStatsDTO();

        TaskFilter taskBuilder = new TaskFilter();
        taskBuilder.setOnlyCount(true);
        taskBuilder.setOnlyActive(true);

        TaskFilter taskBuilderAll = new TaskFilter();
        taskBuilderAll.setOnlyCount(true);

        basicStats.setAllActiveTasksCount((int) taskRepository.getTaskByFilter(taskBuilder).getEntityNumber());
        basicStats.setAllTasksCount((int) taskRepository.getTaskByFilter(taskBuilderAll).getEntityNumber());


        ////////////////
        //TODO: slow
        List<Opportunity> opportunityList = opportunityRepository.findAll();
        List<CustomerDeal> customerDeals = paymentsRepository.findAllCustomerDeals();
        List<CompanyCost> allCompanyCost = paymentsRepository.findAllCompanyCost();
        ////////////////

        basicStats.setAllDealsAmount(paymentsRepository.getAmountForPayment(customerDeals));
        basicStats.setAllDealsCount(customerDeals.size());

        basicStats.setAllOpportunitiesAmount(opportunityList.stream().map(Opportunity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        basicStats.setAllOpportunitiesCount(opportunityList.size());

        basicStats.setAllCompanyCostAmount(paymentsRepository.getAmountForPayment(allCompanyCost));
        basicStats.setAllCompanyCostCount(allCompanyCost.size());

        // document file
        StorageFileFilter documentFileBuilder = new StorageFileFilter();
        documentFileBuilder.setOnlyCount(true);
        basicStats.setDocumentsNumber(defaultStorageService.getStorageFileByFilter(documentFileBuilder).getEntityNumber());

        //TODO: slow
//        basicStats.setColdCallProjectNumber(callRepository.findAllCallProjects().size());

        CustomerFilter customerBuilder = new CustomerFilter();
        customerBuilder.setOnlyCount(true);
        customerBuilder.setCustomer(true);

        CustomerFilter leadBuilder = new CustomerFilter();
        leadBuilder.setOnlyCount(true);
        leadBuilder.setLead(true);

        basicStats.setAllCustomersCount((int) customerRepository.getCustomersByFilter(customerBuilder).getEntityNumber());
        basicStats.setAllLeadsCount((int) customerRepository.getCustomersByFilter(leadBuilder).getEntityNumber());

        // userAccounts
        List<UserAccount> userAccounts = microserviceUsersRepository.findAllUsers();
        basicStats.setAllStaffCount(userAccounts.size());
        basicStats.setAllActiveStaffCount(userAccounts.stream().filter(UserAccount::getEnabled).count());

        // customers
        basicStats.setActiveCustomersNumber(getActiveClientsNumber());
        basicStats.setCurrentUserResponsibleAndActiveCustomersNumber(getCurrentUserResponsibleClientsNumber());
        basicStats.setActiveLeadsNumber(getActiveLeadsNumber());

        // web analytics counter
        WebAnalyticsCounterFilter webAnalyticsCounterBuilder = new WebAnalyticsCounterFilter();
        webAnalyticsCounterBuilder.setOnlyCount(true);
        basicStats.setWebCounterNumber(analyticsRepository.getWebAnalyticsCounterByFilter(webAnalyticsCounterBuilder).getEntityNumber());

        // lead gen method
        LeadGenMethodFilter leadGenMethodBuilder = new LeadGenMethodFilter();
        leadGenMethodBuilder.setOnlyCount(true);
        basicStats.setLeadGenMethodsNumber(leadRepository.getLeadGenMethodByFilter(leadGenMethodBuilder).getEntityNumber());

        // lead gen project
        LeadGenProjectFilter leadGenProjectBuilder = new LeadGenProjectFilter();
        leadGenProjectBuilder.setOnlyCount(true);
        basicStats.setLeadGenProjectsNumber(leadRepository.getLeadGeProjectByFilter(leadGenProjectBuilder).getEntityNumber());

        // data source builder
        DataSourceFilter dataSourceBuilder = new DataSourceFilter();
        dataSourceBuilder.setOnlyCount(true);
        basicStats.setDataSourcesNumber(allData.getDataSourceByFilter(dataSourceBuilder).getEntityNumber());

        // company builder
        CompanyFilter filter = new CompanyFilter();
        filter.setOnlyCount(true);
        basicStats.setCompaniesNumber(companyRepository.getCompanyByFilter(filter).getEntityNumber());

        // static segments number
        StaticSegmentFilter staticSegmentBuilder = new StaticSegmentFilter();
        staticSegmentBuilder.setOnlyCount(true);
        basicStats.setStaticSegmentsNumber(segmentsRepository.getStaticSegmentByFilter(staticSegmentBuilder).getEntityNumber());

        // dynamic segment number
        DynamicSegmentFilter dynamicSegmentBuilder = new DynamicSegmentFilter();
        dynamicSegmentBuilder.setOnlyCount(true);
        basicStats.setDynamicSegmentsNumber(segmentsRepository.getDynamicSegmentByFilter(dynamicSegmentBuilder).getEntityNumber());

        return basicStats;
    }
}
