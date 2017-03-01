/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.gateway.useraccount.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 * Date: 10/9/2015
 * All Rights Reserved
 */

public class OAuthNewTokenResponse implements Serializable {

    private List<String> roles = new ArrayList<>();
    private String clientApplicationID;
    private String redirectUri;
    private String accessCode;


    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getClientApplicationID() {
        return clientApplicationID;
    }

    public void setClientApplicationID(String clientApplicationID) {
        this.clientApplicationID = clientApplicationID;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }
}
