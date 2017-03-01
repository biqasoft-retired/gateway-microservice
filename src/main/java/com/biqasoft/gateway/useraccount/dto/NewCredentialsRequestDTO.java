/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.useraccount.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 6/8/2016
 *         All Rights Reserved
 */
public class NewCredentialsRequestDTO {
    private List<String> roles = new ArrayList<>();
    private Date expire = null;

    public Date getExpire() {
        return expire;
    }

    public void setExpire(Date expire) {
        this.expire = expire;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
