/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.cloud.democonfiguration;

import com.biqasoft.audit.object.BiqaClassService;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.constants.*;
import com.biqasoft.entity.customer.*;
import com.biqasoft.entity.datasources.DataSource;
import com.biqasoft.entity.filters.CustomerFilter;
import com.biqasoft.entity.filters.TaskFilter;
import com.biqasoft.entity.core.objects.CustomField;
import com.biqasoft.entity.core.objects.field.CustomDictionary;
import com.biqasoft.entity.core.objects.field.CustomDictionaryItem;
import com.biqasoft.entity.core.objects.field.DataSourcesTypes;
import com.biqasoft.entity.payments.CompanyCost;
import com.biqasoft.entity.payments.CustomerDeal;
import com.biqasoft.entity.salesfunnel.SalesFunnel;
import com.biqasoft.entity.salesfunnel.SalesFunnelStatus;
import com.biqasoft.entity.core.DomainSettings;
import com.biqasoft.entity.system.NameValueMap;
import com.biqasoft.entity.tasks.Task;
import com.biqasoft.entity.tasks.TaskProject;
import com.biqasoft.entity.tasks.TaskTemplate;
import com.biqasoft.users.domain.useraccount.UserAccount;
import com.biqasoft.users.domain.useraccount.UserAccountPredicate;
import com.biqasoft.entity.widgets.Widget;
import com.biqasoft.entity.widgets.WidgetsDashboard;
import com.biqasoft.gateway.customer.repositories.CustomerRepository;
import com.biqasoft.gateway.customer.repositories.OpportunityRepository;
import com.biqasoft.gateway.customer.repositories.SalesFunnelRepository;
import com.biqasoft.gateway.customer.repositories.SegmentsRepository;
import com.biqasoft.gateway.datasources.repositories.DataSourceRepository;
import com.biqasoft.gateway.email.services.EmailPrepareAndSendService;
import com.biqasoft.gateway.leadgen.repositories.LeadGenRepository;
import com.biqasoft.gateway.payments.repositories.PaymentsRepository;
import com.biqasoft.gateway.tasks.repositories.TaskProjectRepository;
import com.biqasoft.gateway.tasks.repositories.TaskRepository;
import com.biqasoft.gateway.tasks.repositories.TaskTemplateRepository;
import com.biqasoft.gateway.widgets.WidgetRepository;
import com.biqasoft.microservice.common.MicroserviceDomainSettings;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 4/6/2015
 *         All Rights Reserved
 */

@Service
public class CreateTestData {

    private final TaskRepository taskRepository;
    private final TaskProjectRepository taskProjectRepository;
    private final TaskTemplateRepository taskTemplateRepository;
    private final CurrentUser currentUser;
    private final LeadGenRepository leadRepository;
    private final SalesFunnelRepository salesFunnelRepository;
    private final CustomerRepository customerRepository;
    private final PaymentsRepository paymentsRepository;
    private final OpportunityRepository opportunityRepository;
    private final EmailPrepareAndSendService emailPrepareAndSendService;
    private final WidgetRepository widgetRepository;
    private final DataSourceRepository dataSourceAllData;
    private final SegmentsRepository segmentsRepository;
    private final CreateTestDataEsid createTestDataEsid;
    private final CreateTestDataStorage createTestDataStorage;
    private final CreateTestUserAccountsGroups createTestUserAccountsGroups;
    private final MicroserviceDomainSettings microserviceDomainSettings;
    private final BiqaClassService biqaClassService;

    @Autowired
    public CreateTestData(LeadGenRepository leadRepository, OpportunityRepository opportunityRepository, EmailPrepareAndSendService emailPrepareAndSendService,
                          TaskRepository taskRepository, PaymentsRepository paymentsRepository, CreateTestDataStorage createTestDataStorage,
                          CreateTestUserAccountsGroups createTestUserAccountsGroups, SalesFunnelRepository salesFunnelRepository, WidgetRepository widgetRepository,
                          SegmentsRepository segmentsRepository, CreateTestDataEsid createTestDataEsid, CustomerRepository customerRepository,
                          TaskProjectRepository taskProjectRepository, DataSourceRepository dataSourceAllData, TaskTemplateRepository taskTemplateRepository,
                          CurrentUser currentUser, MicroserviceDomainSettings microserviceDomainSettings, BiqaClassService biqaClassService) {
        this.leadRepository = leadRepository;
        this.opportunityRepository = opportunityRepository;
        this.emailPrepareAndSendService = emailPrepareAndSendService;
        this.taskRepository = taskRepository;
        this.paymentsRepository = paymentsRepository;
        this.createTestDataStorage = createTestDataStorage;
        this.createTestUserAccountsGroups = createTestUserAccountsGroups;
        this.salesFunnelRepository = salesFunnelRepository;
        this.widgetRepository = widgetRepository;
        this.segmentsRepository = segmentsRepository;
        this.createTestDataEsid = createTestDataEsid;
        this.customerRepository = customerRepository;
        this.taskProjectRepository = taskProjectRepository;
        this.dataSourceAllData = dataSourceAllData;
        this.taskTemplateRepository = taskTemplateRepository;
        this.currentUser = currentUser;
        this.microserviceDomainSettings = microserviceDomainSettings;
        this.biqaClassService = biqaClassService;
    }

    private List<CustomField> createCustomFieldsForLeadGenMethod() {
        List<CustomField> customFields = new ArrayList<>();

        CustomField mediaTypeName = new CustomField("Медиа", CUSTOM_FIELD_TYPES.STRING);
        CustomField mediaFormatName = new CustomField("Медиа формат", CUSTOM_FIELD_TYPES.STRING);
        CustomField mediaLocationName = new CustomField("Медиа место", CUSTOM_FIELD_TYPES.STRING);


        customFields.add(mediaTypeName);
        customFields.add(mediaFormatName);
        customFields.add(mediaLocationName);

        return customFields;
    }

