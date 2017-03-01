/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.cloud.dto;

import java.io.Serializable;
import java.util.Date;

public class DateResponseDTO implements Serializable {

    private Date date;

    public DateResponseDTO(Date date) {
        this.date = date;
    }

    public DateResponseDTO() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
