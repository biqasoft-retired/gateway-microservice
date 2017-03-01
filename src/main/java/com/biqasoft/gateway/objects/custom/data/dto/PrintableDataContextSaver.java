/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.objects.custom.data.dto;

import com.biqasoft.entity.objects.CustomObjectPrintableTemplate;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 2/20/2016.
 * All Rights Reserved
 */
public class PrintableDataContextSaver {

    private byte[] bytes;

    private CustomObjectPrintableTemplate printableTemplate;
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

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public CustomObjectPrintableTemplate getPrintableTemplate() {
        return printableTemplate;
    }

    public void setPrintableTemplate(CustomObjectPrintableTemplate printableTemplate) {
        this.printableTemplate = printableTemplate;
    }
}
