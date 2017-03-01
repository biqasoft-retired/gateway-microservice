/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.customer.repositories;

import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.customer.Customer;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.dto.export.excel.ExportCustomersDTO;
import com.biqasoft.entity.filters.CustomerFilter;
import com.biqasoft.gateway.export.MicroserviceExport;
import com.biqasoft.gateway.system.parsing.ParsingFilesServices;
import com.biqasoft.microservice.communicator.servicediscovery.MicroserviceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerExcelRepository {

    private final CustomerFilterRequestContextService customerFilterRequestContextService;
    private final ParsingFilesServices parsingFilesServices;
    private final SalesFunnelRepository salesFunnelRepository;
    private final CustomerRepository customerRepository;
    private final com.biqasoft.microservice.communicator.servicediscovery.MicroserviceHelper microserviceHelper;
    private final MicroserviceExport microserviceExport;
    private final CurrentUser currentUser;

    private static final Logger logger = LoggerFactory.getLogger(CustomerExcelRepository.class);

    @Autowired
    public CustomerExcelRepository(CustomerFilterRequestContextService customerFilterRequestContextService, ParsingFilesServices parsingFilesServices,
                                   SalesFunnelRepository salesFunnelRepository, CustomerRepository customerRepository, MicroserviceHelper microserviceHelper, MicroserviceExport microserviceExport, CurrentUser currentUser) {
        this.customerFilterRequestContextService = customerFilterRequestContextService;
        this.parsingFilesServices = parsingFilesServices;
        this.salesFunnelRepository = salesFunnelRepository;
        this.customerRepository = customerRepository;
        this.microserviceHelper = microserviceHelper;
        this.microserviceExport = microserviceExport;
        this.currentUser = currentUser;
    }


    public byte[] getCustomerInEXCEL(CustomerFilter customerBuilder) {
        BiqaPaginationResultList<Customer> customers = customerFilterRequestContextService.getCustomersByFilter(customerBuilder);

        ExportCustomersDTO exportCustomersDTO = new ExportCustomersDTO();
        exportCustomersDTO.setCustomerFilter(customerBuilder);
        exportCustomersDTO.setEntityNumber(customers.getEntityNumber());
        exportCustomersDTO.setResultedObjects(customers.getResultedObjects());
        exportCustomersDTO.setDateFormat(currentUser.getDateFormat());

        return microserviceExport.getCustomersInExcel(exportCustomersDTO);
    }


//
//        public List<Customer> getParsedJsonFromUploadedXLSXForCustomers(MultipartFile[] fileSource) {
//        List<List<NameValueMap>> result = null;
//
//        for (MultipartFile fileInInputStream : fileSource) {
//            try {
//                result = parsingFilesServices.convertXLSXToNameValueMap(fileInInputStream.getInputStream());
//            } catch (Exception e) {
//                throw new RuntimeException(e.getMessage());
//            }
//        }
//
//        List<Customer> callList = new ArrayList<>();
//
//        for (List<NameValueMap> currentCall : result) {
//            Customer customer = new Customer();
////            List<NameValueMap> additionalFields = new ArrayList<>();
//
//            for (NameValueMap nameValueMap : currentCall) {
//
//                // if we have empty field(cell) in excel - ignore it
//                if (nameValueMap.getValue() == null || nameValueMap.getValue().length() < 1) continue;
//
//                switch (nameValueMap.getName()) {
//
//                    // if we update customer(not create new),
//                    // in excel customer ID should be
//                    // the first column should be `ID`
//                    case CUSTOMER_FIELDS.ID:
//                        customer.setId(nameValueMap.getValue());
//                        // don't override all customer -
//                        // just get customer
//                        // and then update customer
//                        customer = customerRepository.findCustomerOrLeadByID(nameValueMap.getValue());
//                        break;
//
//                    case CUSTOMER_FIELDS.FIRST_NAME:
//                        customer.setFirstName(nameValueMap.getValue());
//                        break;
//
//                    case CUSTOMER_FIELDS.LAST_NAME:
//                        customer.setLastName(nameValueMap.getValue());
//                        break;
//
//                    case CUSTOMER_FIELDS.PATRONYMIC:
//                        customer.setPatronymic(nameValueMap.getValue());
//                        break;
//
//                    case CUSTOMER_FIELDS.EMAIL:
//                        customer.setEmail(nameValueMap.getValue());
//                        break;
//
//                    case CUSTOMER_FIELDS.TELEPHONE:
//                        customer.setTelephone(nameValueMap.getValue());
//                        break;
//
//                    case CUSTOMER_FIELDS.LEAD:
//                        customer.setLead(nameValueMap.getValue().equals(SYSTEM_FIELDS_CONST.TRUE));
//                        break;
//
//                    case CUSTOMER_FIELDS.CUSTOMER:
//                        customer.setCustomer(nameValueMap.getValue().equals(SYSTEM_FIELDS_CONST.TRUE));
//                        break;
//
//                    case CUSTOMER_FIELDS.ACTIVE:
//                        customer.setActive(nameValueMap.getValue().equals(SYSTEM_FIELDS_CONST.TRUE));
//                        break;
//
//                    case CUSTOMER_FIELDS.IMPORTANT:
//                        customer.setImportant(nameValueMap.getValue().equals(SYSTEM_FIELDS_CONST.TRUE));
//                        break;
//
//                    case CUSTOMER_FIELDS.ADDRESS:
//                        customer.setAddress(nameValueMap.getValue());
//                        break;
//
//                    case CUSTOMER_FIELDS.POSITION:
//                        customer.setPosition(nameValueMap.getValue());
//                        break;
//
//                    case CUSTOMER_FIELDS.COMPANY_ID:
//                        //TODO: order is important
//                        // we should know that this
//                        // is b2b
//                        if (customer.isB2b() && nameValueMap.getValue() != null && !nameValueMap.getValue().equals("")) {
//                            Company company = new Company();
//                            company.setId(nameValueMap.getValue());
//                            customer.setCompany(company);
//                        }
//                        break;
//
//                    case CUSTOMER_FIELDS.LEAD_GEN_METHOD:
//                        customer.setLeadGenMethod(nameValueMap.getValue());
//                        break;
//
//                    case CUSTOMER_FIELDS.LEAD_GEN_PROJECT:
//                        customer.setLeadGenProject(nameValueMap.getValue());
//                        break;
//
//                    case CUSTOMER_FIELDS.B2B:
//                        customer.setB2b(nameValueMap.getValue().equals(SYSTEM_FIELDS_CONST.TRUE));
//                        break;
//
//                    case CUSTOMER_FIELDS.SEX:
//                        customer.setSex(nameValueMap.getValue());
//                        break;
//
//                    case CUSTOMER_FIELDS.NOTE:
//                        customer.setDescription(nameValueMap.getValue());
//                        break;
//
//                    case CUSTOMER_FIELDS.RESPONSIBLE_MANAGER_ID:
//                        if (nameValueMap.getValue() != null && !"".equals(nameValueMap.getValue()) && !"NO".equals(nameValueMap.getValue()))
//                            customer.setResponsibleManagerID(nameValueMap.getValue());
//                        break;
//
//                    case CUSTOMER_FIELDS.SALES_FUNNEL_STATUS_ID:
//                        if (nameValueMap.getValue() != null && !"".equals(nameValueMap.getValue()) && !"NO".equals(nameValueMap.getValue())) {
//                            customer.setSalesFunnelStatus(salesFunnelRepository.findSalesFunnelByStatusId(nameValueMap.getValue()));
//                        }
//                        break;
//
//                    default:
////                        NameValueMap nameValueMap1 = new NameValueMap();
////                        nameValueMap1.setName(nameValueMap.getName());
////                        nameValueMap1.setValue(nameValueMap.getValue());
////                        additionalFields.add(nameValueMap1);
//                        break;
//                }
//            }
////            customer.setGlobalCustomerAdditionalFields(additionalFields);
//            callList.add(customer);
//        }
//
//        return callList;
//
//    }


}
