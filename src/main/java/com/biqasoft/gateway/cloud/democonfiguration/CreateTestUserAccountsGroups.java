/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.cloud.democonfiguration;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.core.useraccount.UserAccountGroup;
import com.biqasoft.gateway.useraccount.MicroserviceUserAccountGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Nikita Bakaev, ya@nbakaev.ru on 4/21/2016.
 * All Rights Reserved
 */
@Service
public class CreateTestUserAccountsGroups {

    private final MicroserviceUserAccountGroup microserviceUserAccountGroup;

    @Autowired
    public CreateTestUserAccountsGroups(MicroserviceUserAccountGroup microserviceUserAccountGroup) {
        this.microserviceUserAccountGroup = microserviceUserAccountGroup;
    }


    public void createUserAccountsGroups(){

        {
            UserAccountGroup marketing = new UserAccountGroup();
            marketing.setName("Маркетологи");

            List<String> marketingRoles = marketing.getGrantedRoles();

            marketingRoles.add(SYSTEM_ROLES.LEAD_GEN_METHOD_ADD);
            marketingRoles.add(SYSTEM_ROLES.LEAD_GEN_METHOD_EDIT);
            marketingRoles.add(SYSTEM_ROLES.LEAD_GEN_METHOD_EXCEL);
            marketingRoles.add(SYSTEM_ROLES.LEAD_GEN_METHOD_GET_ALL);
            marketingRoles.add(SYSTEM_ROLES.LEAD_GEN_METHOD_ROOT);
            marketingRoles.add(SYSTEM_ROLES.KPI_LEAD_GEN_METHOD);

            microserviceUserAccountGroup.create(marketing);
        }

        {
            UserAccountGroup baseGroup = new UserAccountGroup();
            baseGroup.setName("Базовая группа");
            baseGroup.setDescription("Все пользователи могут безопасно иметь эту группу");

            List<String> baseGroupRole = baseGroup.getGrantedRoles();

            baseGroupRole.add(SYSTEM_ROLES.USER_ACCOUNT_ROOT);
            baseGroupRole.add(SYSTEM_ROLES.USER_ACCOUNT_GET);
            baseGroupRole.add(SYSTEM_ROLES.DOMAIN_COMPANY_GET_BASIC);

            // allow get all tasks
            baseGroupRole.add(SYSTEM_ROLES.TASK_ROOT);
            baseGroupRole.add(SYSTEM_ROLES.TASK_GET);
            baseGroupRole.add(SYSTEM_ROLES.TASK_ADD);
            baseGroupRole.add(SYSTEM_ROLES.TASK_EDIT);

            // allow get task projects
            baseGroupRole.add(SYSTEM_ROLES.TASK_PROJECT_GET);
            baseGroupRole.add(SYSTEM_ROLES.TASK_PROJECT_ROOT);

            // to allow get background widgets
            baseGroupRole.add(SYSTEM_ROLES.WIDGET_ROOT);

            microserviceUserAccountGroup.create(baseGroup);
        }

        {
            UserAccountGroup managersCRM = new UserAccountGroup();
            managersCRM.setName("Менеджеры в CRM");

            List<String> managersCRMRole = managersCRM.getGrantedRoles();
            managersCRMRole.add(SYSTEM_ROLES.CUSTOMER_ROOT);
            managersCRMRole.add(SYSTEM_ROLES.CUSTOMER_ADD);
            managersCRMRole.add(SYSTEM_ROLES.CUSTOMER_EDIT);

            managersCRMRole.add(SYSTEM_ROLES.PAYMENT_ROOT);
            managersCRMRole.add(SYSTEM_ROLES.PAYMENT_ADD_CUSTOMER_DEAL);
            managersCRMRole.add(SYSTEM_ROLES.PAYMENT_EDIT_CUSTOMER_DEALS);
            managersCRMRole.add(SYSTEM_ROLES.PAYMENT_GET_CUSTOMER_DEALS);

            // to get sales funnel
            managersCRMRole.add(SYSTEM_ROLES.LEAD_GEN_METHOD_ROOT);
            managersCRMRole.add(SYSTEM_ROLES.LEAD_GEN_METHOD_GET_ALL);

            microserviceUserAccountGroup.create(managersCRM);
        }

        {
            UserAccountGroup admin = new UserAccountGroup();
            admin.setName("Администратор");

            List<String> adminRoles = admin.getGrantedRoles();
            adminRoles.add(SYSTEM_ROLES.ROLE_ADMIN);
            adminRoles.add(SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED);

            microserviceUserAccountGroup.create(admin);
        }

        {
            UserAccountGroup admin = new UserAccountGroup();
            admin.setName("Высшее руководство");

            List<String> adminRoles = admin.getGrantedRoles();
            adminRoles.add(SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED);

            microserviceUserAccountGroup.create(admin);
        }

    }

}
