package com.bearingpoint.beyond.test-bpintegration.service.handler;


import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class CleanupHandler {

    private final TasksService tasksService;


    public void cleanupTask(TaskInfo currentTask) {
        TaskV2DomainTask oldTask = tasksService.getTask(currentTask.getTenant(), currentTask.getTaskId());

        if ("Acknowledged".equals(oldTask.getState())) {
            TaskV2DomainTask newTask = new TaskV2DomainTask();
            newTask.setState("InProgress");
            newTask.setAssignee("assisted_sales_workflow");
            newTask.setId(oldTask.getId());

            Map<String, String> parameters = oldTask.getParameters();
            parameters.put("CANCELLED", "true");

            newTask.setParameters(parameters);

            log.debug("Update Task (id={}) to InProgress state.", currentTask.getTaskId());
            tasksService.updateTask(currentTask.getTenant(), newTask);

            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {

            }

            TaskType taskType = TaskType.valueOf(oldTask.getTaskDefinition());

            newTask.setResolutionAction(taskType.getGoodPathResolution());
            newTask.setState("Resolved");

            log.debug("Update Task (id={}) to Resolved state.", currentTask.getTaskId());

            tasksService.updateTask(currentTask.getTenant(), newTask);
        } else if ("InProgress".equals(oldTask.getState())) {
            TaskType taskType = TaskType.valueOf(oldTask.getTaskDefinition());

            TaskV2DomainTask newTask = new TaskV2DomainTask();
            newTask.setResolutionAction(taskType.getGoodPathResolution());
            newTask.setState("Resolved");
            newTask.setId(oldTask.getId());

            Map<String, String> parameters = oldTask.getParameters();
            parameters.put("CANCELLED", "true");
            newTask.setParameters(parameters);

            log.debug("Update Task (id={}) to Resolved state.", currentTask.getTaskId());

            tasksService.updateTask(currentTask.getTenant(), newTask);
        }
    }
}
