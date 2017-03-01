/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.tasks.repositories;

import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.annotations.BiqaAuditObject;
import com.biqasoft.entity.tasks.Task;
import com.biqasoft.entity.tasks.TaskProject;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskProjectRepository {

    private final MongoOperations ops;
    private final BiqaObjectFilterService biqaObjectFilterService;
    private final TaskRepository taskRepository;

    @Autowired
    public TaskProjectRepository(TaskRepository taskRepository, @TenantDatabase MongoOperations ops, BiqaObjectFilterService biqaObjectFilterService) {
        this.taskRepository = taskRepository;
        this.ops = ops;
        this.biqaObjectFilterService = biqaObjectFilterService;
    }

    @BiqaAddObject
    @BiqaAuditObject
    public TaskProject addProject(TaskProject note) {
        ops.insert(note);
        return note;
    }

    @BiqaAuditObject
    public TaskProject updateProject(TaskProject taskProject) {
        return biqaObjectFilterService.safeUpdate(taskProject, ops);
    }

    public boolean deleteTaskProjectById(String id) {

        List<Task> tasks = taskRepository.findAllTaskByTaskProjectId(id);
        for (Task task : tasks) {
            task.setTaskProjectID(null);
            taskRepository.updateTask(task);
        }

        TaskProject note = findTaskProjectById(id);
        ops.remove(note);
        return true;
    }

    public TaskProject findTaskProjectById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id)), TaskProject.class);
    }

    public List<TaskProject> findAllTaskProjects() {
        return ops.findAll(TaskProject.class);
    }

}
