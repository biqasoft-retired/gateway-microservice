/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.customer.repositories;

import com.biqasoft.audit.object.BiqaClassService;
import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.annotations.BiqaCheckSecuredModifyObject;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.entity.customer.*;
import com.biqasoft.entity.filters.CustomerFilter;
import com.biqasoft.entity.filters.DealsFilter;
import com.biqasoft.entity.filters.OpportunityFilter;
import com.biqasoft.entity.filters.TaskFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.payments.CustomerDeal;
import com.biqasoft.entity.salesfunnel.AbstractSalesFunnelStatus;
import com.biqasoft.entity.salesfunnel.SalesFunnelStatus;
import com.biqasoft.gateway.leadgen.repositories.LeadGenRepository;
import com.biqasoft.gateway.payments.repositories.PaymentsRepository;
import com.biqasoft.gateway.tasks.repositories.TaskRepository;
import com.biqasoft.microservice.common.MicroserviceDomainSettings;
import com.biqasoft.microservice.database.MongoTenantHelper;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import com.biqasoft.storage.entity.StorageFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerRepository {

    private final MongoOperations ops;
    private final CurrentUser currentUser;
    private LeadGenRepository leadRepository;
    private final BiqaObjectFilterService biqaObjectFilterService;
    private final CustomerFilterRequestContextService customerFilterRequestContextService;
    private final MicroserviceDomainSettings microserviceDomainSettings;
    private final MongoTenantHelper mongoTenantHelper;
    private final BiqaClassService biqaClassService;

    // setters di

    private TaskRepository taskRepository;
    private PaymentsRepository paymentsRepository;
    private OpportunityRepository opportunityRepository;

    @Autowired
    public CustomerRepository(@TenantDatabase MongoOperations ops, BiqaObjectFilterService biqaObjectFilterService,
                              CustomerFilterRequestContextService customerFilterRequestContextService, CurrentUser currentUser, MicroserviceDomainSettings microserviceDomainSettings, MongoTenantHelper mongoTenantHelper, BiqaClassService biqaClassService) {
        this.ops = ops;
        this.biqaObjectFilterService = biqaObjectFilterService;
        this.customerFilterRequestContextService = customerFilterRequestContextService;
        this.currentUser = currentUser;
        this.microserviceDomainSettings = microserviceDomainSettings;
        this.mongoTenantHelper = mongoTenantHelper;
        this.biqaClassService = biqaClassService;
    }

    @Autowired
    public void setLeadRepository(LeadGenRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Autowired
    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Autowired
    public void setPaymentsRepository(PaymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    @Autowired
    public void setOpportunityRepository(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }


    /**
     * used when we detect that changed some objects, connected with customer
     * for example we create new opportunity, connected with customer
     * or done/create new task, connected with customer
     * and want to update customerOverview information
     *
     * @param customerID
     * @return
     */
    public Customer refreshCustomerOverview(String customerID) {
        Customer customer = this.findCustomerOrLeadByID(customerID);
        CustomerOverview customerOverview = customer.getCustomerOverview();

        // task builder
        TaskFilter taskBuilder = new TaskFilter();
        taskBuilder.setOnlyCount(true);
        taskBuilder.setOnlyActive(true);
        taskBuilder.setUseConnectedCustomerId(true);
        taskBuilder.setConnectedCustomerId(customer.getId());

        customerOverview.setActiveTaskNumber(this.taskRepository.getTaskByFilter(taskBuilder).getEntityNumber());
        //@ task builder

        // deals
        DealsFilter dealsBuilder = new DealsFilter();
        List<String> customerIDs = new ArrayList<>();
        customerIDs.add(customer.getId());

        dealsBuilder.setUseCustomerIDs(true);
        dealsBuilder.setCustomerIDs(customerIDs);
        BiqaPaginationResultList<CustomerDeal> dealsList = this.paymentsRepository.getDealsByFilter(dealsBuilder);

        customerOverview.setDealsAmount(this.paymentsRepository.getAmountForPayment(dealsList.getResultedObjects()));
        customerOverview.setDealsNumber(dealsList.getEntityNumber());

        // get last created deal
        if (dealsList.getResultedObjects().size() > 0) {
            try {
                customerOverview.setLastDealDate(
                        dealsList.getResultedObjects().stream()
                                .max((x, y) -> x.getCreatedInfo().getCreatedDate().compareTo(y.getCreatedInfo().getCreatedDate())).get().getCreatedInfo().getCreatedDate()
                );
            } catch (Exception e) {
            }
        }
        //

        // @deals

        OpportunityFilter opportunityBuilder = new OpportunityFilter();
        List<String> customerIDsOpportunity = new ArrayList<>();
        customerIDsOpportunity.add(customer.getId());

        opportunityBuilder.setUseCustomerIDs(true);
        opportunityBuilder.setCustomerIDs(customerIDsOpportunity);
        BiqaPaginationResultList<Opportunity> opportunityBuilderList = this.opportunityRepository.getOpportunitysByFilter(opportunityBuilder);

        // get last created opportunity
        if (opportunityBuilderList.getResultedObjects().size() > 0) {
            try {
                customerOverview.setLastOpportunityDate(
                        opportunityBuilderList.getResultedObjects().stream()
                                .max((x, y) -> x.getCreatedInfo().getCreatedDate().compareTo(y.getCreatedInfo().getCreatedDate())).get().getCreatedInfo().getCreatedDate()
                );
            } catch (Exception e) {
            }
        }
        //

        customerOverview.setOpportunityAmount(this.paymentsRepository.getAmountForPayment(opportunityBuilderList.getResultedObjects()));
        customerOverview.setOpportunityNumber(opportunityBuilderList.getEntityNumber());

        if (customerOverview.getOpportunityNumber() > 0) {
            customerOverview.setAverageOpportunityAmount(customerOverview.getOpportunityAmount()
                    .divide(new BigDecimal(Long.toString(customerOverview.getOpportunityNumber())), RoundingMode.HALF_UP));
        }

        if (customerOverview.getDealsNumber() > 0) {
            customerOverview.setAverageDealsAmount(customerOverview.getDealsAmount()
                    .divide(new BigDecimal(Long.toString(customerOverview.getDealsNumber())), RoundingMode.HALF_UP));
        }

        this.updateCustomer(customer);

        return customer;
    }

    private SalesFunnelStatus findSalesFunnelStatusInLeadGenMethod(LeadGenMethod leadGenMethod, String salesFunnelStatusId) {

        SalesFunnelStatus[] salesFunnelStatus = new SalesFunnelStatus[1];


        leadGenMethod.getLeadGenSalesFunnel().getSalesFunnelStatuses().stream().filter(x -> x.getId().equals(salesFunnelStatusId)).findFirst()
                .ifPresent(x -> {
                    salesFunnelStatus[0] = x;
                });

        if (salesFunnelStatus[0] != null) return salesFunnelStatus[0];

        leadGenMethod.getLeadConversionSalesFunnel().getSalesFunnelStatuses().stream().filter(x -> x.getId().equals(salesFunnelStatusId)).findFirst()
                .ifPresent(x -> {
                    salesFunnelStatus[0] = x;
                });

        if (salesFunnelStatus[0] != null) return salesFunnelStatus[0];

        leadGenMethod.getAccountManagementSalesFunnel().getSalesFunnelStatuses().stream().filter(x -> x.getId().equals(salesFunnelStatusId)).findFirst()
                .ifPresent(x -> {
                    salesFunnelStatus[0] = x;
                });

        if (salesFunnelStatus[0] != null) return salesFunnelStatus[0];

        return null;
    }

    @BiqaAddObject
    @BiqaAuditObject
    public Customer addCustomer(Customer customer) {

        // if user not set is he
        // customer or lead - set customer by default
        if (!customer.isLead() && !customer.isCustomer()) {
            customer.setCustomer(true);
        }
        // new customer or lead is active when created
        customer.setActive(true);

        // if user not set lead gen method -
        // use default
        if (customer.getLeadGenMethod() == null || customer.getLeadGenMethod().equals("")) {
            LeadGenMethod leadGenMethod = this.leadRepository.findLeadGenMethodById(this.microserviceDomainSettings.findDomainSetting().getDefaultLeadGenMethodID());
            LeadGenProject leadGenMethod2 = this.leadRepository.findLeadGenProjectById(this.microserviceDomainSettings.findDomainSetting().getDefaultLeadGenProjectID());
            customer.setLeadGenMethod(leadGenMethod.getId());
            customer.setLeadGenProject(leadGenMethod2.getId());
        }

        LeadGenMethod method = this.leadRepository.findLeadGenMethodById(customer.getLeadGenMethod());

        // if we do not choose salesFunnelStatus for customer/lead
        if (customer.getSalesFunnelStatus() == null || customer.getSalesFunnelStatus().getId() == null || customer.getSalesFunnelStatus().getId().length() == 0) {

            if (customer.isCustomer()) {
                if (method.getAccountManagementSalesFunnel().getSalesFunnelStatuses().size() > 0) {
                    customer.setSalesFunnelStatus(AbstractSalesFunnelStatus.transformSalesFunnelToWithoutDataSource(
                            method.getAccountManagementSalesFunnel().getSalesFunnelStatuses().get(0))
                    );
                }
            } else {
                // this is lead
                if (method.getAccountManagementSalesFunnel().getSalesFunnelStatuses().size() > 0) {
                    customer.setSalesFunnelStatus(AbstractSalesFunnelStatus.transformSalesFunnelToWithoutDataSource(
                            method.getLeadConversionSalesFunnel().getSalesFunnelStatuses().get(0))
                    );
                }
            }
        } else {
            // if we choose salesFunnelStatus for customer/lead -
            // update salesFunnelStatus.description and salesFunnelStatus.color to the latest
            SalesFunnelStatus s = findSalesFunnelStatusInLeadGenMethod(method, customer.getSalesFunnelStatus().getId());
            if (s != null) {
                customer.getSalesFunnelStatus().setColor(s.getColor());
                customer.getSalesFunnelStatus().setDescription(s.getDescription());
            }
        }

        // if not set responsible manager -
        // set current user
        if (customer.getResponsibleManagerID() == null) {
            customer.setResponsibleManagerID(this.currentUser.getCurrentUser().getId());
        }

        this.ops.insert(customer);

        return customer;
    }

    // used in excel
    public List<Customer> addListCustomers(List<Customer> customers) {
        for (Customer customer : customers) {
            if (this.isCustomerExistById(customer.getId())) {
                this.updateCustomer(customer);
            } else {
                this.addCustomer(customer);
            }
        }
        return customers;
    }

    public Customer findCustomerOrLeadByID(String id) {
        return this.ops.findOne(Query.query(Criteria.where("id").is(id)), Customer.class);
    }

    public Customer addDocuemntFileTOcustomerById(String id, StorageFile documentFile) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().push("connectedInfo.connectedFiles", documentFile.getId());
        return this.ops.findAndModify(query, update, Customer.class);
    }

    // only internal system use
    public Customer updateCustomer(Customer customer) {
        ops.save(customer);
        return customer;
    }

    // use for public API controller
    // and other user action
    @BiqaAuditObject
    @BiqaCheckSecuredModifyObject
    public Customer updateCustomerForController(Customer customer) {
        return biqaObjectFilterService.safeUpdate(customer, ops);
    }

    public boolean isCustomerExistById(String id) {
        Criteria criteria = Criteria.where("id").is(id);
        Query query = new Query(criteria);
        return this.ops.exists(query, Customer.class);
    }

    public List<Customer> findAllCustomers() {
        CustomerFilter customerBuilder = new CustomerFilter();
        customerBuilder.setCustomer(true);

        List list = customerFilterRequestContextService.getCustomersByFilter(customerBuilder).getResultedObjects();
        return (List<Customer>) list;
    }

    /**
     * DON'T ADD {@link BiqaAddObject} Annotation!!!
     * THIS METHOD USED FOR PUBLIC WEB ANALYTICS AND Current user is not com.biqasoft.auth and domain is null... !!!
     *
     * @param lead
     * @param leadRepository
     */
    public void addWebSdkLead(Customer lead, LeadGenRepository leadRepository, String domain) {
        lead.setLead(true);
        lead.setActive(true);
        lead.setId(new ObjectId().toString());

        if (lead.getLeadGenMethod() == null || lead.getLeadGenMethod().equals("")) {
            LeadGenMethod leadGenMethod = leadRepository.findLeadGenMethodById(microserviceDomainSettings.unsafeFindDomainSettingsById(domain).getDefaultLeadGenMethodID());
            lead.setLeadGenMethod(leadGenMethod.getId());
        }

        LeadGenMethod method = leadRepository.findLeadGenMethodByIdWithoutDomainCheckingSecurity(lead.getLeadGenMethod(), domain);
        if (method.getLeadConversionSalesFunnel().getSalesFunnelStatuses().size() > 0)
            lead.setSalesFunnelStatus(AbstractSalesFunnelStatus.transformSalesFunnelToWithoutDataSource(
                    method.getLeadConversionSalesFunnel().getSalesFunnelStatuses().get(0))
            );

        DomainSettings domainSettings = microserviceDomainSettings.unsafeFindDomainSettingsById(domain);
        lead.setCustomFields(domainSettings.getCustomFieldForClass(biqaClassService.getName(lead)));

        if (currentUser.getCurrentUser() != null) {
            if (lead.getResponsibleManagerID() == null) {
                lead.setResponsibleManagerID(currentUser.getCurrentUser().getId());
            }
        }

        mongoTenantHelper.domainDataBaseUnsafeGet(domain).insert(lead);
    }

}
