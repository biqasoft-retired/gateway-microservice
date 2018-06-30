/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.gateway.indicators.dto;

import com.biqasoft.auth.core.UserAccount;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.payments.CustomerDeal;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class ManagerPaymentEntity {

    @Id
    private String id = new ObjectId().toString();

    private List<Customer> customers = new ArrayList<>();
    private List<CustomerDeal> customerDeals = new ArrayList<>();
    private BigDecimal allDealsAmount = new BigDecimal("0");
    private int allDealsCount = 0;

    private Date latestDealDate;
    private CustomerDeal latestDeal;

    private UserAccount userAccount;

}