    public void create(UserAccount user, String domainName, int timeZoneOffset) throws Exception {

        /////// START: DOMAIN SETTINGS   ///////
        DomainSettings domainSettings = new DomainSettings();
        domainSettings.setId(domainName);
        domainSettings.setDefaultEmail(user.getEmail());
        domainSettings.setTimeZoneOffset(timeZoneOffset);
        domainSettings.setCurrency("RUB");

        domainSettings.setCustomFieldForClass(biqaClassService.getName(LeadGenMethod.class), createCustomFieldsForLeadGenMethod());

        microserviceDomainSettings.create(domainSettings);
        /////// END: DOMAIN SETTINGS   ///////

        //////////////// START: LEAD GEN SALES FUNNEL  ////////////////
        SalesFunnel leadGenSalesFunnel = new SalesFunnel();
        List<SalesFunnelStatus> leadGenSalesFunnelStatuses = new ArrayList<>();
        leadGenSalesFunnel.setSystemIssued(true);

        SalesFunnelStatus leadGenSalesFunnelStatus1 = new SalesFunnelStatus();
        leadGenSalesFunnelStatus1.setName("Посетители b2c");
        leadGenSalesFunnelStatus1.setColor("#303F9F");

        {
            DataSource pos = new DataSource();
            pos.setReturnType(DATA_SOURCES_RETURNED_TYPES.INTEGER);
            pos.setControlledClass(DATA_SOURCES.INSIDE_VALUE);
            pos.setResolved(true);
            pos.setValues(new DataSourcesTypes());
            pos.getValues().setIntVal(139);

            pos.setName("Тестовые данные: " + leadGenSalesFunnelStatus1.getName());

            NameValueMap map1 = new NameValueMap("value", "139");

            List<NameValueMap> list1 = new ArrayList<>();
            list1.add(map1);
            pos.setParameters(list1);

            dataSourceAllData.addNewDataSourceSavedData(pos);

            leadGenSalesFunnelStatus1.setDataSource(pos);
            leadGenSalesFunnelStatuses.add(leadGenSalesFunnelStatus1);
        }

        {
            SalesFunnelStatus leadGenSalesFunnelStatus2 = new SalesFunnelStatus();
            leadGenSalesFunnelStatus2.setName("Заинтересовались");
            leadGenSalesFunnelStatus2.setColor("#283593");

            DataSource pos2 = new DataSource();
            pos2.setReturnType(DATA_SOURCES_RETURNED_TYPES.INTEGER);
            pos2.setControlledClass(DATA_SOURCES.INSIDE_VALUE);
            pos2.setResolved(true);
            pos2.setValues(new DataSourcesTypes());
            pos2.getValues().setIntVal(74);
            pos2.setName("Тестовые данные: " + leadGenSalesFunnelStatus2.getName());

            NameValueMap map12 = new NameValueMap("value", "74");

            List<NameValueMap> list12 = new ArrayList<>();
            list12.add(map12);
            pos2.setParameters(list12);

            dataSourceAllData.addNewDataSourceSavedData(pos2);
            leadGenSalesFunnelStatus2.setDataSource(pos2);
            leadGenSalesFunnelStatuses.add(leadGenSalesFunnelStatus2);
        }

        {
            SalesFunnelStatus leadGenSalesFunnelStatus3 = new SalesFunnelStatus();
            leadGenSalesFunnelStatus3.setName("Оставили свои данные b2c");
            leadGenSalesFunnelStatus3.setColor("#1A237E");

            DataSource pos23 = new DataSource();
            pos23.setReturnType(DATA_SOURCES_RETURNED_TYPES.INTEGER);
            pos23.setControlledClass(DATA_SOURCES.INSIDE_VALUE);
            pos23.setResolved(true);
            pos23.setValues(new DataSourcesTypes());
            pos23.getValues().setIntVal(7);
            pos23.setName("Тестовые данные b2c: " + leadGenSalesFunnelStatus3.getName());

            NameValueMap map123 = new NameValueMap("value", "7");

            List<NameValueMap> list123 = new ArrayList<>();
            list123.add(map123);
            pos23.setParameters(list123);

            dataSourceAllData.addNewDataSourceSavedData(pos23);
            leadGenSalesFunnelStatus3.setDataSource(pos23);

            leadGenSalesFunnelStatuses.add(leadGenSalesFunnelStatus3);

            leadGenSalesFunnel.setName("Воронка лидов в B2C");
            leadGenSalesFunnel.setPhase(SALES_FUNNEL.LEAD_GEN_SALES_FUNNEL);
            leadGenSalesFunnel.setSalesFunnelStatuses(leadGenSalesFunnelStatuses);

            salesFunnelRepository.addSalesFunnel(leadGenSalesFunnel);
        }
//////////////// END: LEAD GEN SALES FUNNEL  ////////////////

////////////////////////// LC //////////////////////////////////////////////////
        SalesFunnel leadConversionSalesFunnel = new SalesFunnel();
        List<SalesFunnelStatus> leadConversionSalesFunnelStatuses = new ArrayList<>();
        leadConversionSalesFunnel.setSystemIssued(true);

        SalesFunnelStatus leadConversionSalesFunnelStatus0 = new SalesFunnelStatus();
        leadConversionSalesFunnelStatus0.setName("Новый");
        leadConversionSalesFunnelStatus0.setColor("#90CAF9");
        leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus0);

        SalesFunnelStatus leadConversionSalesFunnelStatus1 = new SalesFunnelStatus();
        leadConversionSalesFunnelStatus1.setName("Получены возможности");
        leadConversionSalesFunnelStatus1.setDescription("Лиду добавлены возможности");
        leadConversionSalesFunnelStatus1.setColor("#64B5F6");
        leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus1);

        SalesFunnelStatus leadConversionSalesFunnelStatus1t = new SalesFunnelStatus();
        leadConversionSalesFunnelStatus1t.setName("Ожидание лида");
        leadConversionSalesFunnelStatus1t.setColor("#42A5F5");
        leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus1t);

        SalesFunnelStatus leadConversionSalesFunnelStatus2 = new SalesFunnelStatus();
        leadConversionSalesFunnelStatus2.setName("Получили частичную оплату");
        leadConversionSalesFunnelStatus2.setColor("#2196F3");
        leadConversionSalesFunnelStatus2.setDescription("Оплачено частично");
        leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus2);

        SalesFunnelStatus leadConversionSalesFunnelStatus3 = new SalesFunnelStatus();
        leadConversionSalesFunnelStatus3.setName("Условная оплата");
        leadConversionSalesFunnelStatus3.setColor("#1E88E5");
        leadConversionSalesFunnelStatus3.setDescription("Лид сказал, что оплачено, но еще не подвержено в бухгалтерии");
        leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus3);

        SalesFunnelStatus leadConversionSalesFunnelStatus3tt = new SalesFunnelStatus();
        leadConversionSalesFunnelStatus3tt.setName("Оплата подтвержена");
        leadConversionSalesFunnelStatus3tt.setColor("#1976D2");
        leadConversionSalesFunnelStatus3tt.setDescription("Бухгалтерия подтвердила факт оплаты");
        leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus3tt);

        SalesFunnelStatus leadConversionSalesFunnelStatus3ttt = new SalesFunnelStatus();
        leadConversionSalesFunnelStatus3ttt.setName("Условный отказ");
        leadConversionSalesFunnelStatus3ttt.setColor("#1565C0");
        leadConversionSalesFunnelStatus3ttt.setDescription("Отказ покупки лидом. Ожидание подтверждения невозможности дожима отделом качества");
        leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus3ttt);

        SalesFunnelStatus leadConversionSalesFunnelStatus3tttt = new SalesFunnelStatus();
        leadConversionSalesFunnelStatus3tttt.setName("Отказ");
        leadConversionSalesFunnelStatus3tttt.setColor("#0D47A1");
        leadConversionSalesFunnelStatus3tttt.setDescription("Отказ покупки лидом. Подтвержденено невозможности дожима отделом качества");
        leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus3tttt);


        leadConversionSalesFunnel.setName("Воронка конверсии лидов в B2C");
        leadConversionSalesFunnel.setPhase(SALES_FUNNEL.LEAD_CONVERCIAL_SALES_FUNNEL);
        leadConversionSalesFunnel.setSalesFunnelStatuses(leadConversionSalesFunnelStatuses);
        salesFunnelRepository.addSalesFunnel(leadConversionSalesFunnel);
/////////////////////////////////////////////////////////////////////////////////////

