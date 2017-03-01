/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.cloud.dto;

import io.swagger.annotations.ApiModelProperty;
import com.biqasoft.entity.core.useraccount.UserAccount;

import java.io.Serializable;

public class LandingPageRequestDTO implements Serializable {

    @ApiModelProperty("Main (with ROLE_ADMIN) account in domain")
    private UserAccount userAccount;

    @ApiModelProperty("User browser time zone for domain")
    private int timeZoneOffset = 3;

    @ApiModelProperty("Instead of OAuth2 username and password, return real username and password")
    private boolean returnUserNameAndPassword = false;


    public boolean isReturnUserNameAndPassword() {
        return returnUserNameAndPassword;
    }

    public void setReturnUserNameAndPassword(boolean returnUserNameAndPassword) {
        this.returnUserNameAndPassword = returnUserNameAndPassword;
    }

    public int getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(int timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }


}
