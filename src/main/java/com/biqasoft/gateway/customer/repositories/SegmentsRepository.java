/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.customer.repositories;

import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.constants.DATA_SOURCES;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.customer.DynamicSegment;
import com.biqasoft.entity.customer.SegmentStats;
import com.biqasoft.entity.customer.StaticSegment;
import com.biqasoft.entity.datasources.DataSource;
import com.biqasoft.entity.filters.DynamicSegmentFilter;
import com.biqasoft.entity.filters.StaticSegmentFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.system.NameValueMap;
import com.biqasoft.gateway.datasources.repositories.DataSourceRepository;
import com.biqasoft.gateway.payments.repositories.PaymentsRepository;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SegmentsRepository {

    private final MongoTemplate ops;
    private final PaymentsRepository paymentsRepository;
    private final OpportunityRepository opportunityRepository;
    private final BiqaObjectFilterService biqaObjectFilterService;
    private final CustomerFilterRequestContextService customerFilterRequestContextService;
    private final DataSourceRepository dataSourceAllData;

    @Autowired
    public SegmentsRepository(@TenantDatabase MongoTemplate ops, PaymentsRepository paymentsRepository, OpportunityRepository opportunityRepository,
                              BiqaObjectFilterService biqaObjectFilterService, CustomerFilterRequestContextService customerFilterRequestContextService, DataSourceRepository dataSourceAllData) {
        this.ops = ops;
        this.paymentsRepository = paymentsRepository;
        this.opportunityRepository = opportunityRepository;
        this.biqaObjectFilterService = biqaObjectFilterService;
        this.customerFilterRequestContextService = customerFilterRequestContextService;
        this.dataSourceAllData = dataSourceAllData;
    }

    ///////////////////////// SEGMENTATION ///////////////////////////
    @BiqaAddObject
    @BiqaAuditObject
    public StaticSegment addStaticSegment(StaticSegment staticSegment) {
        ops.insert(staticSegment);
        return staticSegment;
    }

    private void processDynamicSegment(DynamicSegment dynamicSegment) {
        DataSource dataSource = new DataSource();
        dataSource.setName("Клиентов в сегменте " + dynamicSegment.getName());
        dataSource.setSystemIssued(true);
//        dataSource.setResolved(true);
        dataSource.setControlledClass(DATA_SOURCES.CUSTOMERS_BY_DYNAMIC_SEGMENT_ID);

        List<NameValueMap> realParamsForWidget2 = new ArrayList<>();
        NameValueMap realParamsForWidget22 = new NameValueMap();
        realParamsForWidget22.setName("dynamicSegmentId");
        realParamsForWidget22.setValue(dynamicSegment.getId());
        realParamsForWidget2.add(realParamsForWidget22);
        dataSource.setParameters(realParamsForWidget2);

        dataSourceAllData.addNewDataSourceSavedData(dataSource);
    }

    @BiqaAddObject
    @BiqaAuditObject
    public DynamicSegment addDynamicSegment(DynamicSegment dynamicSegment) {
        ops.insert(dynamicSegment);
        try {
            processDynamicSegment(dynamicSegment);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dynamicSegment;
    }

    @BiqaAuditObject
    public DynamicSegment updateDynamicSegment(DynamicSegment dynamicSegment) {
        return biqaObjectFilterService.safeUpdate(dynamicSegment, ops);
    }

    @BiqaAuditObject
    public StaticSegment updateStaticSegment(StaticSegment staticSegment) {
        return biqaObjectFilterService.safeUpdate(staticSegment, ops);
    }

    public SegmentStats getStatsByStaticSegmentId(String staticSegmentID) {
        SegmentStats segmentStats = new SegmentStats();

        List<Customer> customers = getAllCustomersByStaticSegment(staticSegmentID);

        for (Customer customer : customers) {
            if (customer.isLead()) segmentStats.setLeadsCount(segmentStats.getLeadsCount() + 1);
            if (customer.isCustomer()) segmentStats.setCustomerCount(segmentStats.getCustomerCount() + 1);

            //TODO: slow
            segmentStats.setDealsAmount(segmentStats.getDealsAmount().add(paymentsRepository.getDealsAmountByCustomerId(customer.getId())));
            segmentStats.setDealsNumber(segmentStats.getDealsNumber() + paymentsRepository.findAllDealsByCustomerId(customer.getId()).size());

            //TODO: slow
            segmentStats.setOpportunityAmount(segmentStats.getOpportunityAmount().add(opportunityRepository.getOpportunityAmountByCustomerId(customer.getId())));
            segmentStats.setOpportunityNumber(segmentStats.getDealsNumber() + opportunityRepository.findOpportunitieByCustomerId(customer.getId()).size());
        }

        return segmentStats;
    }

    public SegmentStats getStatsByDynamicSegmentId(String dynamicSegmentID) {
        SegmentStats segmentStats = new SegmentStats();

        List<Customer> customers = findCustomerByDynamicSegment(dynamicSegmentID);

        for (Customer customer : customers) {
            if (customer.isLead()) segmentStats.setLeadsCount(segmentStats.getLeadsCount() + 1);
            if (customer.isCustomer()) segmentStats.setCustomerCount(segmentStats.getCustomerCount() + 1);
            //TODO: slow
            segmentStats.setDealsAmount(segmentStats.getDealsAmount().add(paymentsRepository.getDealsAmountByCustomerId(customer.getId())));
            segmentStats.setDealsNumber(segmentStats.getDealsNumber() + paymentsRepository.findAllDealsByCustomerId(customer.getId()).size());

            //TODO: slow
            segmentStats.setOpportunityAmount(segmentStats.getOpportunityAmount().add(opportunityRepository.getOpportunityAmountByCustomerId(customer.getId())));
            segmentStats.setOpportunityNumber(segmentStats.getDealsNumber() + opportunityRepository.findOpportunitieByCustomerId(customer.getId()).size());
        }

        return segmentStats;
    }

    public List<Customer> findCustomerByDynamicSegment(String dynamicSegmentID) {
        return customerFilterRequestContextService.getCustomersByFilter(findDynamicSegmentById(dynamicSegmentID).getCustomerBuilder()).getResultedObjects();
    }

    public List<Customer> getAllCustomersByStaticSegment(String staticSegmentID) {
        return ops.find(Query.query(Criteria.
                where("staticSegmentsIDs").in(staticSegmentID)
        ), Customer.class);
    }

    public DynamicSegment findDynamicSegmentById(String dynamicSegmentId) {
        return ops.findOne(Query.query(Criteria.where("id").is(dynamicSegmentId)), DynamicSegment.class);
    }

    public StaticSegment findStaticSegmentById(String dynamicSegmentId) {
        return ops.findOne(Query.query(Criteria.where("id").is(dynamicSegmentId)), StaticSegment.class);
    }

    public List<StaticSegment> findAllStaticSegments() {
        return ops.findAll(StaticSegment.class);
    }

    public List<DynamicSegment> findAllDynamicSegments() {
        return ops.findAll(DynamicSegment.class);
    }
///////////////////////// SEGMENTATION ///////////////////////////

    public BiqaPaginationResultList<StaticSegment> getStaticSegmentByFilter(StaticSegmentFilter filter) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(filter);
        Query query = biqaObjectFilterService.getQueryFromFilter(filter, criteria);

        if (filter.isUseStaticSegments()) criteria.and("id").in(filter.getStaticSegmentsIDs());

        return biqaObjectFilterService.getPaginationResultList(filter, criteria, query, StaticSegment.class, ops);
    }

    public BiqaPaginationResultList<DynamicSegment> getDynamicSegmentByFilter(DynamicSegmentFilter filter) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(filter);
        Query query = biqaObjectFilterService.getQueryFromFilter(filter, criteria);

        if (filter.isUseListOfIDs()) criteria.and("id").in(filter.getListOfIDs());

        return biqaObjectFilterService.getPaginationResultList(filter, criteria, query, DynamicSegment.class, ops);
    }

}
