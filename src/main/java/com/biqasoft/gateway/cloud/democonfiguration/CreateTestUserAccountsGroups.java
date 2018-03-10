/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.cloud.democonfiguration;

import com.biqasoft.entity.constants.SystemRoles;
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

            marketingRoles.add(SystemRoles.LEAD_GEN_METHOD_ADD);
            marketingRoles.add(SystemRoles.LEAD_GEN_METHOD_EDIT);
            marketingRoles.add(SystemRoles.LEAD_GEN_METHOD_EXCEL);
            marketingRoles.add(SystemRoles.LEAD_GEN_METHOD_GET_ALL);
            marketingRoles.add(SystemRoles.LEAD_GEN_METHOD_ROOT);
            marketingRoles.add(SystemRoles.KPI_LEAD_GEN_METHOD);

            microserviceUserAccountGroup.create(marketing);
        }

        {
            UserAccountGroup baseGroup = new UserAccountGroup();
            baseGroup.setName("Базовая группа");
            baseGroup.setDescription("Все пользователи могут безопасно иметь эту группу");

            List<String> baseGroupRole = baseGroup.getGrantedRoles();

            baseGroupRole.add(SystemRoles.USER_ACCOUNT_ROOT);
            baseGroupRole.add(SystemRoles.USER_ACCOUNT_GET);
            baseGroupRole.add(SystemRoles.DOMAIN_COMPANY_GET_BASIC);

            // allow get all tasks
            baseGroupRole.add(SystemRoles.TASK_ROOT);
            baseGroupRole.add(SystemRoles.TASK_GET);
            baseGroupRole.add(SystemRoles.TASK_ADD);
            baseGroupRole.add(SystemRoles.TASK_EDIT);

            // allow get task projects
            baseGroupRole.add(SystemRoles.TASK_PROJECT_GET);
            baseGroupRole.add(SystemRoles.TASK_PROJECT_ROOT);

            // to allow get background widgets
            baseGroupRole.add(SystemRoles.WIDGET_ROOT);

            microserviceUserAccountGroup.create(baseGroup);
        }

        {
            UserAccountGroup managersCRM = new UserAccountGroup();
            managersCRM.setName("Менеджеры в CRM");

            List<String> managersCRMRole = managersCRM.getGrantedRoles();
            managersCRMRole.add(SystemRoles.CUSTOMER_ROOT);
            managersCRMRole.add(SystemRoles.CUSTOMER_ADD);
            managersCRMRole.add(SystemRoles.CUSTOMER_EDIT);

            managersCRMRole.add(SystemRoles.PAYMENT_ROOT);
            managersCRMRole.add(SystemRoles.PAYMENT_ADD_CUSTOMER_DEAL);
            managersCRMRole.add(SystemRoles.PAYMENT_EDIT_CUSTOMER_DEALS);
            managersCRMRole.add(SystemRoles.PAYMENT_GET_CUSTOMER_DEALS);

            // to get sales funnel
            managersCRMRole.add(SystemRoles.LEAD_GEN_METHOD_ROOT);
            managersCRMRole.add(SystemRoles.LEAD_GEN_METHOD_GET_ALL);

            microserviceUserAccountGroup.create(managersCRM);
        }

        {
            UserAccountGroup admin = new UserAccountGroup();
            admin.setName("Администратор");

            List<String> adminRoles = admin.getGrantedRoles();
            adminRoles.add(SystemRoles.ROLE_ADMIN);
            adminRoles.add(SystemRoles.ALLOW_ALL_DOMAIN_BASED);

            microserviceUserAccountGroup.create(admin);
        }

        {
            UserAccountGroup admin = new UserAccountGroup();
            admin.setName("Высшее руководство");

            List<String> adminRoles = admin.getGrantedRoles();
            adminRoles.add(SystemRoles.ALLOW_ALL_DOMAIN_BASED);

            microserviceUserAccountGroup.create(admin);
        }

    }

}
