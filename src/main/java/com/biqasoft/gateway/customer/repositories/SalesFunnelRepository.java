/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.customer.repositories;

import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.constants.DATA_SOURCES;
import com.biqasoft.entity.constants.DATA_SOURCES_RETURNED_TYPES;
import com.biqasoft.entity.core.objects.field.DataSourcesTypes;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.customer.LeadGenMethod;
import com.biqasoft.entity.datasources.DataSource;
import com.biqasoft.entity.filters.SalesFunnelFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.salesfunnel.SalesFunnel;
import com.biqasoft.entity.salesfunnel.SalesFunnelStatus;
import com.biqasoft.entity.system.NameValueMap;
import com.biqasoft.gateway.datasources.repositories.DataSourceRepository;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * This is sales funnel which is embedded to {@link LeadGenMethod}
 */
@Service
public class SalesFunnelRepository {

    private final MongoTemplate ops;
    private final DataSourceRepository dataSourceAllData;
    private final BiqaObjectFilterService biqaObjectFilterService;

    @Autowired
    public SalesFunnelRepository(@TenantDatabase MongoTemplate ops, DataSourceRepository dataSourceAllData, BiqaObjectFilterService biqaObjectFilterService) {
        this.ops = ops;
        this.dataSourceAllData = dataSourceAllData;
        this.biqaObjectFilterService = biqaObjectFilterService;
    }

    /**
     * If we add `sales funnel status`
     * without `data source` -
     * we add new `data source` with number of
     * customer with this status and
     * add to sales funnel
     * NOTE: this method may not add new sales funnel status,
     * may or not update status. This method just called on
     * create sales funnel or update sales funnel
     *
     * @param newSalesFunnel
     * @return
     */
    public SalesFunnel addDefaultDataSourceToSalesFunnel(SalesFunnel newSalesFunnel) {
        if (newSalesFunnel == null) return newSalesFunnel;

        for (SalesFunnelStatus status : newSalesFunnel.getSalesFunnelStatuses()) {
            if (status.getDataSource() != null && status.getDataSource().getControlledClass() != null)
                continue;

            DataSource data = new DataSource();
            data.setReturnType(DATA_SOURCES_RETURNED_TYPES.INTEGER);
            data.setSystemIssued(true);

            // simulate that this is new dataSource and resolved
            data.setResolved(true);

            if (data.getValues() == null) {
                data.setValues(new DataSourcesTypes());
            }

            data.getValues().setIntVal(0);
            ////////////////////////////////////////////////////

            List<NameValueMap> parameteres = new ArrayList<>();

            NameValueMap nameValueMap = new NameValueMap();
            nameValueMap.setName("salesFunnelStatusId");
            nameValueMap.setValue(status.getId());

            parameteres.add(nameValueMap);

            data.setParameters(parameteres);
            data.setName(status.getName() + " | " + newSalesFunnel.getName());
            data.setControlledClass(DATA_SOURCES.CUSTOMERS_OR_LEADS_NUMBER_BY_SALES_FUNNEL_STATUS);

            dataSourceAllData.addNewDataSourceSavedData(data);

            status.setDataSource(data);
        }

        updateCustomerStatusSalesFunnelName(newSalesFunnel);
        return newSalesFunnel;
    }

    /**
     * update all customers fields `salesFunnelStatus`
     * to be this field up-to-date
     * this method is invoked when we update leadGenMethod
     * which include sales funnel
     *
     * @param salesFunnel
     * @return salesfunnel
     */
    private SalesFunnel updateCustomerStatusSalesFunnelName(SalesFunnel salesFunnel) {
        SalesFunnel oldSalesFunnel = findById(salesFunnel.getId());
        if (oldSalesFunnel == null) return null;

        for (SalesFunnelStatus status : salesFunnel.getSalesFunnelStatuses()) {

            long hasStatusWithId = oldSalesFunnel.getSalesFunnelStatuses().stream().filter(x -> x.getId().equals(status.getId())).count();
            if (hasStatusWithId < 1) continue;

            //TODO: if we change color - update only color, if name - update only name
            long doesStatusFieldChanged = oldSalesFunnel.getSalesFunnelStatuses().stream().filter(x -> x.getId().equals(status.getId()) && x.getName().equals(status.getName())
                    && x.getColor() != null && x.getColor().equals(status.getColor()))
                    .count();

            if (doesStatusFieldChanged == 0) {
                Query query = new Query(Criteria.where("salesFunnelStatus._id").is(new ObjectId(status.getId())));
                Update update = new Update();
                update.set("salesFunnelStatus.name", status.getName());
                update.set("salesFunnelStatus.color", status.getColor());
                update.set("salesFunnelStatus.description", status.getDescription());
                ops.updateMulti(query, update, Customer.class);
            }
        }
        return salesFunnel;
    }

    @BiqaAddObject
    @BiqaAuditObject
    public SalesFunnel addSalesFunnel(SalesFunnel newSalesFunnel) {
        newSalesFunnel = addDefaultDataSourceToSalesFunnel(newSalesFunnel);
        ops.insert(newSalesFunnel);
        return newSalesFunnel;
    }

    @BiqaAuditObject
    public SalesFunnel updateSalesFunnel(SalesFunnel newSalesFunnel) {
        newSalesFunnel = addDefaultDataSourceToSalesFunnel(newSalesFunnel);
        return biqaObjectFilterService.safeUpdate(newSalesFunnel, ops);
    }

    public SalesFunnel findById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id)), SalesFunnel.class);
    }

    public SalesFunnelStatus findSalesFunnelByStatusId(String id) {
        List<SalesFunnel> salesFunnels = findAll();

        for (SalesFunnel salesFunnel : salesFunnels) {
            for (SalesFunnelStatus salesFunnelStatus : salesFunnel.getSalesFunnelStatuses()) {
                if (salesFunnelStatus.getId().equals(id)) return salesFunnelStatus;
            }
        }
        return null;
    }

    public List<SalesFunnel> findAll() {
        return ops.findAll(SalesFunnel.class);
    }

    public BiqaPaginationResultList<SalesFunnel> getSalesFunnelByFilter(SalesFunnelFilter filter) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(filter);
        Query query = biqaObjectFilterService.getQueryFromFilter(filter, criteria);

        return biqaObjectFilterService.getPaginationResultList(filter, criteria, query, SalesFunnel.class, ops);
    }

}
