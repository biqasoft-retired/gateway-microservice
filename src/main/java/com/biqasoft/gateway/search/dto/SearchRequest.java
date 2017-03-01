/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.search.dto;

import java.io.Serializable;

public class SearchRequest implements Serializable {

    private String text;


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
