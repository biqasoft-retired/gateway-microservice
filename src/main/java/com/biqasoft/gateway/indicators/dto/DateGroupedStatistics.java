/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.gateway.indicators.dto;

import com.biqasoft.entity.tasks.Task;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DateGroupedStatistics {

    private int allEntities;
    private int doneTasks = 0;
    private int activeTasks = 0;
    private int createdTask = 0;
    private int overdueTasks = 0;


    private List<Task> taskList = new ArrayList<>();

}
