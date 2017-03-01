/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.indicators.repositories;

import com.biqasoft.entity.core.useraccount.UserAccount;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.filters.PaymentDealsFilter;
import com.biqasoft.entity.indicators.dto.ManagerPaymentEntity;
import com.biqasoft.entity.payments.CustomerDeal;
import com.biqasoft.gateway.customer.repositories.CustomerRepository;
import com.biqasoft.microservice.common.MicroserviceUsersRepository;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KPIsPaymentsRepository {

    private final MongoOperations ops;
    private final CustomerRepository customerRepository;
    private final BiqaObjectFilterService biqaObjectFilterService;
    private final MicroserviceUsersRepository microserviceUsersRepository;

    @Autowired
    public KPIsPaymentsRepository(@TenantDatabase MongoOperations ops, CustomerRepository customerRepository, BiqaObjectFilterService biqaObjectFilterService, MicroserviceUsersRepository microserviceUsersRepository) {
        this.ops = ops;
        this.customerRepository = customerRepository;
        this.biqaObjectFilterService = biqaObjectFilterService;
        this.microserviceUsersRepository = microserviceUsersRepository;
    }


    /**
     * @param hashmap       key - {@link UserAccount#getId()}
     * @param customerDeals
     * @return
     */
    private Map<String, ManagerPaymentEntity> groupCustomerDealsToResponsibleManager(Map<String, ManagerPaymentEntity> hashmap, List<CustomerDeal> customerDeals) {
        for (CustomerDeal customerDeal : customerDeals) {

            if (customerDeal.getConnectedInfo() == null || customerDeal.getConnectedInfo().getConnectedCustomerId() == null) {
                continue;
            }

            Customer customer = customerRepository.findCustomerOrLeadByID(customerDeal.getConnectedInfo().getConnectedCustomerId());
            if (customer == null) {
                continue;
            }

            UserAccount responsibleManager = microserviceUsersRepository.findByUserId(customer.getResponsibleManagerID());
            String userNameId = responsibleManager.getId();

            ManagerPaymentEntity entity = hashmap.get(userNameId);

            if (entity == null) {
                hashmap.put(userNameId, new ManagerPaymentEntity());
                entity = hashmap.get(userNameId);
            }

            entity.getCustomerDeals().add(customerDeal);
            entity.setAllDealsCount(entity.getAllDealsCount() + 1);
            entity.setAllDealsAmount(entity.getAllDealsAmount().add(customerDeal.getAmount()));
        }
        return hashmap;
    }


    public List<CustomerDeal> getCustomerDealsFromBuilder(PaymentDealsFilter paymentDealsBuilder) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(paymentDealsBuilder);
        Query query = biqaObjectFilterService.getQueryFromFilter(paymentDealsBuilder, criteria);
        return ops.find(query, CustomerDeal.class);
    }

    public List<ManagerPaymentEntity> getAllManagerPaymentDealsKPIs(PaymentDealsFilter paymentDealsBuilder) {
        List<CustomerDeal> customerDeals = getCustomerDealsFromBuilder(paymentDealsBuilder);

        Map<String, ManagerPaymentEntity> hashMap = groupCustomerDealsToResponsibleManager(new HashMap<>(), customerDeals);

        List<ManagerPaymentEntity> managerPaymentEntities = new ArrayList<>();

        for (Map.Entry<String, ManagerPaymentEntity> currentElement : hashMap.entrySet()) {
            currentElement.getValue().setUserAccount(microserviceUsersRepository.findByUserId(currentElement.getKey()));
            managerPaymentEntities.add(currentElement.getValue());
        }

        if (paymentDealsBuilder.isSortByDealsAmount()) {
            managerPaymentEntities = Lists.reverse(
                    managerPaymentEntities.stream().sorted((object1, object2) -> Double.compare(object1.getAllDealsAmount().doubleValue(), object2.getAllDealsAmount().doubleValue()))
                            .collect(Collectors.toList())
            );
        }

        for (ManagerPaymentEntity entity : managerPaymentEntities) {
            if (entity.getCustomerDeals().size() == 0) continue;

            CustomerDeal latestDeal = Lists.reverse(
                    entity.getCustomerDeals().stream().sorted((obj1, obj2) -> obj1.getCreatedInfo().getCreatedDate().compareTo(obj2.getCreatedInfo().getCreatedDate())).collect(Collectors.toList())
            ).get(0);

            entity.setLatestDealDate(latestDeal.getCreatedInfo().getCreatedDate());
            entity.setLatestDeal(latestDeal);
        }

        return managerPaymentEntities;
    }

}
