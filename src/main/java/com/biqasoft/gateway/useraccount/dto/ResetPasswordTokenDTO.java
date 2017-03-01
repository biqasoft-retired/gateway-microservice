/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.useraccount.dto;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

public class ResetPasswordTokenDTO implements Serializable {

    private String id = new ObjectId().toString();

    private String email;
    private String randomString;
    private String password;


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRandomString() {
        return randomString;
    }

    public void setRandomString(String randomString) {
        this.randomString = randomString;
    }
}
