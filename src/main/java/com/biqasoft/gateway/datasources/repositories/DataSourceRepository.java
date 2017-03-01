/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.datasources.repositories;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.constants.DATA_SOURCES;
import com.biqasoft.entity.core.CreatedInfo;
import com.biqasoft.entity.datasources.DataSource;
import com.biqasoft.entity.datasources.SavedDataSource;
import com.biqasoft.entity.filters.DataSourceFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.widgets.Widget;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DataSourceRepository {

    private final MongoTemplate ops;
    private final BiqaObjectFilterService biqaObjectFilterService;

    @Autowired
    public DataSourceRepository(@TenantDatabase MongoTemplate ops, BiqaObjectFilterService biqaObjectFilterService) {
        this.ops = ops;
        this.biqaObjectFilterService = biqaObjectFilterService;
    }

    public List<DataSource> getActualDataSourceObjects(List<DataSource> dataSources) {
        DataSourceFilter builder = new DataSourceFilter();
        builder.setUseListOfIDs(true);
        dataSources.forEach(x -> builder.getListOfIDs().add(x.getId()));

        return this.getDataSourceByFilter(builder).getResultedObjects();
    }

    public Widget getActualWidgetWithDataSource(Widget widget) {
        if (widget.getDataSources().size() == 0) return widget;

        widget.setDataSources(getActualDataSourceObjects(widget.getDataSources()));
        return widget;
    }

    public SavedDataSource saveDataSourceMetric(SavedDataSource dat) {
        dat.setCreatedInfo(new CreatedInfo(new Date()));
        ops.save(dat);
        return dat;
    }

    public BiqaPaginationResultList<DataSource> getDataSourceByFilter(DataSourceFilter filter) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(filter);
        Query query = biqaObjectFilterService.getQueryFromFilter(filter, criteria);

        if (filter.isUseListOfIDs()) criteria.and("id").in(filter.getListOfIDs());

        if (filter.isUseResolved()) {
            criteria.and("resolved").is(filter.isResolved());
        }

        if (filter.isSystemIssued()) {
            criteria.and("systemIssued").is(filter.isSystemIssued());
        }

        if (filter.isUseLightStatus()) {
            criteria.and("lights.currentLight").is(filter.getLightStatus());
        }

        if (filter.isUseControlledClass()) {
            criteria.and("controlledClass").is(filter.getControlledClass());
        }

        if (filter.isUseReturnType()) {
            criteria.and("returnType").is(filter.getReturnType());
        }

        return biqaObjectFilterService.getPaginationResultList(filter, criteria, query, DataSource.class, ops);
    }

    @BiqaAuditObject
    public DataSource updateDataSourceSavedData(DataSource dataSource) {
        return biqaObjectFilterService.safeUpdate(dataSource, ops);
    }

    public void deleteDataSourceSavedData(DataSource dataSource) {
        // check that data source can not be deleted
        // if some customers have salesFunnelStatus which have this dataSource
        if (dataSource.getControlledClass().equals(DATA_SOURCES.CUSTOMERS_OR_LEADS_NUMBER_BY_SALES_FUNNEL_STATUS) && dataSource.getValues().getIntVal() > 0) {
            ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("sales_funnel.delete_with_existing_customers");
        }

        ops.remove(dataSource);
    }

    @BiqaAddObject
    @BiqaAuditObject
    public DataSource addNewDataSourceSavedData(DataSource dataSource) {
        ops.insert(dataSource);
        return dataSource;
    }

    public DataSource findDataSourceSavedDataById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id)), DataSource.class);
    }

    public List<DataSource> findAllDataSourceSavedData() {
        return ops.findAll(DataSource.class);
    }

}
