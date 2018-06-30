/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.gateway.indicators;

import com.biqasoft.auth.core.UserAccount;
import com.biqasoft.entity.customer.Customer;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ManagerCreatedCustomersEntity implements Serializable {

    @Id
    private String id = new ObjectId().toString();

    private List<Customer> customers = new ArrayList<>();
    private List<Customer> leads = new ArrayList<>();

    private int customersNumber = 0;
    private int leadsNumber = 0;

    private UserAccount userAccount = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public List<Customer> getLeads() {
        return leads;
    }

    public void setLeads(List<Customer> leads) {
        this.leads = leads;
    }

    public int getCustomersNumber() {
        return customersNumber;
    }

    public void setCustomersNumber(int customersNumber) {
        this.customersNumber = customersNumber;
    }

    public int getLeadsNumber() {
        return leadsNumber;
    }

    public void setLeadsNumber(int leadsNumber) {
        this.leadsNumber = leadsNumber;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }
}
