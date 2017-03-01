/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.payments.repositories;

import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.constants.SYSTEM_FIELDS_CONST;
import com.biqasoft.entity.filters.CostsFilter;
import com.biqasoft.entity.filters.DealsFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.payments.CompanyCost;
import com.biqasoft.entity.payments.CustomerDeal;
import com.biqasoft.entity.payments.Payment;
import com.biqasoft.gateway.customer.repositories.CustomerRepository;
import com.biqasoft.gateway.leadgen.repositories.LeadGenRepository;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentsRepository {

    private final MongoOperations ops;
    private final BiqaObjectFilterService biqaObjectFilterService;
    private LeadGenRepository leadRepository;
    private CustomerRepository customerRepository;

    @Autowired
    public PaymentsRepository(@TenantDatabase MongoOperations ops, BiqaObjectFilterService biqaObjectFilterService) {
        this.ops = ops;
        this.biqaObjectFilterService = biqaObjectFilterService;
    }

    @BiqaAddObject
    @BiqaAuditObject
    public CustomerDeal addCustomerDeal(CustomerDeal customer) {
        ops.insert(customer);

        if (customer.getConnectedInfo().getConnectedCustomerId() != null && !customer.getConnectedInfo().getConnectedCustomerId().equals("")) {
            customerRepository.refreshCustomerOverview(customer.getConnectedInfo().getConnectedCustomerId());
        }

        return customer;
    }

    public boolean deleteCustomerDeal(String customerDealId) {
        CustomerDeal c = findCustomerDealById(customerDealId);
        ops.remove(c);
        return true;
    }

    @BiqaAddObject
    @BiqaAuditObject
    public CompanyCost addCompanyCost(CompanyCost customer) {

        if (customer.getLeadGenProjectId() != null && !customer.getLeadGenProjectId().equals("")) {
            customer.setLeadGenMethodId(leadRepository.findLeadGenProjectById(customer.getLeadGenProjectId()).getLeadGenMethodId());
        }

        ops.insert(customer);
        return customer;
    }

    @BiqaAuditObject
    public CustomerDeal updateCustomerDeal(CustomerDeal customerDeal) {
        customerDeal = biqaObjectFilterService.safeUpdate(customerDeal, ops);
        if (customerDeal.getConnectedInfo().getConnectedCustomerId() != null && !customerDeal.getConnectedInfo().getConnectedCustomerId().equals("")) {
            customerRepository.refreshCustomerOverview(customerDeal.getConnectedInfo().getConnectedCustomerId());
        }
        return customerDeal;
    }

    @BiqaAuditObject
    public CompanyCost updateCompanyCost(CompanyCost customer) {
        return biqaObjectFilterService.safeUpdate(customer, ops);
    }

    public List<CustomerDeal> findAllCustomerDeals() {
        return ops.findAll(CustomerDeal.class);
    }

    public List<CompanyCost> findAllCompanyCost() {
        return ops.findAll(CompanyCost.class);
    }

    public List<CustomerDeal> findAllDealsByCustomerId(String customerId) {
        return ops.find(Query.query(Criteria.where("connectedInfo.connectedCustomerId").is(customerId)), CustomerDeal.class);
    }

    public BigDecimal getDealsAmountByCustomerId(String id) {
        return findAllDealsByCustomerId(id).parallelStream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public <T extends Payment> BigDecimal getAmountForPayment(List<T> payments) {
        return payments.parallelStream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public CustomerDeal findCustomerDealById(String customerId) {
        return ops.findOne(Query.query(Criteria.where("id").is(customerId)), CustomerDeal.class);
    }

    public CompanyCost findCompanyCostById(String customerId) {
        return ops.findOne(Query.query(Criteria.where("id").is(customerId)), CompanyCost.class);
    }

    public List<CompanyCost> findAllCompanyCostByLeadGenProjectId(String leadGenProjectId) {
        return ops.find(Query.query(Criteria.where("customLeadGenProjectId").is(leadGenProjectId)), CompanyCost.class);
    }

    public BiqaPaginationResultList<CustomerDeal> getDealsByFilter(DealsFilter dealsBuilder) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(dealsBuilder);
        Query query = biqaObjectFilterService.getQueryFromFilter(dealsBuilder, criteria);

        // only less then amount filter
        if (dealsBuilder.isUseAmountLessThan() && !dealsBuilder.isUseAmountMoreThan())
            criteria.and("amount").lte(dealsBuilder.getAmountLessThan());
        // only more then amount filter
        if (dealsBuilder.isUseAmountMoreThan() && !dealsBuilder.isUseAmountLessThan())
            criteria.and("amount").gte(dealsBuilder.getAmountMoreThan());
        //  less then and more then amount filter together
        if (dealsBuilder.isUseAmountMoreThan() && dealsBuilder.isUseAmountLessThan())
            criteria.and("amount").lte(dealsBuilder.getAmountLessThan()).gte(dealsBuilder.getAmountMoreThan());

        // only more then priority filter
        if (dealsBuilder.isUsePriorityMoreThan() && !dealsBuilder.isUsePriorityLessThan())
            criteria.and("priority").gte(dealsBuilder.getPriorityMoreThan());
        // only less then priority filter
        if (dealsBuilder.isUsePriorityLessThan() && !dealsBuilder.isUsePriorityMoreThan())
            criteria.and("priority").lte(dealsBuilder.getPriorityLessThan());
        //  less then and more then priority filter together
        if (dealsBuilder.isUsePriorityLessThan() && dealsBuilder.isUsePriorityMoreThan())
            criteria.and("priority").lte(dealsBuilder.getPriorityLessThan()).gte(dealsBuilder.getPriorityMoreThan());

        // who is buyers (customers)
        if (dealsBuilder.isUseCustomerIDs()) {
            criteria.and("connectedInfo.connectedCustomerId").in(dealsBuilder.getCustomerIDs());
        }

        if (dealsBuilder.getLeadGenMethodId() != null && !dealsBuilder.getLeadGenMethodId().equals(SYSTEM_FIELDS_CONST.ANY))
            criteria.and("leadGenMethodId").is(dealsBuilder.getLeadGenMethodId());

        if (dealsBuilder.getLeadGenProjectId() != null && !dealsBuilder.getLeadGenProjectId().equals(SYSTEM_FIELDS_CONST.ANY))
            criteria.and("leadGenProjectId").is(dealsBuilder.getLeadGenProjectId());

        if (dealsBuilder.isSortByAmount()) {
            query.with(new Sort(Sort.Direction.DESC, "amount"));
        }

        return biqaObjectFilterService.getPaginationResultList(dealsBuilder, criteria, query, CustomerDeal.class, ops);
    }

    public BiqaPaginationResultList<CompanyCost> getCostsByFilter(CostsFilter filter) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(filter);
        Query query = biqaObjectFilterService.getQueryFromFilter(filter, criteria);

        // only less then amount filter
        if (filter.isUseAmountLessThan() && !filter.isUseAmountMoreThan())
            criteria.and("amount").lte(filter.getAmountLessThan());
        // only more then amount filter
        if (filter.isUseAmountMoreThan() && !filter.isUseAmountLessThan())
            criteria.and("amount").gte(filter.getAmountMoreThan());
        //  less then and more then amount filter together
        if (filter.isUseAmountMoreThan() && filter.isUseAmountLessThan())
            criteria.and("amount").lte(filter.getAmountLessThan()).gte(filter.getAmountMoreThan());

        // only more then priority filter
        if (filter.isUsePriorityMoreThan() && !filter.isUsePriorityLessThan())
            criteria.and("priority").gte(filter.getPriorityMoreThan());
        // only less then priority filter
        if (filter.isUsePriorityLessThan() && !filter.isUsePriorityMoreThan())
            criteria.and("priority").lte(filter.getPriorityLessThan());
        //  less then and more then priority filter together
        if (filter.isUsePriorityLessThan() && filter.isUsePriorityMoreThan())
            criteria.and("priority").lte(filter.getPriorityLessThan()).gte(filter.getPriorityMoreThan());

        // who is buyers (customers)
        if (filter.isUseCustomerIDs()) {
            criteria.and("connectedInfo.connectedCustomerId").in(filter.getCustomerIDs());
        }

        if (filter.getLeadGenMethodId() != null && !filter.getLeadGenMethodId().equals(SYSTEM_FIELDS_CONST.ANY))
            criteria.and("leadGenMethodId").is(filter.getLeadGenMethodId());

        if (filter.getLeadGenProjectId() != null && !filter.getLeadGenProjectId().equals(SYSTEM_FIELDS_CONST.ANY))
            criteria.and("customLeadGenProjectId").is(filter.getLeadGenProjectId());

        if (filter.isSortByAmount()) {
            query.with(new Sort(Sort.Direction.DESC, "amount"));
        }

        return biqaObjectFilterService.getPaginationResultList(filter, criteria, query, CompanyCost.class, ops);
    }

    @Autowired
    public void setLeadRepository(LeadGenRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Autowired
    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
}