////////////////////////// AM ////////////////////////
        SalesFunnel leadAccountManagementSalesFunnel = new SalesFunnel();
        List<SalesFunnelStatus> leadAccountManagementSalesFunnelStatuses = new ArrayList<>();
        leadAccountManagementSalesFunnel.setSystemIssued(true);

        SalesFunnelStatus leadAccountManagementSalesFunnelStatus1 = new SalesFunnelStatus();
        leadAccountManagementSalesFunnelStatus1.setName("Первая покупка");
        leadAccountManagementSalesFunnelStatus1.setColor("#D32F2F");
        leadAccountManagementSalesFunnelStatuses.add(leadAccountManagementSalesFunnelStatus1);

        SalesFunnelStatus leadAccountManagementSalesFunnelStatus2 = new SalesFunnelStatus();
        leadAccountManagementSalesFunnelStatus2.setName("Получили еще возможность");
        leadAccountManagementSalesFunnelStatus2.setColor("#C62828");
        leadAccountManagementSalesFunnelStatuses.add(leadAccountManagementSalesFunnelStatus2);

        SalesFunnelStatus leadAccountManagementSalesFunnelStatus3 = new SalesFunnelStatus();
        leadAccountManagementSalesFunnelStatus3.setName("Еще оплата");
        leadAccountManagementSalesFunnelStatus3.setColor("#B71C1C");
        leadAccountManagementSalesFunnelStatuses.add(leadAccountManagementSalesFunnelStatus3);

        leadAccountManagementSalesFunnel.setName("Воронка поддержки клиентов в B2C");
        leadAccountManagementSalesFunnel.setPhase(SALES_FUNNEL.ACCOUNT_MANAGEMENT_SALES_FUNNEL);
        leadAccountManagementSalesFunnel.setSalesFunnelStatuses(leadAccountManagementSalesFunnelStatuses);
        salesFunnelRepository.addSalesFunnel(leadAccountManagementSalesFunnel);


        /////// START: LEAD GEN METHOD   ///////
        LeadGenMethod leadGenMethod = new LeadGenMethod();
        leadGenMethod.setLeadGenSalesFunnel(leadGenSalesFunnel);
        leadGenMethod.setLeadConversionSalesFunnel(leadConversionSalesFunnel);
        leadGenMethod.setAccountManagementSalesFunnel(leadAccountManagementSalesFunnel);

        leadGenMethod.setName("Процесс продаж B2C по умолчанию");
        leadRepository.addLeadGenMethod(leadGenMethod);

        LeadGenProject leadGenProject = new LeadGenProject();
        leadGenProject.setName("Проект по умолчанию");
        leadGenProject.setLeadGenMethodId(leadGenMethod.getId());

        List<String> promoCodes = new ArrayList<>();
        promoCodes.add("ER600");
        leadGenProject.setPromoCodes(promoCodes);
        leadRepository.addLeadGenProject(leadGenProject);

        leadGenMethod.getLeadGenProjects().add(leadGenProject);
        leadGenMethod = leadRepository.updateLeadGenMethod(leadGenMethod);
        /////// END: LEAD GEN METHOD   ///////


        /////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////
        domainSettings.setDefaultIDSalesFunnelLG(leadGenSalesFunnel.getId());
        domainSettings.setDefaultIDSalesFunnelLC(leadConversionSalesFunnel.getId());
        domainSettings.setDefaultIDSalesFunnelAM(leadAccountManagementSalesFunnel.getId());
        domainSettings.setDefaultLeadGenMethodID(leadGenMethod.getId());
        domainSettings.setDefaultLeadGenProjectID(leadGenProject.getId());

        microserviceDomainSettings.updateDomainSettings(domainSettings);

        createCustomFields();

        createDefaultSegments();
        createTestCustomer();
        createDefaultTaskTemplates();
        biznesMolodostDefault();
        createTestDataEsid.createEsid();
        createLeadGenMethods();
        createTestDashboard();
        createCustomFieldsForOpportunity();
        createTaskProjects();
        createTestDataStorage.createFolders();
        createTestUserAccountsGroups.createUserAccountsGroups();
    }

    public void createTestDashboard() {

        WidgetsDashboard dashboardWidgets = widgetRepository.addNewDashboardWidgets(new WidgetsDashboard());
        dashboardWidgets.setName("Главная панель управления, KPI");
        dashboardWidgets = widgetRepository.updateOneDashboard(dashboardWidgets);

        WidgetsDashboard summaryDashboard = widgetRepository.addNewDashboardWidgets(new WidgetsDashboard());
        summaryDashboard.setName("Свобдная таблица KPI");
        summaryDashboard = widgetRepository.updateOneDashboard(summaryDashboard);

//////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////
        Widget widget = new Widget();
        widget.setName("Уникальных посетителей сегодня");
        widget.setTemplateController(WIDGET_CONTROLLERS.DATA_SOURCE_ONE_DATA);
        widget.setLocation(WIDGET_LOCATION.DASHBOARD);
        widget.setDemo(true);
        widget.setSizeY(9);
        widget.setSizeX(7);

        DataSource dataSource1 = new DataSource();
        dataSource1.setName("Уникальных посетителей сегодня");
        dataSource1.setSystemIssued(true);
        dataSource1.setResolved(true);
        dataSource1.setValues(new DataSourcesTypes());
        dataSource1.getValues().setIntVal(1379);
        dataSource1.setControlledClass(DATA_SOURCES.INSIDE_VALUE);
        dataSource1.setReturnType(DATA_SOURCES_RETURNED_TYPES.INTEGER);

        List<NameValueMap> paramsForWidget = new ArrayList<>();
        NameValueMap nameValueMap = new NameValueMap();
        nameValueMap.setName("value");
        nameValueMap.setValue("1379");

        paramsForWidget.add(nameValueMap);
        dataSource1.setParameters(paramsForWidget);

        List<DataSource> datas111Source = new ArrayList<>();
        datas111Source.add(dataSource1);
        widget.setDataSources(datas111Source);


        List<NameValueMap> realParamsForWidget = new ArrayList<>();
        NameValueMap styleParamForWidget = new NameValueMap();
        styleParamForWidget.setName("style");
        styleParamForWidget.setValue("1");
        realParamsForWidget.add(styleParamForWidget);


        widget.setAdditionalData(realParamsForWidget);

        dataSourceAllData.addNewDataSourceSavedData(dataSource1);
        widgetRepository.addWidget(widget);
//////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////
        Widget allTasksInSystemWidget = new Widget();
        allTasksInSystemWidget.setName("Число задач в системе");
        allTasksInSystemWidget.setSizeY(9);
        allTasksInSystemWidget.setSizeX(7);

        DataSource dataSource = new DataSource();
        dataSource.setName("Число задач в системе");
        dataSource.setSystemIssued(true);
        dataSource.setResolved(true);
        dataSource.setControlledClass(DATA_SOURCES.ALL_TASKS_NUMBERS);

        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(dataSource);

        allTasksInSystemWidget.setDataSources(dataSources);
        allTasksInSystemWidget.setLocation(WIDGET_LOCATION.DASHBOARD);
        allTasksInSystemWidget.setTemplateController(WIDGET_CONTROLLERS.DATA_SOURCE_ONE_DATA);
        allTasksInSystemWidget.setDemo(false);

        List<NameValueMap> realParamsForWidget2 = new ArrayList<>();
        NameValueMap realParamsForWidget22 = new NameValueMap();
        realParamsForWidget22.setName("style");
        realParamsForWidget22.setValue("1");
        realParamsForWidget2.add(realParamsForWidget22);
        allTasksInSystemWidget.setAdditionalData(realParamsForWidget2);

        widgetRepository.addWidget(allTasksInSystemWidget);

//////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////

        summaryDashboard.getWidgets().add(allTasksInSystemWidget);
        summaryDashboard = widgetRepository.updateOneDashboard(summaryDashboard);

        dashboardWidgets.getWidgets().add(widget);
        dashboardWidgets = widgetRepository.updateOneDashboard(dashboardWidgets);
    }

    public void createCustomFields() {
        DomainSettings domainSettings = microserviceDomainSettings.findDomainSetting();

        List<CustomField> customFields = new ArrayList<>();
        List<CustomField> customFieldsForCompany = new ArrayList<>();

        {
            CustomField field = new CustomField();
            field.setType(CUSTOM_FIELD_TYPES.STRING);
            field.setName("Желаемый результат");
            customFields.add(field);

            CustomField field2 = new CustomField();
            field2.setType(CUSTOM_FIELD_TYPES.STRING);
            field2.setName("Цель");
            customFields.add(field2);

            CustomField field3 = new CustomField();
            field3.setType(CUSTOM_FIELD_TYPES.DOUBLE);
            field3.setName("Оценка в часах");
            field3.setDescription("Оценка времени, необходимого для выполнения задачи");
            customFields.add(field3);
        }

        {
            // company
            CustomField field4 = new CustomField();
            field4.setType(CUSTOM_FIELD_TYPES.DICTIONARY);
            field4.setName("Надежность");
            field4.setDescription("Риск контрагента, условия договора, сроки. Успешность работы в прошлом");

            DataSourcesTypes value4 = new DataSourcesTypes();
            CustomDictionary dictionary = new CustomDictionary();
            List<CustomDictionaryItem> items = new ArrayList<>();

            CustomDictionaryItem defaultVal = new CustomDictionaryItem("0", "0");

            items.add(defaultVal);
            items.add(new CustomDictionaryItem("1", "1"));
            items.add(new CustomDictionaryItem("2", "2"));
            items.add(new CustomDictionaryItem("3", "3"));
            items.add(new CustomDictionaryItem("4", "4"));
            items.add(new CustomDictionaryItem("5", "5"));
            items.add(new CustomDictionaryItem("6", "6"));

            dictionary.setValues(items);
            dictionary.setValue(defaultVal);

            value4.setDictVal(dictionary);

            field4.setValue(value4);

            customFieldsForCompany.add(field4);
        }


        /**
         * ОГРН	1027700132195
         * ИНН	7707083893
         * КПП	775003035
         * ОКПО	57972160
         * БИК	044525225
         */
        {
            CustomField field4 = new CustomField();
            field4.setType(CUSTOM_FIELD_TYPES.LONG);
            field4.setName("ОГРН");
            field4.setDescription("ОГРН");
            field4.setAlias("OGRN");
            customFieldsForCompany.add(field4);
        }

        {
            CustomField field4 = new CustomField();
            field4.setType(CUSTOM_FIELD_TYPES.LONG);
            field4.setName("ИНН");
            field4.setDescription("ИНН");
            field4.setAlias("INN");
            customFieldsForCompany.add(field4);
        }


        {
            CustomField field4 = new CustomField();
            field4.setType(CUSTOM_FIELD_TYPES.LONG);
            field4.setName("КПП");
            field4.setDescription("КПП");
            field4.setAlias("KPP");
            customFieldsForCompany.add(field4);
        }


        {
            CustomField field4 = new CustomField();
            field4.setType(CUSTOM_FIELD_TYPES.LONG);
            field4.setName("ОКПО");
            field4.setDescription("ОКПО");
            field4.setAlias("OKPO");
            customFieldsForCompany.add(field4);
        }

        {
            CustomField field4 = new CustomField();
            field4.setType(CUSTOM_FIELD_TYPES.LONG);
            field4.setName("БИК");
            field4.setDescription("БИК");
            field4.setAlias("BIK");
            customFieldsForCompany.add(field4);
        }

        domainSettings.setCustomFieldForClass(biqaClassService.getName(Task.class), customFields);
        domainSettings.setCustomFieldForClass(biqaClassService.getName(Company.class), customFieldsForCompany);

        microserviceDomainSettings.updateDomainSettings(domainSettings);
    }

    public void createCustomFieldsForOpportunity() {
        DomainSettings domainSettings = microserviceDomainSettings.findDomainSetting();

        List<CustomField> customFieldsForCompany = new ArrayList<>();

        CustomField field4 = new CustomField();
        field4.setType(CUSTOM_FIELD_TYPES.DICTIONARY);
        field4.setName("Интересность");
        field4.setDescription("Когда и сколько предположительно придут денег");

        DataSourcesTypes value4 = new DataSourcesTypes();
        CustomDictionary dictionary = new CustomDictionary();
        List<CustomDictionaryItem> items = new ArrayList<>();

        CustomDictionaryItem defaultVal = new CustomDictionaryItem("Не указано", "0");

        items.add(defaultVal);
        items.add(new CustomDictionaryItem("Быстро-много", "1"));
        items.add(new CustomDictionaryItem("Быстро-мало", "2"));
        items.add(new CustomDictionaryItem("Много-долго", "3"));
        items.add(new CustomDictionaryItem("Долго-мало", "4"));

        dictionary.setValues(items);
        dictionary.setValue(defaultVal);

        value4.setDictVal(dictionary);
        field4.setValue(value4);

        customFieldsForCompany.add(field4);

        domainSettings.setCustomFieldForClass(biqaClassService.getName(Task.class), customFieldsForCompany);

        microserviceDomainSettings.updateDomainSettings(domainSettings);
    }


    public void createLeadGenMethods() {

        //////////////// START: LEAD GEN SALES FUNNEL  ////////////////
        SalesFunnel leadGenSalesFunnel = new SalesFunnel();
        List<SalesFunnelStatus> leadGenSalesFunnelStatuses = new ArrayList<>();
        leadGenSalesFunnel.setSystemIssued(true);

        SalesFunnelStatus leadGenSalesFunnelStatus1 = new SalesFunnelStatus();
        leadGenSalesFunnelStatus1.setName("Посетители b2b");

        {
            DataSource pos = new DataSource();
            pos.setReturnType(DATA_SOURCES_RETURNED_TYPES.INTEGER);
            pos.setControlledClass(DATA_SOURCES.INSIDE_VALUE);
            pos.setResolved(true);
            pos.setValues(new DataSourcesTypes());
            pos.getValues().setIntVal(239);
            pos.setName("Тестовые данные: " + leadGenSalesFunnelStatus1.getName());

            NameValueMap map1 = new NameValueMap("value", "239");

            List<NameValueMap> list1 = new ArrayList<>();
            list1.add(map1);
            pos.setParameters(list1);

            dataSourceAllData.addNewDataSourceSavedData(pos);

            leadGenSalesFunnelStatus1.setDataSource(pos);
            leadGenSalesFunnelStatuses.add(leadGenSalesFunnelStatus1);
        }

        {
            SalesFunnelStatus leadGenSalesFunnelStatus2 = new SalesFunnelStatus();
            leadGenSalesFunnelStatus2.setName("Заинтересовались");

            DataSource pos2 = new DataSource();
            pos2.setReturnType(DATA_SOURCES_RETURNED_TYPES.INTEGER);
            pos2.setControlledClass(DATA_SOURCES.INSIDE_VALUE);
            pos2.setResolved(true);
            pos2.setValues(new DataSourcesTypes());
            pos2.getValues().setIntVal(34);
            pos2.setName("Тестовые данные: " + leadGenSalesFunnelStatus2.getName());

            NameValueMap map12 = new NameValueMap("value", "34");

            List<NameValueMap> list12 = new ArrayList<>();
            list12.add(map12);
            pos2.setParameters(list12);

            dataSourceAllData.addNewDataSourceSavedData(pos2);
            leadGenSalesFunnelStatus2.setDataSource(pos2);


            leadGenSalesFunnelStatuses.add(leadGenSalesFunnelStatus2);
        }

        {
            SalesFunnelStatus leadGenSalesFunnelStatus3 = new SalesFunnelStatus();
            leadGenSalesFunnelStatus3.setName("Оставили свои данные b2b");
            DataSource pos23 = new DataSource();
            pos23.setReturnType(DATA_SOURCES_RETURNED_TYPES.INTEGER);
            pos23.setControlledClass(DATA_SOURCES.INSIDE_VALUE);
            pos23.setResolved(true);
            pos23.setValues(new DataSourcesTypes());
            pos23.getValues().setIntVal(4);
            pos23.setName("Тестовые данные b2b: " + leadGenSalesFunnelStatus3.getName());

            NameValueMap map123 = new NameValueMap("value", "4");

            List<NameValueMap> list123 = new ArrayList<>();
            list123.add(map123);
            pos23.setParameters(list123);

            dataSourceAllData.addNewDataSourceSavedData(pos23);
            leadGenSalesFunnelStatus3.setDataSource(pos23);

            leadGenSalesFunnelStatuses.add(leadGenSalesFunnelStatus3);

            leadGenSalesFunnel.setName("Воронка лидов в B2B");
            leadGenSalesFunnel.setPhase(SALES_FUNNEL.LEAD_GEN_SALES_FUNNEL);
            leadGenSalesFunnel.setSalesFunnelStatuses(leadGenSalesFunnelStatuses);

            salesFunnelRepository.addSalesFunnel(leadGenSalesFunnel);
        }
//////////////// END: LEAD GEN SALES FUNNEL  ////////////////

        SalesFunnel leadConversionSalesFunnel = new SalesFunnel();
        {
            List<SalesFunnelStatus> leadConversionSalesFunnelStatuses = new ArrayList<>();
            leadConversionSalesFunnel.setSystemIssued(true);

            SalesFunnelStatus leadConversionSalesFunnelStatus0 = new SalesFunnelStatus();
            leadConversionSalesFunnelStatus0.setName("Новый");
            leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus0);

            SalesFunnelStatus leadConversionSalesFunnelStatus01 = new SalesFunnelStatus();
            leadConversionSalesFunnelStatus01.setName("Назначена встреча");
            leadConversionSalesFunnelStatus01.setDescription("Встреча или иной вариант согласования");
            leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus01);

            SalesFunnelStatus leadConversionSalesFunnelStatus1 = new SalesFunnelStatus();
            leadConversionSalesFunnelStatus1.setName("Получены возможности");
            leadConversionSalesFunnelStatus1.setDescription("Лиду добавлены возможности. Принято решение");
            leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus1);

            SalesFunnelStatus leadConversionSalesFunnelStatus1ttttt = new SalesFunnelStatus();
            leadConversionSalesFunnelStatus1ttttt.setName("Проект договора");
            leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus1ttttt);

            SalesFunnelStatus leadConversionSalesFunnelStatus1tttttt = new SalesFunnelStatus();
            leadConversionSalesFunnelStatus1tttttt.setName("Договор на согласовании");
            leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus1tttttt);

            SalesFunnelStatus leadConversionSalesFunnelStatus1ttttttttttt = new SalesFunnelStatus();
            leadConversionSalesFunnelStatus1ttttttttttt.setName("Договор подписан");
            leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus1ttttttttttt);

            SalesFunnelStatus leadConversionSalesFunnelStatus1t = new SalesFunnelStatus();
            leadConversionSalesFunnelStatus1t.setName("Ожидание лида");
            leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus1t);

            SalesFunnelStatus leadConversionSalesFunnelStatus2 = new SalesFunnelStatus();
            leadConversionSalesFunnelStatus2.setName("Продоплата");
            leadConversionSalesFunnelStatus2.setDescription("Оплачено частично");
            leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus2);

            SalesFunnelStatus leadConversionSalesFunnelStatus3tt = new SalesFunnelStatus();
            leadConversionSalesFunnelStatus3tt.setName("Оплачено");
            leadConversionSalesFunnelStatus3tt.setDescription("Бухгалтери я подтвердила факт оплаты");
            leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus3tt);

            SalesFunnelStatus leadConversionSalesFunnelStatus3tt444t = new SalesFunnelStatus();
            leadConversionSalesFunnelStatus3tt444t.setName("Исполняется");
            leadConversionSalesFunnelStatus3tt444t.setDescription("В процессе реализации");
            leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus3tt444t);

            SalesFunnelStatus leadConversionSalesFunnelStatus3tt445344t = new SalesFunnelStatus();
            leadConversionSalesFunnelStatus3tt445344t.setName("Всё выполено");
            leadConversionSalesFunnelStatus3tt445344t.setDescription("Всё выполнено");
            leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus3tt445344t);

            SalesFunnelStatus leadConversionSalesFunnelStatus3ttt = new SalesFunnelStatus();
            leadConversionSalesFunnelStatus3ttt.setName("Условный отказ");
            leadConversionSalesFunnelStatus3ttt.setDescription("Отказ покупки лидом. Ожидание подтверждения невозможности дожима отделом качества");
            leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus3ttt);

            SalesFunnelStatus leadConversionSalesFunnelStatus3tttt = new SalesFunnelStatus();
            leadConversionSalesFunnelStatus3tttt.setName("Отказ");
            leadConversionSalesFunnelStatus3tttt.setDescription("Отказ покупки лидом. Подтвержденено невозможности дожима отделом качества");
            leadConversionSalesFunnelStatuses.add(leadConversionSalesFunnelStatus3tttt);


            leadConversionSalesFunnel.setName("Воронка конверсии лидов в B2B");
            leadConversionSalesFunnel.setPhase(SALES_FUNNEL.LEAD_CONVERCIAL_SALES_FUNNEL);
            leadConversionSalesFunnel.setSalesFunnelStatuses(leadConversionSalesFunnelStatuses);
            salesFunnelRepository.addSalesFunnel(leadConversionSalesFunnel);
        }
