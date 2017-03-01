/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.gateway.customer.dto;

import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.customer.Opportunity;
import com.biqasoft.entity.payments.CustomerDeal;
import com.biqasoft.entity.tasks.Task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CustomerDetails implements Serializable {

    private Customer customer;

    private List<Task> tasks = new ArrayList<>();
    private List<Opportunity> opportunities = new ArrayList<>();
    private List<CustomerDeal> customerDeals = new ArrayList<>();

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Opportunity> getOpportunities() {
        return opportunities;
    }

    public void setOpportunities(List<Opportunity> opportunities) {
        this.opportunities = opportunities;
    }

    public List<CustomerDeal> getCustomerDeals() {
        return customerDeals;
    }

    public void setCustomerDeals(List<CustomerDeal> customerDeals) {
        this.customerDeals = customerDeals;
    }
}
