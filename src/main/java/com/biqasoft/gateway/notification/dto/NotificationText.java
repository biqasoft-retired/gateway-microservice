/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.notification.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.biqasoft.entity.core.CreatedInfo;

import java.io.Serializable;

@Document
public class NotificationText implements Serializable {

    @Id
    private String id;

    private String header;
    private String shortText;
    private String fullText;
    private CreatedInfo createdInfo;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public CreatedInfo getCreatedInfo() {
        return createdInfo;
    }

    public void setCreatedInfo(CreatedInfo createdInfo) {
        this.createdInfo = createdInfo;
    }
}
