/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.tasks.controllers;

import com.biqasoft.entity.constants.SystemRoles;
import com.biqasoft.entity.tasks.TaskProject;
import com.biqasoft.gateway.tasks.repositories.TaskProjectRepository;
import com.biqasoft.gateway.tasks.repositories.TaskRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Api(value = "Task project")
@Secured(value = {SystemRoles.TASK_PROJECT_ROOT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
@RestController
@RequestMapping(value = "/v1/task/project")
public class TaskProjectController {

    private final TaskProjectRepository taskProjectRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public TaskProjectController(TaskRepository taskRepository, TaskProjectRepository taskProjectRepository) {
        this.taskRepository = taskRepository;
        this.taskProjectRepository = taskProjectRepository;
    }

    @Secured(value = {SystemRoles.TASK_PROJECT_GET, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get task project by id")
    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    public TaskProject findTaskProjectById(@PathVariable("id") String id) {
        TaskProject taskProject = taskProjectRepository.findTaskProjectById(id);
        taskProject.setTasks(taskRepository.findAllTaskByTaskProjectId(taskProject.getId()));

        return taskProject;
    }

    @Secured(value = {SystemRoles.TASK_PROJECT_GET, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all tasks from some project id")
    @RequestMapping(value = "all/with_tasks", method = RequestMethod.GET)
    public List<TaskProject> getTaskProjcetWithTasks() {
        List<TaskProject> taskProjects = taskProjectRepository.findAllTaskProjects();

        for (TaskProject taskProject : taskProjects) {
            taskProject.setTasks(taskRepository.findAllTaskByTaskProjectId(taskProject.getId()));
        }

        return taskProjects;
    }

    @Secured(value = {SystemRoles.TASK_PROJECT_GET, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "get all task projects")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<TaskProject> findAllTaskProjects() {
        return taskProjectRepository.findAllTaskProjects();
    }

    @Secured(value = {SystemRoles.TASK_PROJECT_ADD, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "add new task project")
    @RequestMapping(method = RequestMethod.POST)
    public TaskProject addNewTaskProject(@RequestBody TaskProject task, HttpServletResponse response) {
        taskProjectRepository.addProject(task);

        response.setStatus(HttpServletResponse.SC_CREATED);
        return task;
    }

    @Secured(value = {SystemRoles.TASK_PROJECT_EDIT, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "update task project")
    @RequestMapping(method = RequestMethod.PUT)
    public TaskProject updateProject(@RequestBody TaskProject task, HttpServletResponse response) {
        task.setTasks(new ArrayList<>());  /// To avoid task filed on property to recursive

        response.setStatus(HttpServletResponse.SC_OK);
        return taskProjectRepository.updateProject(task);
    }

    @Secured(value = {SystemRoles.TASK_PROJECT_DELETE, SystemRoles.ALLOW_ALL_DOMAIN_BASED, SystemRoles.ROLE_ADMIN})
    @ApiOperation(value = "delete task project")
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void updateProject(HttpServletResponse response, @PathVariable("id") String id) {
        taskProjectRepository.deleteTaskProjectById(id);
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
