/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.admin.dto;

import com.mongodb.CommandResult;
import io.swagger.annotations.ApiModel;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 4/17/2016.
 * All Rights Reserved
 */
@ApiModel("Execute mongodb command result")
public class ExecuteDatabaseCommandResultDTO {

    private CommandResult result = null;

    public CommandResult getResult() {
        return result;
    }

    public void setResult(CommandResult result) {
        this.result = result;
    }
}
