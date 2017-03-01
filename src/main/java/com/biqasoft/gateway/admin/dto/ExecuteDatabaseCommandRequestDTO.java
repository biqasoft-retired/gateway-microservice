/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.admin.dto;

import io.swagger.annotations.ApiModel;

import java.io.Serializable;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 4/17/2016.
 * All Rights Reserved
 */
@ApiModel("Execute mongodb command request")
public class ExecuteDatabaseCommandRequestDTO implements Serializable {

    private String command = null;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
