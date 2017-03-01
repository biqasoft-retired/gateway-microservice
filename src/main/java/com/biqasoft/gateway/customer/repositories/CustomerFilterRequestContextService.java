/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.customer.repositories;

import com.biqasoft.customer.CustomerFilterService;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.filters.CustomerFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import com.biqasoft.persistence.base.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomerFilterRequestContextService extends CustomerFilterService {

    private final MongoTemplate ops;
    private final CurrentUser currentUser;

    @Autowired
    public CustomerFilterRequestContextService(CurrentUser currentUser, @TenantDatabase MongoTemplate ops, BiqaObjectFilterService biqaObjectFilterService, DateService dateService) {
        super(biqaObjectFilterService, dateService);

        this.currentUser = currentUser;
        this.ops = ops;
    }

    public BiqaPaginationResultList<Customer> getCustomersByFilter(CustomerFilter customerBuilder) {
        if (customerBuilder.isShowOnlyWhenIamResponsible()) {
            customerBuilder.getResponsiblesManagersList().clear();
            customerBuilder.getResponsiblesManagersList().add(currentUser.getCurrentUser().getId());
            customerBuilder.setUseResponsiblesManagersList(true);
        }
        return getCustomersByFilterForDomain(customerBuilder, ops);
    }

}
