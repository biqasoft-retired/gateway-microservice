/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.objects.custom.data.dto;

import com.biqasoft.entity.filters.CustomObjectsDataFilter;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 2/20/2016.
 * All Rights Reserved
 */
public class RequestPrintableBuilder {

    private CustomObjectsDataFilter customObjectsDataBuilder = null;

    private String viewId = null;

    private String requestedMimeType;
    private String requestedExtension;


    public String getRequestedMimeType() {
        return requestedMimeType;
    }

    public void setRequestedMimeType(String requestedMimeType) {
        this.requestedMimeType = requestedMimeType;
    }

    public String getRequestedExtension() {
        return requestedExtension;
    }

    public void setRequestedExtension(String requestedExtension) {
        this.requestedExtension = requestedExtension;
    }

    public CustomObjectsDataFilter getCustomObjectsDataBuilder() {
        return customObjectsDataBuilder;
    }

    public void setCustomObjectsDataBuilder(CustomObjectsDataFilter customObjectsDataBuilder) {
        this.customObjectsDataBuilder = customObjectsDataBuilder;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }
}
