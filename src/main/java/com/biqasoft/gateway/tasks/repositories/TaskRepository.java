/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.tasks.repositories;

import com.biqasoft.common.exceptions.ThrowExceptionHelper;
import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.core.CurrentUser;
import com.biqasoft.entity.core.useraccount.UserAccount;
import com.biqasoft.entity.filters.TaskFilter;
import com.biqasoft.entity.format.BiqaPaginationResultList;
import com.biqasoft.entity.tasks.Task;
import com.biqasoft.gateway.cloud.DateServiceRequestContext;
import com.biqasoft.gateway.customer.repositories.CustomerRepository;
import com.biqasoft.gateway.email.services.EmailPrepareAndSendService;
import com.biqasoft.microservice.common.MicroserviceUsersRepository;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import com.biqasoft.persistence.base.DateService;
import com.biqasoft.storage.entity.StorageFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskRepository {

    private final MongoOperations ops;
    private final BiqaObjectFilterService biqaObjectFilterService;
    private CustomerRepository customerRepository;
    private final DateService dateServiceRequestContext;
    private final CurrentUser currentUser;
    private final EmailPrepareAndSendService emailPrepareAndSendService;
    private final MicroserviceUsersRepository microserviceUsersRepository;

    @Autowired
    public TaskRepository(@TenantDatabase MongoOperations ops, EmailPrepareAndSendService emailPrepareAndSendService, DateService dateServiceRequestContext,
                          CurrentUser currentUser, BiqaObjectFilterService biqaObjectFilterService, MicroserviceUsersRepository microserviceUsersRepository) {
        this.ops = ops;
        this.emailPrepareAndSendService = emailPrepareAndSendService;
        this.dateServiceRequestContext = dateServiceRequestContext;
        this.currentUser = currentUser;
        this.biqaObjectFilterService = biqaObjectFilterService;
        this.microserviceUsersRepository = microserviceUsersRepository;
    }

    @Autowired
    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @BiqaAddObject
    @BiqaAuditObject
    public Task addTask(Task task) {
        ops.insert(task);

        if (task.getConnectedInfo().getConnectedCustomerId() != null && !task.getConnectedInfo().getConnectedCustomerId().equals("")) {
            customerRepository.refreshCustomerOverview(task.getConnectedInfo().getConnectedCustomerId());
        }

        List<UserAccount> responsibeles = getResponsibleForTask(task);

        if (responsibeles != null) {
            for (UserAccount userAccount : responsibeles) {
                if (userAccount == null || userAccount.getEmail() == null) continue;
                emailPrepareAndSendService.addNewTaskEmailSendResponsible(userAccount, task);
            }
        }

        return task;
    }

    private List<UserAccount> getResponsibleForTask(Task task) {
        if (task.getResponsibles() == null || task.getResponsibles().getUserAccountsIDs().size() == 0) return null;

        return microserviceUsersRepository.findAllUsers().stream().filter(x -> task.getResponsibles().getUserAccountsIDs().contains(x.getId())).collect(Collectors.toList());
    }

    public List<Task> findTasksForCustomer(String customerID) {
        TaskFilter taskBuilder = new TaskFilter();
        taskBuilder.setUseConnectedCustomerId(true);
        taskBuilder.setConnectedCustomerId(customerID);

        return getTaskByFilter(taskBuilder).getResultedObjects();
    }

    public BiqaPaginationResultList<Task> getTaskByFilter(TaskFilter filter) {
        Criteria criteria = biqaObjectFilterService.getCriteriaFromAbstractBuilder(filter);
        Query query = biqaObjectFilterService.getQueryFromFilter(filter, criteria);

        if (filter.isUseCompletedDateFrom() && !filter.isUseCompletedDateTo()) {
            criteria.and("completedDate").gte(dateServiceRequestContext.parseDateExpression(filter.getCompletedDateFrom()));
        }

        if (!filter.isUseCompletedDateFrom() && filter.isUseCompletedDateTo()) {
            criteria.and("completedDate").lte(dateServiceRequestContext.parseDateExpression(filter.getCompletedDateTo()));
        }

        if (filter.isUseCompletedDateFrom() && filter.isUseCompletedDateTo()) {
            criteria.and("completedDate")
                    .gte(dateServiceRequestContext.parseDateExpression(filter.getCompletedDateFrom()))
                    .lte(dateServiceRequestContext.parseDateExpression(filter.getCompletedDateTo()));
        }

        //
        if (filter.isOnlyDone()) criteria.and("completed").is(true);
        if (filter.isOnlyActive()) criteria.and("completed").is(false);
        if (filter.isOnlyFavourite()) criteria.and("favourite").is(true);
        //

        if (filter.isUseConnectedCustomerId())
            criteria.and("connectedInfo.connectedCustomerId").is(filter.getConnectedCustomerId());

        if (filter.getResponsibles() != null && !filter.isShowOnlyWhenIamResponsible()) {
            if (filter.getResponsibles().getUserAccountsIDs() != null && filter.getResponsibles().getUserAccountsIDs().size() > 0) {
                criteria.and("responsibles.userAccountsIDs").in(filter.getResponsibles().getUserAccountsIDs());
            }
        }

        // only more then priority filter
        if (filter.isUsePriorityMoreThan() && !filter.isUsePriorityLessThan())
            criteria.and("priority").gte(filter.getPriorityMoreThan());
        // only less then priority filter
        if (filter.isUsePriorityLessThan() && !filter.isUsePriorityMoreThan())
            criteria.and("priority").lte(filter.getPriorityLessThan());
        //  less then and more then priority filter together
        if (filter.isUsePriorityLessThan() && filter.isUsePriorityMoreThan())
            criteria.and("priority").lte(filter.getPriorityLessThan()).gte(filter.getPriorityMoreThan());

        if (filter.isShowOnlyWhenIamResponsible()) {
            criteria.and("responsibles.userAccountsIDs").in(currentUser.getCurrentUser().getId());
        }

        return biqaObjectFilterService.getPaginationResultList(filter, criteria, query, Task.class, ops );
    }

    public Task addDocuemntFileToTaskById(String id, StorageFile documentFile) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().push("connectedInfo.connectedFiles", documentFile.getId());
        return ops.findAndModify(query, update, Task.class);
    }

    private void checkChecklistForTask(Task task) {
        if (task.getCheckListItems() != null && task.getCheckListItems().size() > 0) {
            long completedTask = task.getCheckListItems().stream().filter(x -> x.isDone()).count();

            if (completedTask != task.getCheckListItems().size()) {
                if (!currentUser.getCurrentUserDomain().isAllowCompleteTaskWithoutCheckList()) {
                    ThrowExceptionHelper.throwExceptionInvalidRequestLocalized("tasks.done_no_checklist");
                }
            }
        }
    }

    /**
     * update task
     *
     * @param task
     * @return
     */
    @BiqaAuditObject
    public Task updateTask(Task task) {
        Task oldTask = findTaskById(task.getId());

        // если этот запрос - завершили задачу
        if (!oldTask.isCompleted() && task.isCompleted()) {
            checkChecklistForTask(task);

            List<UserAccount> responsibeles = getResponsibleForTask(task);
            task.setCompletedDate(new Date());

            if (responsibeles != null) {
                for (UserAccount userAccount : responsibeles) {
                    if (userAccount == null || userAccount.getEmail() == null) continue;
                    emailPrepareAndSendService.sendTaskDoneEmail(userAccount, task);
                }
            }
        }

        task = biqaObjectFilterService.safeUpdate(task, ops);

        if (task.getConnectedInfo().getConnectedCustomerId() != null && !task.getConnectedInfo().getConnectedCustomerId().equals("")) {
            customerRepository.refreshCustomerOverview(task.getConnectedInfo().getConnectedCustomerId());
        }
        return task;
    }

    public void deleteTask(Task task) {
        ops.remove(task);
    }

    public Task findTaskById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id)), Task.class);
    }

    public List<Task> findAll() {
        return ops.findAll(Task.class);
    }

    public List<Task> findAllTaskByTaskProjectId(String taskId) {
        return ops.find(Query.query(Criteria.where("taskProjectId").is(taskId)), Task.class);
    }

}