/////////////////////////////////////////////////////////////////////////////////////

////////////////////////// AM ////////////////////////
        SalesFunnel leadAccountManagementSalesFunnel = new SalesFunnel();
        {
            List<SalesFunnelStatus> leadAccountManagementSalesFunnelStatuses = new ArrayList<>();
            leadAccountManagementSalesFunnel.setSystemIssued(true);

            SalesFunnelStatus leadAccountManagementSalesFunnelStatus1 = new SalesFunnelStatus();
            leadAccountManagementSalesFunnelStatus1.setName("Первая покупка");
//            leadAccountManagementSalesFunnelStatus1.setColor("#FF4081");
            leadAccountManagementSalesFunnelStatuses.add(leadAccountManagementSalesFunnelStatus1);

            SalesFunnelStatus leadAccountManagementSalesFunnelStatus2 = new SalesFunnelStatus();
            leadAccountManagementSalesFunnelStatus2.setName("Получили еще возможность");
//            leadAccountManagementSalesFunnelStatus2.setColor("#FF4081");
            leadAccountManagementSalesFunnelStatuses.add(leadAccountManagementSalesFunnelStatus2);

            SalesFunnelStatus leadAccountManagementSalesFunnelStatus3 = new SalesFunnelStatus();
            leadAccountManagementSalesFunnelStatus3.setName("Еще оплата");
//            leadAccountManagementSalesFunnelStatus3.setColor("#F50057");
            leadAccountManagementSalesFunnelStatuses.add(leadAccountManagementSalesFunnelStatus3);

            leadAccountManagementSalesFunnel.setName("Воронка поддержки клиентов в B2B");
            leadAccountManagementSalesFunnel.setPhase(SALES_FUNNEL.ACCOUNT_MANAGEMENT_SALES_FUNNEL);
            leadAccountManagementSalesFunnel.setSalesFunnelStatuses(leadAccountManagementSalesFunnelStatuses);
            salesFunnelRepository.addSalesFunnel(leadAccountManagementSalesFunnel);
        }

        /////// START: LEAD GEN METHOD   ///////
        LeadGenMethod leadGenMethod = new LeadGenMethod();
        leadGenMethod.setLeadGenSalesFunnel(leadGenSalesFunnel);
        leadGenMethod.setLeadConversionSalesFunnel(leadConversionSalesFunnel);
        leadGenMethod.setAccountManagementSalesFunnel(leadAccountManagementSalesFunnel);

        leadGenMethod.setName("Процесс продаж B2B по умолчанию");
        leadRepository.addLeadGenMethod(leadGenMethod);

        LeadGenProject leadGenProject = new LeadGenProject();
        leadGenProject.setName("Проект по умолчанию");
        leadGenProject.setLeadGenMethodId(leadGenMethod.getId());

        List<String> promoCodes = new ArrayList<>();
        promoCodes.add("EB600");
        leadGenProject.setPromoCodes(promoCodes);
        leadRepository.addLeadGenProject(leadGenProject);

        leadGenMethod.getLeadGenProjects().add(leadGenProject);
        leadGenMethod = leadRepository.updateLeadGenMethod(leadGenMethod);

    }

    public void createTestCustomer() {
        Customer customer = new Customer();

        customer.setCustomer(true);
        customer.setFirstName("Владимир");
        customer.setLastName("Белов");
//        customer.setLeadGenMethod(leadGenMethod.getId());
//        customer.setSalesFunnelStatus(leadAccountManagementSalesFunnelStatus2);
//        customer.setLeadGenProject(leadGenProject.getId());
        customer.setTelephone("79159551234");
//        customer.setResponsibleManagerID(user.getId());

        customer.setB2b(false);

        LeadGenMethod leadGenMethod = leadRepository.findLeadGenMethodById(microserviceDomainSettings.findDomainSetting().getDefaultLeadGenMethodID());

        customer.setSalesFunnelStatus(leadGenMethod.getAccountManagementSalesFunnel().getSalesFunnelStatuses().get(2));

        customerRepository.addCustomer(customer);

////////////////////////////////////////////////////////////////////////

        CustomerDeal customerDeal = new CustomerDeal();
        customerDeal.setLeadGenMethodId(customer.getLeadGenMethod());
        customerDeal.setName("Покупка 3D LED телевизор LG 42LB673V");
        customerDeal.setAmount(new BigDecimal("37999"));
        customerDeal.getConnectedInfo().setConnectedCustomerId(customer.getId());
        paymentsRepository.addCustomerDeal(customerDeal);

        CompanyCost companyCost = new CompanyCost();
        companyCost.setName("Оплата по счету #56897-БК Компании ООО Контакт");
        companyCost.setAmount(new BigDecimal("10000"));
        companyCost.setLeadGenProjectId(customer.getLeadGenProject());
        companyCost.setLeadGenMethodId(customer.getLeadGenMethod());

        paymentsRepository.addCompanyCost(companyCost);

//        leadGenProject.getCostsIDs().add(companyCost.getId());
//        leadRepository.updateLeadGenProject(leadGenProject);

////////////////////////////////////////////////////////////////////////

        Opportunity opportunity = new Opportunity();
        opportunity.setName("iPhone 6 Plus");
        opportunity.setAmount(new BigDecimal("38000"));
        opportunity.getConnectedInfo().setConnectedCustomerId(customer.getId());
        opportunity.setActive(true);

        opportunityRepository.addOpportunity(opportunity);

//////////////////// START:  TEST LEADS AND OPPORTUNITIES  ////////////////////

        Customer customer2 = new Customer();

        customer2.setCustomer(true);
        customer2.setFirstName("Иван");
        customer2.setLastName("Петухов");
        customer2.setTelephone("79159551237");
        customer2.setSalesFunnelStatus(leadGenMethod.getAccountManagementSalesFunnel().getSalesFunnelStatuses().get(1));
        customerRepository.addCustomer(customer2);

////////////////////////////////////////////////////////////////////////
        CustomerDeal customerDeal2 = new CustomerDeal();
        customerDeal2.setName("Покупка Nexus 6");
        customerDeal2.setAmount(new BigDecimal("59000"));
        customerDeal2.getConnectedInfo().setConnectedCustomerId(customer2.getId());
        paymentsRepository.addCustomerDeal(customerDeal2);
////////////////////////////////////////////////////////////////////////

        Opportunity opportunity2 = new Opportunity();
        opportunity2.setName("iPhone 6 Plus 64gb");
        opportunity2.setAmount(new BigDecimal("68000"));
        opportunity2.getConnectedInfo().setConnectedCustomerId(customer2.getId());
        opportunity2.setActive(true);
        opportunityRepository.addOpportunity(opportunity2);


        Customer customer3 = new Customer();

        customer3.setCustomer(true);
        customer3.setFirstName("Андрей");
        customer3.setLastName("Карпов");
        customer3.setTelephone("79159551255");
        customerRepository.addCustomer(customer3);
////////////////////////////////////////////////////////////////////////

        CustomerDeal customerDeal3 = new CustomerDeal();
        customerDeal3.setName("Покупка Nexus 6");
        customerDeal3.setAmount(new BigDecimal("12000"));
        customerDeal3.getConnectedInfo().setConnectedCustomerId(customer3.getId());
        paymentsRepository.addCustomerDeal(customerDeal3);

////////////////////////////////////////////////////////////////////////

        Opportunity opportunity3 = new Opportunity();
        opportunity3.setName("iPhone 6 Plus 32gb");
        opportunity3.setAmount(new BigDecimal("48000"));
        opportunity3.getConnectedInfo().setConnectedCustomerId(customer3.getId());
        opportunity3.setActive(true);

        opportunityRepository.addOpportunity(opportunity3);

        Customer customer4 = new Customer();

        customer4.setCustomer(false);
        customer4.setLead(true);
        customer4.setFirstName("Афанасий");
        customer4.setLastName("Еремеев");
        customer4.setTelephone("79159551244");
        customer4.setSalesFunnelStatus(leadGenMethod.getLeadConversionSalesFunnel().getSalesFunnelStatuses().get(3));
        customerRepository.addCustomer(customer4);

////////////////////////////////////////////////////////////////////////

        Opportunity opportunity4 = new Opportunity();
        opportunity4.setName("Покупку Asus Zenbook UX-45");
        opportunity4.setAmount(new BigDecimal("88000"));
        opportunity4.getConnectedInfo().setConnectedCustomerId(customer4.getId());
        opportunity4.setActive(true);
        opportunityRepository.addOpportunity(opportunity4);


        Customer customer5 = new Customer();

        customer5.setCustomer(false);
        customer5.setLead(true);
        customer5.setFirstName("Александр");
        customer5.setLastName("Парабеллум");
        customer5.setTelephone("79153451211");
        customerRepository.addCustomer(customer5);

////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////

        Opportunity opportunity5 = new Opportunity();

        opportunity5.setName("Покупка видео тренингов");
        opportunity5.setAmount(new BigDecimal("118000"));
        opportunity5.getConnectedInfo().setConnectedCustomerId(customer4.getId());
        opportunity5.setActive(true);
        opportunityRepository.addOpportunity(opportunity5);


        Customer customer6 = new Customer();

        customer6.setCustomer(false);
        customer6.setLead(true);
        customer6.setFirstName("Екатарина");
        customer6.setLastName("Новосельцева");
        customer6.setTelephone("79199451211");
        customerRepository.addCustomer(customer6);

////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////

        Opportunity opportunity6 = new Opportunity();

        opportunity6.setName("Покупка видео тренингов");
        opportunity6.setAmount(new BigDecimal("11000"));
        opportunity6.getConnectedInfo().setConnectedCustomerId(customer4.getId());
        opportunity6.setActive(true);
        opportunityRepository.addOpportunity(opportunity6);

////////////////////////////////////////////////////////////////////////
        Task task = new Task();

        task.getConnectedInfo().setConnectedCustomerId(customer.getId());
        task.setName("Позвонить и узнать, что с возможностью iPhone");
        task.setPriority(4);
        task.setFavourite(true);
        task.setStartDate(new Date());

        DateTime dateTime = new DateTime(new Date());
        DateTime tomorrowStart = dateTime.plusDays(1).withTimeAtStartOfDay();

        task.setStartDate(dateTime.toDate());
        task.setFinalDate(tomorrowStart.toDate());

        UserAccountPredicate responsibles = new UserAccountPredicate();

        List<String> userAccounts = new ArrayList<>();
        userAccounts.add(currentUser.getCurrentUser().getId());

        List<Task.CheckListItem> checkListItems = new ArrayList<>();
        Task.CheckListItem checkListItem1 = new Task.CheckListItem();
        checkListItem1.setName("Позвонить");

        Task.CheckListItem checkListItem2 = new Task.CheckListItem();
        checkListItem2.setName("Внести данные в CRM");

        checkListItems.add(checkListItem1);
        checkListItems.add(checkListItem2);

        task.setCheckListItems(checkListItems);

        responsibles.setUserAccountsIDs(userAccounts);
        task.setResponsibles(responsibles);

        taskRepository.addTask(task);
    }

    public void createTaskProjects() {
        TaskProject taskProject = new TaskProject();
        taskProject.setName("Внедрить CRM");

        taskProjectRepository.addProject(taskProject);
    }

    public void createDefaultSegments() {
//        Вcе клиенты - динамические сегменты; важные клиенты; лиды за сегодня; важные лиды
        DynamicSegment dynamicSegment = new DynamicSegment();
        dynamicSegment.setName("Все клиенты");
        dynamicSegment.setDescription("В этот сегмент входят все клиенты");
        CustomerFilter customerBuilder = new CustomerFilter();
        customerBuilder.setCustomer(true);
        dynamicSegment.setCustomerBuilder(customerBuilder);
        segmentsRepository.addDynamicSegment(dynamicSegment);

        DynamicSegment dynamicSegment2 = new DynamicSegment();
        dynamicSegment2.setName("Все лиды");
        dynamicSegment2.setDescription("В этот сегмент входят все лиды");
        CustomerFilter customerBuilder2 = new CustomerFilter();
        customerBuilder2.setLead(true);
        dynamicSegment2.setCustomerBuilder(customerBuilder2);
        segmentsRepository.addDynamicSegment(dynamicSegment2);

        DynamicSegment dynamicSegment3 = new DynamicSegment();
        dynamicSegment3.setName("Важные клиенты и лиды");
        dynamicSegment3.setDescription("В этот сегмент входят все лиды лиды и клиенты, помеченные как `Важный`");
        CustomerFilter customerBuilder3 = new CustomerFilter();
        customerBuilder3.setImportant(true);
        dynamicSegment3.setCustomerBuilder(customerBuilder3);
        segmentsRepository.addDynamicSegment(dynamicSegment3);

        DynamicSegment dynamicSegment4 = new DynamicSegment();
        dynamicSegment4.setName("Лиды за сегодня");
        dynamicSegment4.setDescription("В этот сегмент входят все лиды, созданные с начала текущего дня и до конца текущего дня");
        CustomerFilter customerBuilder4 = new CustomerFilter();
        customerBuilder4.setLead(true);
        customerBuilder4.setUseRelativeCreatedDateFrom(true);
        customerBuilder4.setRelativeCreatedDateFrom(DATE_CONSTS.CURRENT_DAY_START);
        customerBuilder4.setUseRelativeCreatedDateTo(true);
        customerBuilder4.setRelativeCreatedDateTo(DATE_CONSTS.CURRENT_DAY_END);
        dynamicSegment4.setCustomerBuilder(customerBuilder4);
        segmentsRepository.addDynamicSegment(dynamicSegment4);

        DynamicSegment dynamicSegment5 = new DynamicSegment();
        dynamicSegment5.setName("Клиенты и лиды без задач");
        dynamicSegment5.setDescription("В этот сегмент входят все АКТИВНЫЕ клиенты и лиды, у которых нет поставленных АКТИВНЫХ задач т.е нет следующего шага");
        CustomerFilter customerBuilder5 = new CustomerFilter();
        customerBuilder5.setActive(true);
        customerBuilder5.setUseActiveTaskNumberLessThan(true);
        customerBuilder5.setActiveTaskNumberLessThan(0L);
        dynamicSegment5.setCustomerBuilder(customerBuilder5);
        segmentsRepository.addDynamicSegment(dynamicSegment5);

        DynamicSegment dynamicSegment6 = new DynamicSegment();
        dynamicSegment6.setName("Все");
        dynamicSegment6.setDescription("В этот сегмент входят все лиды и клиенты");
        CustomerFilter customerBuilder6 = new CustomerFilter();
        dynamicSegment6.setCustomerBuilder(customerBuilder6);
        segmentsRepository.addDynamicSegment(dynamicSegment6);

        DynamicSegment dynamicSegment7 = new DynamicSegment();
        dynamicSegment7.setName("Мои клиенты");
        dynamicSegment7.setDescription("В этот сегмент входят все АКТИВНЫЕ клиенты, где текущий пользователь - ответственный менеджер");
        CustomerFilter customerBuilder7 = new CustomerFilter();
        customerBuilder7.setShowOnlyWhenIamResponsible(true);
        customerBuilder7.setCustomer(true);
        customerBuilder7.setActive(true);
        dynamicSegment7.setCustomerBuilder(customerBuilder7);
        segmentsRepository.addDynamicSegment(dynamicSegment7);

        DynamicSegment dynamicSegment8 = new DynamicSegment();
        dynamicSegment8.setName("Мои лиды");
        dynamicSegment8.setDescription("В этот сегмент входят все АКТИВНЫЕ лиды, где текущий пользователь - ответственный менеджер");
        CustomerFilter customerBuilder8 = new CustomerFilter();
        customerBuilder8.setShowOnlyWhenIamResponsible(true);
        customerBuilder8.setLead(true);
        customerBuilder8.setActive(true);
        dynamicSegment8.setCustomerBuilder(customerBuilder8);
        segmentsRepository.addDynamicSegment(dynamicSegment8);
    }

    public void createDefaultTaskTemplates() {
        TaskTemplate taskTemplate = new TaskTemplate();
        taskTemplate.setName("Мои задачи на сегодня");
        TaskFilter taskBuilder = new TaskFilter();
        taskBuilder.setOnlyActive(true);
        taskBuilder.setShowOnlyWhenIamResponsible(true);
        taskBuilder.setUseRelativeCreatedDateTo(true);
        taskBuilder.setRelativeCreatedDateTo(DATE_CONSTS.CURRENT_DAY_END);
        taskTemplate.setTaskBuilder(taskBuilder);
        taskTemplateRepository.addTaskTemplate(taskTemplate);

        TaskTemplate taskTemplate2 = new TaskTemplate();
        taskTemplate2.setName("Мои задачи на завтра");
        TaskFilter taskBuilder2 = new TaskFilter();
        taskBuilder2.setOnlyActive(true);
        taskBuilder2.setShowOnlyWhenIamResponsible(true);
        taskBuilder2.setUseRelativeCreatedDateTo(true);
        taskBuilder2.setRelativeCreatedDateTo(DATE_CONSTS.TOMORROW_DAY_END);
        taskTemplate2.setTaskBuilder(taskBuilder2);
        taskTemplateRepository.addTaskTemplate(taskTemplate2);

        TaskTemplate taskTemplate3 = new TaskTemplate();
        taskTemplate3.setName("Мои задачи на неделю");
        TaskFilter taskBuilder3 = new TaskFilter();
        taskBuilder3.setOnlyActive(true);
        taskBuilder3.setShowOnlyWhenIamResponsible(true);
        taskBuilder3.setUseRelativeCreatedDateTo(true);
        taskBuilder3.setRelativeCreatedDateTo(DATE_CONSTS.CURRENT_WEEK_END);
        taskTemplate3.setTaskBuilder(taskBuilder3);
        taskTemplateRepository.addTaskTemplate(taskTemplate3);

        TaskTemplate taskTemplate4 = new TaskTemplate();
        taskTemplate4.setName("Мои задачи на месяц");
        TaskFilter taskBuilder4 = new TaskFilter();
        taskBuilder4.setOnlyActive(true);
        taskBuilder4.setShowOnlyWhenIamResponsible(true);
        taskBuilder4.setUseRelativeCreatedDateTo(true);
        taskBuilder4.setRelativeCreatedDateTo(DATE_CONSTS.CURRENT_MONTH_END);
        taskTemplate4.setTaskBuilder(taskBuilder4);
        taskTemplateRepository.addTaskTemplate(taskTemplate4);

        TaskTemplate taskTemplate5 = new TaskTemplate();
        taskTemplate5.setName("Мои важные задачи");
        TaskFilter taskBuilder5 = new TaskFilter();
        taskBuilder5.setOnlyActive(true);
        taskBuilder5.setShowOnlyWhenIamResponsible(true);
        taskBuilder5.setOnlyFavourite(true);
        taskTemplate5.setTaskBuilder(taskBuilder5);
        taskTemplateRepository.addTaskTemplate(taskTemplate5);
    }


    public void biznesMolodostDefault() {
        StaticSegment staticSegment = new StaticSegment();
        staticSegment.setName("0,5-е лиды");
        staticSegment.setDescription("По шкале от 0 до 1 - это лиды, " +
                "заинтересованные с нами работать и мы заинтересованы с ними работать," +
                " Последний контакт был менее недели назад, но стоимость конечная не согласована и договора так же нет");
        segmentsRepository.addStaticSegment(staticSegment);

        StaticSegment staticSegment2 = new StaticSegment();
        staticSegment2.setName("0,9-е лиды");
        staticSegment2.setDescription("По шкале от 0 до 1 - это лиды, с которыми у нас уже есть соглашение( крайне желательно письменное ), что мы будем работать вместе, но" +
                " денежные средства на наш счет еще не поступили, либо не выполнен другой важный пункт договора ");
        segmentsRepository.addStaticSegment(staticSegment2);

        DynamicSegment dynamicSegment3 = new DynamicSegment();
        dynamicSegment3.setName("0,9-е лиды");
        dynamicSegment3.setDescription("0,9-е лиды");
        dynamicSegment3.setDescription("Лиды с сегментом `0,9-е лиды`. По шкале от 0 до 1 - это лиды, с которыми у нас уже есть соглашение( крайне желательно письменное ), что мы будем работать вместе, но" +
                " денежные средства на наш счет еще не поступили, либо не выполнен другой важный пункт договора ");
        CustomerFilter customerBuilder3 = new CustomerFilter();
        customerBuilder3.getStaticSegmentsIDs().add(staticSegment2.getId());
        customerBuilder3.setLead(true);
        customerBuilder3.setUseStaticSegments(true);
        dynamicSegment3.setCustomerBuilder(customerBuilder3);
        segmentsRepository.addDynamicSegment(dynamicSegment3);

        DynamicSegment dynamicSegment4 = new DynamicSegment();
        dynamicSegment4.setName("0,5-е лиды");
        dynamicSegment4.setDescription("0,5-е лиды");
        dynamicSegment4.setDescription("Лиды с сегментом `0,5-е лиды`. По шкале от 0 до 1 - это лиды, " +
                "заинтересованные с нами работать и мы заинтересованы с ними работать," +
                " Последний контакт был менее недели назад, но стоимость конечная не согласована и договора так же нет");
        CustomerFilter customerBuilder4 = new CustomerFilter();
        customerBuilder4.getStaticSegmentsIDs().add(staticSegment.getId());
        customerBuilder4.setLead(true);
        customerBuilder4.setUseStaticSegments(true);
        dynamicSegment4.setCustomerBuilder(customerBuilder4);
        segmentsRepository.addDynamicSegment(dynamicSegment4);

    }

}
