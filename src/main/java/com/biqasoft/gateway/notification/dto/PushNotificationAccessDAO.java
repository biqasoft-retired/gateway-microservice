/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.notification.dto;

import com.biqasoft.entity.core.BaseClass;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.biqasoft.entity.core.useraccount.UserAccount;

@Document
public class PushNotificationAccessDAO extends BaseClass {

    private String clientId;

    private String platform;
    private String model;
    private String uuid;


    @DBRef
    private UserAccount userAccount;


    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }
}
