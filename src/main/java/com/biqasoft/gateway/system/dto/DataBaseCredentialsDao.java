/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.system.dto;

import org.javers.core.metamodel.annotation.Value;

import java.io.Serializable;

@Value
public class DataBaseCredentialsDao implements Serializable {

    private String login;
    private String password;
    private String authDataBase;

    private boolean success;


    public String getAuthDataBase() {
        return authDataBase;
    }

    public void setAuthDataBase(String authDataBase) {
        this.authDataBase = authDataBase;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
