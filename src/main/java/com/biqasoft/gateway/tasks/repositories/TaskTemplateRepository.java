/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.tasks.repositories;

import com.biqasoft.entity.annotations.BiqaAddObject;
import com.biqasoft.entity.tasks.TaskTemplate;
import com.biqasoft.microservice.database.TenantDatabase;
import com.biqasoft.persistence.base.BiqaObjectFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskTemplateRepository {

    private final MongoOperations ops;
    private final BiqaObjectFilterService biqaObjectFilterService;

    @Autowired
    public TaskTemplateRepository(@TenantDatabase MongoOperations ops, BiqaObjectFilterService biqaObjectFilterService) {
        this.ops = ops;
        this.biqaObjectFilterService = biqaObjectFilterService;
    }

    // TASK TEMPLATES
    @BiqaAddObject
    public TaskTemplate addTaskTemplate(TaskTemplate note) {
        ops.insert(note);
        return note;
    }

    public TaskTemplate updateTaskTemplate(TaskTemplate note) {
        return biqaObjectFilterService.safeUpdate(note, ops);
    }

    public List<TaskTemplate> findAllTaskTemplate() {
        return ops.findAll(TaskTemplate.class);
    }

    public TaskTemplate findTaskTemplateById(String id) {
        return ops.findOne(Query.query(Criteria.where("id").is(id)), TaskTemplate.class);
    }

}
