/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.notification.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document
public abstract class NotificationMethod implements Serializable {

    @Id
    private String id;

    private String text;
    private String method;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
