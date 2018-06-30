/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.search.dto;

import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.customer.LeadGenMethod;
import com.biqasoft.entity.customer.LeadGenProject;
import com.biqasoft.entity.datasources.DataSource;
import com.biqasoft.storage.entity.StorageFile;
import com.biqasoft.entity.dto.httpresponse.CustomObjectSearchResult;
import com.biqasoft.entity.tasks.Task;
import com.biqasoft.users.domain.useraccount.UserAccount;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SearchResult implements Serializable {

    private List<Customer> customers = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private List<StorageFile> documents = new ArrayList<>();
    private List<UserAccount> userAccounts = new ArrayList<>();
    private List<DataSource> dataSources = new ArrayList<>();
    private List<LeadGenMethod> leadGenMethods = new ArrayList<>();
    private List<LeadGenProject> leadGenProjects = new ArrayList<>();

    private long resultNumber = 0;

    private CustomObjectSearchResult customObjectSearchResult = new CustomObjectSearchResult();


    public CustomObjectSearchResult getCustomObjectSearchResult() {
        return customObjectSearchResult;
    }

    public void setCustomObjectSearchResult(CustomObjectSearchResult customObjectSearchResult) {
        this.customObjectSearchResult = customObjectSearchResult;
    }

    public long getResultNumber() {
        return resultNumber;
    }

    public void setResultNumber(long resultNumber) {
        this.resultNumber = resultNumber;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<StorageFile> getDocuments() {
        return documents;
    }

    public void setDocuments(List<StorageFile> documents) {
        this.documents = documents;
    }

    public List<UserAccount> getUserAccounts() {
        return userAccounts;
    }

    public void setUserAccounts(List<UserAccount> userAccounts) {
        this.userAccounts = userAccounts;
    }

    public List<DataSource> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    public List<LeadGenMethod> getLeadGenMethods() {
        return leadGenMethods;
    }

    public void setLeadGenMethods(List<LeadGenMethod> leadGenMethods) {
        this.leadGenMethods = leadGenMethods;
    }

    public List<LeadGenProject> getLeadGenProjects() {
        return leadGenProjects;
    }

    public void setLeadGenProjects(List<LeadGenProject> leadGenProjects) {
        this.leadGenProjects = leadGenProjects;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}
