/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.useraccount.dto;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 6/8/2016
 *         All Rights Reserved
 */
public class OAuthTokenModificationRequestDTO {
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
