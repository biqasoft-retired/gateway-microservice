/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.notification.dto.android.push;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Document
public class AndroidPushRoot implements Serializable {

    @Id
    private String id;

    private String header;
    private List<String> registration_ids = new ArrayList<>();
    private AndroidPushData data;


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

    public List<String> getRegistration_ids() {
        return registration_ids;
    }

    public void setRegistration_ids(List<String> registration_ids) {
        this.registration_ids = registration_ids;
    }

    public AndroidPushData getData() {
        return data;
    }

    public void setData(AndroidPushData data) {
        this.data = data;
    }
}
