/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.leadgen.repositories;

import com.biqasoft.entity.analytics.UTMAllMetricInfo;
import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.constants.DATA_SOURCES;
import com.biqasoft.entity.constants.SALES_FUNNEL;
import com.biqasoft.entity.constants.SYSTEM_FIELDS_CONST;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.entity.customer.LeadGenMethod;
import com.biqasoft.entity.customer.LeadGenProject;
import com.biqasoft.entity.datasources.DataSource;
import com.biqasoft.entity.datasources.SavedLeadGenKPI;
import com.biqasoft.entity.filters.LeadGenMethodFilter;
import com.biqasoft.entity.filters.LeadGenProjectFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.payments.CompanyCost;
import com.biqasoft.entity.salesfunnel.SalesFunnel;
import com.biqasoft.entity.salesfunnel.SalesFunnelStatus;
import com.biqasoft.entity.system.NameValueMap;
import com.biqasoft.gateway.customer.repositories.SalesFunnelRepository;
import com.biqasoft.gateway.datasources.repositories.DataSourceRepository;
import com.biqasoft.gateway.payments.repositories.PaymentsRepository;
import com.biqasoft.microservice.common.MicroserviceDomainSettings;
import com.biqasoft.microservice.database.MongoTenantHelper;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeadGenRepository {

    private final MongoOperations ops;
    private final BiqaObjectFilterService biqaObjectFilterService;
    private final DataSourceRepository dataSourceAllData;
    private final SalesFunnelRepository salesFunnelRepository;
    private PaymentsRepository paymentsRepository;
    private final MongoTenantHelper mongoTenantHelper;
    private final MicroserviceDomainSettings microserviceDomainSettings;

    @Autowired
    public LeadGenRepository(DataSourceRepository dataSourceAllData, BiqaObjectFilterService biqaObjectFilterService,
                             @TenantDatabase MongoOperations ops, SalesFunnelRepository salesFunnelRepository,
                             MongoTenantHelper mongoTenantHelper, MicroserviceDomainSettings microserviceDomainSettings) {
        this.dataSourceAllData = dataSourceAllData;
        this.biqaObjectFilterService = biqaObjectFilterService;
        this.ops = ops;
        this.salesFunnelRepository = salesFunnelRepository;
        this.mongoTenantHelper = mongoTenantHelper;
        this.microserviceDomainSettings = microserviceDomainSettings;
    }

    @Autowired
    public void setPaymentsRepository(PaymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    public List<LeadGenProject> findAllLeadGenProject() {
        return ops.findAll(LeadGenProject.class);
    }

    public LeadGenProject findLeadGenProjectByPromoCode(String promoCode) {
        return ops.findOne(Query.query(Criteria.where("promoCodes").in(promoCode)), LeadGenProject.class);
    }

    public LeadGenProject findLeadGenProjectByUTMMetrics(String domain, UTMAllMetricInfo utmAllMetricInfo) {
        List<LeadGenProject> leadGenProjects = mongoTenantHelper.domainDataBaseUnsafeGet(domain).findAll(LeadGenProject.class);

        for (LeadGenProject leadGenProject : leadGenProjects) {
            for (UTMAllMetricInfo currentUTMinProject : leadGenProject.getUtm_metrics()) {
                if (currentUTMinProject.getUtm_source().equals(utmAllMetricInfo.getUtm_source()) &&
                        currentUTMinProject.getUtm_medium().equals(utmAllMetricInfo.getUtm_medium()) &&
                        currentUTMinProject.getUtm_campaign().equals(utmAllMetricInfo.getUtm_campaign())
                        )
                    return leadGenProject;
            }
        }
        // if we here that's mean than we can't find project id by this utm - we should use default lead gen method and project
        DomainSettings domainSettings = microserviceDomainSettings.unsafeFindDomainSettingsById(domain);
        LeadGenProject leadGenProject = findLeadGenProjectByIdWithoutDomainCheckingSecurity(domainSettings.getDefaultLeadGenProjectID(), domain);

        return leadGenProject;
    }

    public List<LeadGenMethod> findAllLeadGenMethod() {
        return ops.findAll(LeadGenMethod.class);
    }

    public LeadGenMethod findLeadGenMethodById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id)), LeadGenMethod.class);
    }

    public SavedLeadGenKPI findLeadGenMethodHistoryDataLatest(String leadGenMethodId, Date startDate, Date finalDate) {
        Criteria criteria = new Criteria();
        criteria.and("leadGenMethodId").is(leadGenMethodId);

        if (startDate != null && finalDate == null) criteria.and("createdInfo.createdDate").gte(startDate);
        if (startDate == null && finalDate != null) criteria.and("createdInfo.createdDate").lte(finalDate);
        if (startDate != null && finalDate != null)
            criteria.and("createdInfo.createdDate").gte(startDate).lte(finalDate);

        Query query = new Query(criteria);
        query.limit(1);
        query.with(new Sort(Sort.Direction.DESC, "createdInfo.createdDate"));

        return ops.findOne(query, SavedLeadGenKPI.class);
    }

    public List<SavedLeadGenKPI> findLeadGenMethodHistoryData(String leadGenMethodId, Date startDate, Date finalDate) {
        Criteria criteria = new Criteria();
        criteria.and("leadGenMethodId").is(leadGenMethodId);

        if (startDate != null && finalDate == null) criteria.and("createdInfo.createdDate").gte(startDate);
        if (startDate == null && finalDate != null) criteria.and("createdInfo.createdDate").lte(finalDate);
        if (startDate != null && finalDate != null)
            criteria.and("createdInfo.createdDate").gte(startDate).lte(finalDate);

        return ops.find(Query.query(criteria), SavedLeadGenKPI.class);
    }

    public List<SavedLeadGenKPI> findLeadGenProjectHistoryData(String leadGenProjectId, Date startDate, Date finalDate) {
        Criteria criteria = new Criteria();
        criteria.and("leadGenProjectId").is(leadGenProjectId);

        if (startDate != null && finalDate == null) criteria.and("createdInfo.createdDate").gte(startDate);
        if (startDate == null && finalDate != null) criteria.and("createdInfo.createdDate").lte(finalDate);
        if (startDate != null && finalDate != null)
            criteria.and("createdInfo.createdDate").gte(startDate).lte(finalDate);

        return ops.find(Query.query(criteria), SavedLeadGenKPI.class);
    }

    public SavedLeadGenKPI findLeadGenProjectHistoryDataLatest(String leadGenProjectId, Date startDate, Date finalDate) {
        Criteria criteria = new Criteria();
        criteria.and("leadGenProjectId").is(leadGenProjectId);

        if (startDate != null && finalDate == null) criteria.and("createdInfo.createdDate").gte(startDate);
        if (startDate == null && finalDate != null) criteria.and("createdInfo.createdDate").lte(finalDate);
        if (startDate != null && finalDate != null)
            criteria.and("createdInfo.createdDate").gte(startDate).lte(finalDate);

        Query query = new Query(criteria);
        query.limit(1);
        query.with(new Sort(Sort.Direction.DESC, "createdInfo.createdDate"));

        return ops.findOne(query, SavedLeadGenKPI.class);
    }

    /**
     * used for lead2web form
     */
    public LeadGenMethod findLeadGenMethodByIdWithoutDomainCheckingSecurity(String id, String domain) {
        return mongoTenantHelper.domainDataBaseUnsafeGet(domain).findOne(Query.query(Criteria.where("id").is(id)), LeadGenMethod.class);
    }

    /**
     * used for lead2web form
     */
    public LeadGenProject findLeadGenProjectByIdWithoutDomainCheckingSecurity(String id, String domain) {
        return mongoTenantHelper.domainDataBaseUnsafeGet(domain).findOne(Query.query(Criteria.where("id").is(id)), LeadGenProject.class);
    }

    public LeadGenProject findLeadGenProjectById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id)), LeadGenProject.class);
    }

    @BiqaAddObject
    @BiqaAuditObject
    public LeadGenProject addLeadGenProject(LeadGenProject leadGenProject) {

        leadGenProject.setStartDate(new Date());
        ops.insert(leadGenProject);

        String leadGenMethod = leadGenProject.getLeadGenMethodId();
        if (leadGenMethod != null) {
            LeadGenMethod method = findLeadGenMethodById(leadGenMethod);
            if (method != null) {
                method.getLeadGenProjects().add(leadGenProject);
                dirtySaveLeadGenMethod(method);
            }
        }

        return leadGenProject;
    }

    @BiqaAddObject
    @BiqaAuditObject
    public LeadGenMethod addLeadGenMethod(LeadGenMethod leadGenMethod) {
        leadGenMethod.setStartDate(new Date());

        // if we have not predefined sales funnel - create new
        if (leadGenMethod.getLeadGenSalesFunnel() == null) {
            SalesFunnel leadGenSalesFunnel = new SalesFunnel();
            List<SalesFunnelStatus> leadGenSalesFunnelStatuses = new ArrayList<>();
            leadGenSalesFunnel.setSystemIssued(true);

            leadGenSalesFunnel.setName("Воронка LG");
            leadGenSalesFunnel.setPhase(SALES_FUNNEL.LEAD_GEN_SALES_FUNNEL);
            leadGenSalesFunnel.setSalesFunnelStatuses(leadGenSalesFunnelStatuses);
            salesFunnelRepository.addSalesFunnel(leadGenSalesFunnel);
            leadGenMethod.setLeadGenSalesFunnel(leadGenSalesFunnel);
        }

        // if we have not predefined sales funnel - create new
        if (leadGenMethod.getLeadConversionSalesFunnel() == null) {
            SalesFunnel leadConversionSalesFunnel = new SalesFunnel();
            List<SalesFunnelStatus> leadGenSalesFunnelStatusesLC = new ArrayList<>();
            leadConversionSalesFunnel.setSystemIssued(true);

            leadConversionSalesFunnel.setName("Воронка LC");
            leadConversionSalesFunnel.setPhase(SALES_FUNNEL.LEAD_CONVERCIAL_SALES_FUNNEL);
            leadConversionSalesFunnel.setSalesFunnelStatuses(leadGenSalesFunnelStatusesLC);
            salesFunnelRepository.addSalesFunnel(leadConversionSalesFunnel);
            leadGenMethod.setLeadConversionSalesFunnel(leadConversionSalesFunnel);
        }

        // if we have not predefined sales funnel - create new
        if (leadGenMethod.getAccountManagementSalesFunnel() == null) {
            SalesFunnel amSalesFunnel = new SalesFunnel();
            List<SalesFunnelStatus> amSalesFunnelStatuses = new ArrayList<>();
            amSalesFunnel.setSystemIssued(true);

            amSalesFunnel.setName("Воронка AM");
            amSalesFunnel.setPhase(SALES_FUNNEL.ACCOUNT_MANAGEMENT_SALES_FUNNEL);
            amSalesFunnel.setSalesFunnelStatuses(amSalesFunnelStatuses);
            salesFunnelRepository.addSalesFunnel(amSalesFunnel);
            leadGenMethod.setAccountManagementSalesFunnel(amSalesFunnel);
        }

        ops.insert(leadGenMethod);
        return leadGenMethod;
    }

    @BiqaAuditObject
    public LeadGenMethod updateLeadGenMethod(LeadGenMethod leadGenMethod) {
        salesFunnelRepository.updateSalesFunnel(leadGenMethod.getAccountManagementSalesFunnel());
        salesFunnelRepository.updateSalesFunnel(leadGenMethod.getLeadGenSalesFunnel());
        salesFunnelRepository.updateSalesFunnel(leadGenMethod.getLeadConversionSalesFunnel());
        return biqaObjectFilterService.safeUpdate(leadGenMethod, ops);
    }

    private void dirtySaveLeadGenMethod(LeadGenMethod leadGenMethod) {
        ops.save(leadGenMethod);
    }

    public LeadGenProject updateLeadGenProject(LeadGenProject project) {
        LeadGenProject oldLEadGenProject = findLeadGenProjectById(project.getId());

        if (project.getYandexDirectCompaignsIds().size() > 0) {
            List<Integer> oldYandexDirect = oldLEadGenProject.getYandexDirectCompaignsIds();

            List<Integer> newYandexDirectCamapigns = new ArrayList<>();
            newYandexDirectCamapigns.addAll(project.getYandexDirectCompaignsIds());

            for (Integer campaignId : project.getYandexDirectCompaignsIds()) {
                if (oldLEadGenProject.getYandexDirectCompaignsIds().contains(campaignId)) {
                    newYandexDirectCamapigns.remove(campaignId);
                }
            }

            for (Integer yandexId : newYandexDirectCamapigns) {
                CompanyCost companyCost = new CompanyCost();
                companyCost.setDynamicControlled(true);
                companyCost.setLeadGenProjectId(project.getId());
//                companyCost.setLeadGenMethodId( project.getId() );
                companyCost.setYandexDirectCampaignId(yandexId);
                companyCost.setName("Реклама в директе по компании с ID: " + yandexId);

                paymentsRepository.addCompanyCost(companyCost);

//                for (  String cost : project.getCostsIDs() ){
//                    paymentsRepository.findCompanyCostById( cost );
//                }

                project.getCostsIDs().add(companyCost.getId());
            }
        }

        if (!oldLEadGenProject.getUtm_metrics().equals(project.getUtm_metrics())) {
            List<UTMAllMetricInfo> deffStatuses = (List<UTMAllMetricInfo>) org.apache.commons.collections.CollectionUtils.disjunction(project.getUtm_metrics(), oldLEadGenProject.getUtm_metrics());
            for (UTMAllMetricInfo utmAllMetricInfo : deffStatuses) {

                DataSource data = new DataSource();
                data.setControlledClass(DATA_SOURCES.WEB_ANALYTICS_SITE_UTM_COUNT_WITH_ACTION);

                List<NameValueMap> list = new ArrayList<>();
                NameValueMap map1 = new NameValueMap();
                map1.setName("utm_source");
                map1.setValue(utmAllMetricInfo.getUtm_source());

                NameValueMap map2 = new NameValueMap();
                map2.setName("utm_medium");
                map2.setValue(utmAllMetricInfo.getUtm_medium());

                NameValueMap map3 = new NameValueMap();
                map3.setName("utm_campaign");
                map3.setValue(utmAllMetricInfo.getUtm_campaign());

                NameValueMap map4 = new NameValueMap();
                map4.setName("action");
                map4.setValue("pageView");

                list.add(map1);
                list.add(map2);
                list.add(map3);
                list.add(map4);

                data.setName("Просмотров по UTM " + utmAllMetricInfo.getUtm_source() + " " + utmAllMetricInfo.getUtm_medium() + " " + utmAllMetricInfo.getUtm_campaign());
                data.setParameters(list);

                dataSourceAllData.addNewDataSourceSavedData(data);
            }
        }

        project = biqaObjectFilterService.safeUpdate(project, ops);

        // update leadGenProject in method
        try {
            String leadGenMethod = project.getLeadGenMethodId();
            if (leadGenMethod != null) {
                LeadGenMethod method = findLeadGenMethodById(leadGenMethod);
                if (method != null) {
                    final String projectId = project.getId();
                    List<LeadGenProject> leadGenProject = method.getLeadGenProjects().stream().filter(x -> x.getId().equals(projectId)).collect(Collectors.toList());
                    method.setLeadGenProjects(leadGenProject);
                    dirtySaveLeadGenMethod(method);

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return project;
    }

    public BiqaPaginationResultList<LeadGenMethod> getLeadGenMethodByFilter(LeadGenMethodFilter filter) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(filter);
        Query query = biqaObjectFilterService.getQueryFromFilter(filter, criteria);

        if (filter.isUseListOfIDs()) criteria.and("id").in(filter.getListOfIDs());

        return biqaObjectFilterService.getPaginationResultList(filter, criteria, query, LeadGenMethod.class, ops);
    }

    public BiqaPaginationResultList<LeadGenProject> getLeadGeProjectByFilter(LeadGenProjectFilter filter) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(filter);
        Query query = biqaObjectFilterService.getQueryFromFilter(filter, criteria);

        if (filter.isUseListOfIDs()) criteria.and("id").in(filter.getListOfIDs());

        if (filter.getLeadGenMethodID() != null && !filter.getLeadGenMethodID().equals(SYSTEM_FIELDS_CONST.ANY))
            criteria.and("leadGenMethodId").is(filter.getLeadGenMethodID());

        return biqaObjectFilterService.getPaginationResultList(filter, criteria, query, LeadGenProject.class, ops);
    }

}
