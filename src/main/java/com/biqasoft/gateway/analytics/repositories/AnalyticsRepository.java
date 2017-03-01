/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.analytics.repositories;

import com.biqasoft.entity.analytics.AnalyticRecord;
import com.biqasoft.entity.analytics.UTMAllMetricInfo;
import com.biqasoft.entity.analytics.WebAnalyticsCounter;
import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.datasources.DataSource;
import com.biqasoft.entity.filters.WebAnalyticsCounterFilter;
import com.biqasoft.entity.filters.WebAnalyticsRecordFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.system.NameValueMap;
import com.biqasoft.gateway.datasources.repositories.DataSourceRepository;
import com.biqasoft.microservice.database.MainDatabase;
import com.biqasoft.microservice.database.MongoTenantHelper;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsRepository {

    private final MongoOperations ops;
    private final MongoOperations mainDataBase;
    private final CurrentUser currentUser;
    private final DataSourceRepository sourceSavedDataRepo;
    private final BiqaObjectFilterService biqaObjectFilterService;
    private final MongoTenantHelper mongoTenantHelper;

    @Autowired
    public AnalyticsRepository(@MainDatabase MongoOperations mainDataBase, CurrentUser currentUser, DataSourceRepository sourceSavedDataRepo,
                               @TenantDatabase MongoOperations ops, BiqaObjectFilterService biqaObjectFilterService, MongoTenantHelper mongoTenantHelper) {
        this.mainDataBase = mainDataBase;
        this.currentUser = currentUser;
        this.sourceSavedDataRepo = sourceSavedDataRepo;
        this.ops = ops;
        this.biqaObjectFilterService = biqaObjectFilterService;
        this.mongoTenantHelper = mongoTenantHelper;
    }

    public AnalyticRecord addAnalyticRecord(AnalyticRecord analyticRecord) {
        mongoTenantHelper.domainDataBaseUnsafeGet(analyticRecord.getDomain()).insert(analyticRecord);
        return analyticRecord;
    }

    // TODO: check permission
    @BiqaAddObject
    @BiqaAuditObject
    public WebAnalyticsCounter addAnalyticCounter(WebAnalyticsCounter analyticRecord) {
        analyticRecord.setElp(true);

        analyticRecord.setDomain(currentUser.getDomain().getDomain());

        analyticRecord.setId(new ObjectId().toString());
        DataSource data = new DataSource();
        data.setSystemIssued(true);
        data.setControlledClass("WEB_ANALYTICS_SITE_COUNTER_ALL");
        data.setName("Всего записей в веб-счетчике: " + analyticRecord.getId());

        List<NameValueMap> paramentres = new ArrayList<>();
        NameValueMap an = new NameValueMap();
        an.setName("counterId");
        an.setValue(analyticRecord.getId());

        paramentres.add(an);
        data.setParameters(paramentres);
        sourceSavedDataRepo.addNewDataSourceSavedData(data);

        mainDataBase.insert(analyticRecord);
        return analyticRecord;
    }

    // TODO: check permission
    public WebAnalyticsCounter updateAnalyticsCounter(WebAnalyticsCounter webAnalyticsCounter) {
        if (findWebAnalyticsCounter(webAnalyticsCounter.getId()).getDomain().equals(currentUser.getDomain().getDomain())) {
            mainDataBase.save(webAnalyticsCounter);
        }
        return webAnalyticsCounter;
    }

    @Deprecated
    public AnalyticRecord findAnalyticRecordById(String id) {
        return ops.findOne(Query.query(Criteria
                .where("id").is(id)
        ), AnalyticRecord.class);
    }

    @Deprecated
    public AnalyticRecord updateAnalyticRecord(AnalyticRecord customer) {
        ops.save(customer);
        return customer;
    }

    @Deprecated
    public List<AnalyticRecord> findAllAnalyticRecords() {
        return ops.findAll(AnalyticRecord.class);
    }

    @Deprecated
    public List<AnalyticRecord> findAllAnalyticRecordsWithUtmAndAndAction(UTMAllMetricInfo utm, String action) {
        return ops.find(Query.query(Criteria
                .where("action").is(action)
                .and("utm.utm_source").is(utm.getUtm_source())
                .and("utm.utm_campaign").is(utm.getUtm_campaign())
                .and("utm.utm_medium").is(utm.getUtm_medium())
        ), AnalyticRecord.class);
    }

    public List<AnalyticRecord> findAllAnalyticRecordsByCookiesIds(List<String> cookiesIds) {
        return ops.find(Query.query(Criteria
                .where("userCookieId").in(cookiesIds)
        ), AnalyticRecord.class);
    }

    public List<AnalyticRecord> findAllAnalyticRecordsByCounterId(String id) {
        return ops.find(Query.query(Criteria
                .where("counterId").is(id)
        ), AnalyticRecord.class);
    }

    /**
     * available only for current domain
     * @return
     */
    public List<WebAnalyticsCounter> findAllAnalyticsCounters() {
        return mainDataBase.find(Query.query(Criteria
                .where("domain").is(currentUser.getDomain().getDomain())
        ), WebAnalyticsCounter.class);
    }

    /**
     * Important Note
     * AnyOne from internet can access this
     * @param id
     * @return
     */
    public WebAnalyticsCounter findWebAnalyticsCounter(String id) {
        return mainDataBase.findOne(Query.query(Criteria
                .where("id").is(id)
        ), WebAnalyticsCounter.class);
    }

    public BiqaPaginationResultList getWebAnalyticsRecordByFilter(WebAnalyticsRecordFilter filter) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(filter);
        Query query = biqaObjectFilterService.getQueryFromFilter(filter, criteria);

        BiqaPaginationResultList<AnalyticRecord> biqaPaginationResultList = new BiqaPaginationResultList<>();

        if (filter.isUseListOfIDs()) criteria.and("id").in(filter.getListOfIDs());

        if (filter.isUseCounterId()) criteria.and("counterId").is(filter.getConterID());

        if (filter.isUseUtm_source()) criteria.and("utm.utm_source").is(filter.getUtm_source());
        if (filter.isUseUtm_campaign()) criteria.and("utm.utm_campaign").is(filter.getUtm_campaign());
        if (filter.isUseUtm_medium()) criteria.and("utm.utm_medium").is(filter.getUtm_medium());
        if (filter.isUseAction()) criteria.and("action").is(filter.getAction());

        // if we want to only count resulted diffs - we do it and return result without objects
        if (filter.isOnlyCount()) {
            biqaPaginationResultList.setEntityNumber(biqaObjectFilterService.countResultFromCriteria(filter, criteria, AnalyticRecord.class, ops));
            return biqaPaginationResultList;
        }

        biqaPaginationResultList.setResultedObjects(ops.find(query, AnalyticRecord.class));

        long resultEntity = 0;

        // if we don't use pagination, we can just count resulted objects and don't do extra query
        if (filter.isUsePagination()) {
            resultEntity = biqaObjectFilterService.countResultFromCriteria(filter, criteria, AnalyticRecord.class, ops);
        } else {
            resultEntity = biqaPaginationResultList.getResultedObjects().size();
        }

        biqaPaginationResultList.setEntityNumber(resultEntity);

        return biqaPaginationResultList;
    }

    public BiqaPaginationResultList<WebAnalyticsCounter> getWebAnalyticsCounterByFilter(WebAnalyticsCounterFilter filter) {

        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(filter);
        criteria.and("domain").is(currentUser.getDomain().getDomain());
        Query query = biqaObjectFilterService.getQueryFromFilter(filter, criteria);

        BiqaPaginationResultList<WebAnalyticsCounter> biqaPaginationResultList = new BiqaPaginationResultList<>();

        if (filter.isUseListOfIDs()) criteria.and("id").in(filter.getListOfIDs());

        // if we want to only count resulted diffs - we do it and return result without objects
        if (filter.isOnlyCount()) {
            biqaPaginationResultList.setEntityNumber(biqaObjectFilterService.countResultFromCriteria(filter, criteria, WebAnalyticsCounter.class, ops));
            return biqaPaginationResultList;
        }

        biqaPaginationResultList.setResultedObjects(mainDataBase.find(query, WebAnalyticsCounter.class));

        long resultEntity = 0;

        // if we don't use pagination, we can just count resulted objects and don't do extra query
        if (filter.isUsePagination()) {
            resultEntity = biqaObjectFilterService.countResultFromCriteria(filter, criteria, WebAnalyticsCounter.class, ops);
        } else {
            resultEntity = biqaPaginationResultList.getResultedObjects().size();
        }

        biqaPaginationResultList.setEntityNumber(resultEntity);

        return biqaPaginationResultList;
    }

}
