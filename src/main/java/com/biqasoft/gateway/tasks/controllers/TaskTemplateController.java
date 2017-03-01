/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.tasks.controllers;

import com.biqasoft.entity.constants.SYSTEM_ROLES;
import com.biqasoft.entity.tasks.TaskTemplate;
import com.biqasoft.gateway.tasks.repositories.TaskTemplateRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Secured(value = {SYSTEM_ROLES.TASK_TEMPLATE_ROOT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
@Api(value = "Segments - customers & leads ")
@RequestMapping(value = "/v1/task/template")
public class TaskTemplateController {

    private final TaskTemplateRepository taskRepository;

    @Autowired
    public TaskTemplateController(TaskTemplateRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Secured(value = {SYSTEM_ROLES.TASK_TEMPLATE_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get all task templates ")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<TaskTemplate> findAllTaskTemplate() {
        return taskRepository.findAllTaskTemplate();
    }

    @Secured(value = {SYSTEM_ROLES.TASK_TEMPLATE_GET, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @ApiOperation(value = "get task template by id ")
    @RequestMapping(value = "id/{id}", method = RequestMethod.GET)
    public TaskTemplate findTaskTemplateById(@PathVariable("id") String id) {
        return taskRepository.findTaskTemplateById(id);
    }

    @ApiOperation(value = "add new task template")
    @Secured(value = {SYSTEM_ROLES.TASK_TEMPLATE_ADD, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @RequestMapping(value = "", method = RequestMethod.POST)
    public TaskTemplate addTaskTemplate(@RequestBody TaskTemplate staticSegment, HttpServletResponse response) {
        taskRepository.addTaskTemplate(staticSegment);
        response.setStatus(HttpServletResponse.SC_CREATED);
        return staticSegment;
    }

    @ApiOperation(value = "update task template")
    @Secured(value = {SYSTEM_ROLES.TASK_TEMPLATE_EDIT, SYSTEM_ROLES.ALLOW_ALL_DOMAIN_BASED, SYSTEM_ROLES.ROLE_ADMIN})
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public TaskTemplate updateTaskTemplate(@RequestBody TaskTemplate staticSegment, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_CREATED);
        return taskRepository.updateTaskTemplate(staticSegment);
    }

}



