/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.search;

import com.biqasoft.entity.datasources.DataSource;
import com.biqasoft.gateway.objects.custom.template.CustomObjectsRepository;
import com.biqasoft.gateway.search.dto.SearchRequest;
import com.biqasoft.gateway.search.dto.SearchResult;
import com.biqasoft.microservice.common.MicroserviceUsersRepository;
import com.biqasoft.microservice.database.TenantDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Service;
import com.biqasoft.gateway.objects.custom.data.CustomObjectsDataRepository;
import com.biqasoft.entity.filters.CustomObjectsFilter;
import com.biqasoft.entity.filters.CustomObjectsDataFilter;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.customer.LeadGenMethod;
import com.biqasoft.entity.customer.LeadGenProject;
import com.biqasoft.storage.entity.StorageFile;
import com.biqasoft.entity.dto.httpresponse.CustomObjectSearchResult;
import com.biqasoft.entity.dto.httpresponse.CustomObjectSearchResultNode;
import com.biqasoft.entity.core.objects.CustomObjectData;
import com.biqasoft.entity.objects.CustomObjectTemplate;
import com.biqasoft.entity.tasks.Task;
import com.biqasoft.entity.core.useraccount.UserAccount;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Search objects in database using full-text search of mongodb
 * in current domain
 * searching using `searchAll` method
 * search in userAccounts, documents, tasks... custom objects etc...
 */
@Service
public class SearchRepository {

    private final MongoOperations tenant;
    private final CustomObjectsDataRepository customObjectsDataRepository;
    private final CustomObjectsRepository customObjectsRepository;
    private final MicroserviceUsersRepository microserviceUsersRepository;

    @Autowired
    public SearchRepository(CustomObjectsDataRepository customObjectsDataRepository,
                            @TenantDatabase MongoOperations tenant, CustomObjectsRepository customObjectsRepository, MicroserviceUsersRepository microserviceUsersRepository) {
        this.customObjectsDataRepository = customObjectsDataRepository;
        this.tenant = tenant;
        this.customObjectsRepository = customObjectsRepository;
        this.microserviceUsersRepository = microserviceUsersRepository;
    }

    public SearchResult searchAll(SearchRequest searchRequest) {
        SearchResult searchResult = new SearchResult();

        searchResult.setCustomers(findAllCustomers(searchRequest));
        searchResult.setTasks(findAllTasks(searchRequest));
        searchResult.setUserAccounts(findAllUserAccounts(searchRequest));
        searchResult.setDocuments(findAllDocumentFile(searchRequest));
        searchResult.setDataSources(findAllDataSourceSavedData(searchRequest));
        searchResult.setLeadGenMethods(findAllLeadGenMethod(searchRequest));
        searchResult.setLeadGenProjects(findAllLeadGenProject(searchRequest));

        CustomObjectSearchResult objectSearchResult = processCustomObjects(searchRequest);
        searchResult.setCustomObjectSearchResult(objectSearchResult);

        long resultNum =
                searchResult.getCustomers().size() +
                        searchResult.getTasks().size() +
                        searchResult.getUserAccounts().size() +
                        searchResult.getDocuments().size() +
                        searchResult.getDataSources().size() +
                        searchResult.getLeadGenMethods().size() +
                        searchResult.getLeadGenProjects().size() +
                        objectSearchResult.getObjectNumber();

        searchResult.setResultNumber(resultNum);

        return searchResult;
    }

    private CustomObjectSearchResult processCustomObjects(SearchRequest searchRequest) {
        CustomObjectSearchResult result = new CustomObjectSearchResult();
        Map<String, CustomObjectSearchResultNode> customObjects = new HashMap<>();
        result.setCustomObjects(customObjects);

        long allObjects = 0;

        CustomObjectsFilter customObjectsBuilder = new CustomObjectsFilter();

        List<CustomObjectTemplate> customObjectTemplates = (List<CustomObjectTemplate>) customObjectsRepository.getCustomObjectTemplateFromBuilder(customObjectsBuilder).getResultedObjects();

        // iterate over all customObjectTemplates
        for (CustomObjectTemplate customObjectTemplate : customObjectTemplates) {

            CustomObjectsDataFilter builder = new CustomObjectsDataFilter();
            builder.setCollectionId(customObjectTemplate.getCollectionId());
            builder.setUseFullTextSearch(true);
            builder.setFullTextSearchRequest(searchRequest.getText());

            List<CustomObjectData> customObjectDatas = (List<CustomObjectData>) customObjectsDataRepository.getCustomObjectTemplateFromFilter(builder).getResultedObjects();

            if (customObjectDatas.size() > 0) {
                CustomObjectSearchResultNode node = new CustomObjectSearchResultNode();
                node.setCustomObjectDataList(customObjectDatas);
                node.setCustomObjectTemplate(customObjectTemplate);

                allObjects += customObjectDatas.size();

                customObjects.put(customObjectTemplate.getCollectionId(), node);
            }

        }
        result.setObjectNumber(allObjects);

        return result;
    }

    private List<Customer> findAllCustomers(SearchRequest searchRequest) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchRequest.getText());
        Query query = TextQuery.queryText(criteria).sortByScore();

        List<Customer> customers = tenant.find(query, Customer.class);
        return customers;
    }

    private List<Task> findAllTasks(SearchRequest searchRequest) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchRequest.getText());
        Query query = TextQuery.queryText(criteria).sortByScore();

        List<Task> customers = tenant.find(query, Task.class);
        return customers;
    }

    private List<UserAccount> findAllUserAccounts(SearchRequest searchRequest) {
        return microserviceUsersRepository.fullTextSearch(searchRequest.getText());
    }

    private List<StorageFile> findAllDocumentFile(SearchRequest searchRequest) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchRequest.getText());
        Query query = TextQuery.queryText(criteria).sortByScore();

        List<StorageFile> customers = tenant.find(query, StorageFile.class);
        return customers;
    }

    private List<DataSource> findAllDataSourceSavedData(SearchRequest searchRequest) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchRequest.getText());
        Query query = TextQuery.queryText(criteria).sortByScore();

        List<DataSource> customers = tenant.find(query, DataSource.class);
        return customers;
    }

    private List<LeadGenMethod> findAllLeadGenMethod(SearchRequest searchRequest) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchRequest.getText());
        Query query = TextQuery.queryText(criteria).sortByScore();

        List<LeadGenMethod> customers = tenant.find(query, LeadGenMethod.class);
        return customers;
    }

    private List<LeadGenProject> findAllLeadGenProject(SearchRequest searchRequest) {
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchRequest.getText());
        Query query = TextQuery.queryText(criteria).sortByScore();

        List<LeadGenProject> customers = tenant.find(query, LeadGenProject.class);
        return customers;
    }

}
