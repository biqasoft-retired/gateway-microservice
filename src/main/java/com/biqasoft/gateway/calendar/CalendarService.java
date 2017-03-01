/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.calendar;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.Status;
import com.biqasoft.gateway.tasks.repositories.TaskRepository;
import com.biqasoft.entity.tasks.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Used {@code https://github.com/mangstadt/biweekly} library
 *
 * @author Nikita Bakaev, ya@nbakaev.ru
 *         Date: 6/8/2016
 *         All Rights Reserved
 */
@Service
public class CalendarService {

    private final TaskRepository taskRepository;
    private String basicUrl;

    @Autowired
    public CalendarService(TaskRepository taskRepository, @Value("${biqa.urls.http.cloud}") String basicUrl) {
        this.taskRepository = taskRepository;
        this.basicUrl = basicUrl;
    }

    /**
     * Generate ICalendar string
     * @param tasks tasks that will be converted to icalendar events
     * @return generate ICalendar string that can be imported in google calendar for example
     */
    public String createCalendarForTasks(List<Task> tasks) {
        ICalendar ical = new ICalendar();

        for (Task task : tasks) {
            if (task.getFinalDate() == null) {
                continue;
            }

            VEvent event = new VEvent();
            event.setCreated(task.getCreatedInfo().getCreatedDate());
            event.setDateEnd(task.getFinalDate());
            event.setPriority(task.getPriority());
            event.setDateStart(task.getStartDate());
            event.setUid(task.getId());
            event.setDescription(task.getDescription());
            event.setSummary(task.getName());
            event.setUrl(basicUrl + "/task/details/" + task.getId());

            if (task.isCompleted()) {
                event.setStatus(Status.completed());
            } else {
                event.setStatus(Status.confirmed());
            }

            if (task.getPriority() == 6) {
                event.setColor("red");
            } else if (task.getPriority() > 4 && task.getPriority() < 6) {
                event.setColor("yellow");
            }

            ical.addEvent(event);
        }

        String str = Biweekly.write(ical).go();
        return str;
    }

    /**
     * Get calendar which include all tasks
     * @return
     */
    public String createCalendar() {
        List<Task> tasks = taskRepository.findAll();
        return createCalendarForTasks(tasks);
    }

}
