/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.customer.repositories;

import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.annotations.BiqaCheckSecuredModifyObject;
import com.biqasoft.entity.constants.SYSTEM_FIELDS_CONST;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.customer.Opportunity;
import com.biqasoft.entity.filters.OpportunityFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OpportunityRepository {

    private final MongoTemplate ops;
    private final BiqaObjectFilterService biqaObjectFilterService;
    private final CustomerRepository customerRepository;

    @Autowired
    public OpportunityRepository(BiqaObjectFilterService biqaObjectFilterService, CustomerRepository customerRepository,
                                 @TenantDatabase MongoTemplate ops) {
        this.biqaObjectFilterService = biqaObjectFilterService;
        this.customerRepository = customerRepository;
        this.ops = ops;
    }

    @BiqaAddObject
    @BiqaAuditObject
    public void addOpportunity(Opportunity note) {

        Customer customer = customerRepository.findCustomerOrLeadByID(note.getConnectedInfo().getConnectedCustomerId());

        if (note.getLeadGenProjectId() == null || note.getLeadGenProjectId().equals(""))
            note.setLeadGenProjectId(customer.getLeadGenProject());
        if (note.getLeadGenMethodId() == null || note.getLeadGenMethodId().equals(""))
            note.setLeadGenMethodId(customer.getLeadGenMethod());

        ops.insert(note);

        if (note.getConnectedInfo().getConnectedCustomerId() != null && !note.getConnectedInfo().getConnectedCustomerId().equals("")) {
            customerRepository.refreshCustomerOverview(note.getConnectedInfo().getConnectedCustomerId());
        }
    }

    public Opportunity findOpportunityById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id)), Opportunity.class);
    }

    public List<Opportunity> findOpportunitieByCustomerId(String id) {
        return ops.find(Query.query(Criteria.where("connectedInfo.connectedCustomerId").is(id)), Opportunity.class);
    }

    public BigDecimal getOpportunityAmountByCustomerId(String id) {
        return findOpportunitieByCustomerId(id).parallelStream().map(Opportunity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @BiqaCheckSecuredModifyObject
    @BiqaAuditObject
    public Opportunity updateOpportunity(Opportunity newObject) {
        Opportunity opportunity = findOpportunityById(newObject.getId());
        opportunity.setName(newObject.getName());
        opportunity.setAmount(newObject.getAmount());
        opportunity.setPriority(newObject.getPriority());

        newObject = biqaObjectFilterService.safeUpdate(opportunity, ops);

        if (newObject.getConnectedInfo().getConnectedCustomerId() != null && !newObject.getConnectedInfo().getConnectedCustomerId().equals("")) {
            customerRepository.refreshCustomerOverview(newObject.getConnectedInfo().getConnectedCustomerId());
        }

        return newObject;
    }

    public boolean deleteOpportunity(String id) {
        Opportunity opportunity = findOpportunityById(id);
        ops.remove(opportunity);

        if (opportunity.getConnectedInfo().getConnectedCustomerId() != null && !opportunity.getConnectedInfo().getConnectedCustomerId().equals("")) {
            customerRepository.refreshCustomerOverview(opportunity.getConnectedInfo().getConnectedCustomerId());
        }
        return true;
    }

    public List<Opportunity> findAllActiveByCustomerId(String id) {

        return ops.find(Query.query(Criteria
                .where("connectedInfo.connectedCustomerId").is(id)
                .and("active").is(true)
        ), Opportunity.class);
    }

    public List<Opportunity> findAll() {
        return ops.findAll(Opportunity.class);
    }

    public BiqaPaginationResultList<Opportunity> getOpportunitysByFilter(OpportunityFilter filter) {
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
            criteria.and("leadGenProjectId").is(filter.getLeadGenProjectId());


        if (filter.isSortByAmount()) {
            query.with(new Sort(Sort.Direction.DESC, "amount"));
        }

        return biqaObjectFilterService.getPaginationResultList(filter, criteria, query, Opportunity.class, ops);
    }

}
