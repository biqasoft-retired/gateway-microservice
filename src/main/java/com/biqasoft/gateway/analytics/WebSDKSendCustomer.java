package com.biqasoft.gateway.analytics;

import com.biqasoft.entity.customer.Customer;

/**
 * Created by Nikita on 9/15/2016.
 */
public class WebSDKSendCustomer extends Customer {

    private String domain;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
