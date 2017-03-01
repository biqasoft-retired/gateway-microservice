/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.customer.repositories;

import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.annotations.BiqaCheckSecuredModifyObject;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.customer.Company;
import com.biqasoft.entity.filters.CompanyFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyRepository {

    private final CurrentUser currentUser;
    private final BiqaObjectFilterService biqaObjectFilterService;
    private final MongoTemplate ops;

    @Autowired
    public CompanyRepository(CurrentUser currentUser, BiqaObjectFilterService biqaObjectFilterService, @TenantDatabase MongoTemplate ops) {
        this.currentUser = currentUser;
        this.biqaObjectFilterService = biqaObjectFilterService;
        this.ops = ops;
    }

    @BiqaAddObject
    @BiqaAuditObject
    public Company addCompany(Company note) {
        ops.insert(note);
        return note;
    }

    @BiqaCheckSecuredModifyObject
    @BiqaAuditObject
    public Company updateCompany(Company company) {
        return biqaObjectFilterService.safeUpdate(company, ops);
    }

    public boolean deleteCompanyById(String id) {
        Company company = findCompanyById(id);
        ops.remove(company);
        return true;
    }

    public Company findCompanyById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id)
        ), Company.class);
    }

    public List<Company> findAll() {
        return ops.findAll(Company.class);
    }

    public BiqaPaginationResultList<Company> getCompanyByFilter(CompanyFilter filter) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(filter);
        Query query = biqaObjectFilterService.getQueryFromFilter(filter, criteria);

        if (filter.isOnlyPartner()) criteria.and("partner").is(true);
        if (filter.isOnlyClient()) criteria.and("client").is(true);

        if (filter.isActive()) criteria.and("active").is(true);
        if (filter.isImportant()) criteria.and("important").is(true);

        if (filter.isUseCompanyIDsList()) criteria.and("id").in(filter.getCompanyIDsList());

        if (filter.isUseResponsiblesManagersList())
            criteria.and("responsibleManagerID").in(filter.getResponsiblesManagersList());

        if (filter.isShowOnlyWhenIamResponsible() && !filter.isUseResponsiblesManagersList())
            criteria.and("responsibleManagerID").is(currentUser.getCurrentUser().getId());

       return biqaObjectFilterService.getPaginationResultList(filter, criteria, query, Company.class, ops);
    }

}
