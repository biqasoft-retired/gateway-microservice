/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.notification.dto.android.push;

import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document
public class AndroidPushData implements Serializable {

    private String score;
    private String message;


    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
