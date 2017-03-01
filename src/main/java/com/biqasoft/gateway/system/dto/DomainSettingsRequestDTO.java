/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.system.dto;

import com.biqasoft.entity.core.DomainSettings;

import java.io.Serializable;

public class DomainSettingsRequestDTO implements Serializable {

    private DomainSettings domainSettings;
    private boolean updateCurrentAdditionalFields = false;


    public DomainSettings getDomainSettings() {
        return domainSettings;
    }

    public void setDomainSettings(DomainSettings domainSettings) {
        this.domainSettings = domainSettings;
    }

    public boolean isUpdateCurrentAdditionalFields() {
        return updateCurrentAdditionalFields;
    }

    public void setUpdateCurrentAdditionalFields(boolean updateCurrentAdditionalFields) {
        this.updateCurrentAdditionalFields = updateCurrentAdditionalFields;
    }
}
