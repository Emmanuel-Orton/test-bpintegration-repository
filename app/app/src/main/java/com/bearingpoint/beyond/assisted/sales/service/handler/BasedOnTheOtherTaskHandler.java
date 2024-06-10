package com.bearingpoint.beyond.test-bpintegration.service.handler;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

@Slf4j
@Component
@RequiredArgsConstructor
public class BasedOnTheOtherTaskHandler {

    private final TasksService tasksService;
    private final InfonovaLinkService infonovaLinkService;

    private static final Set<String> DEFAULT_EXCLUDED_PARAMETERS = Set.of("CSM_USER");

    public TaskV2DomainTask createNewTaskFromOldOne(String oldTenant, String oldTaskId, TaskType newTaskType) {
        return createNewTaskFromOldOne(oldTenant, oldTaskId, newTaskType, true, emptySet());
    }

    public TaskV2DomainTask createNewTaskFromOldOne(String oldTenant, String oldTaskId, TaskType newTaskType, boolean createTask) {
        return createNewTaskFromOldOne(oldTenant, oldTaskId, newTaskType, createTask, emptySet());
    }

    public TaskV2DomainTask createNewTaskFromOldOne(String oldTenant, String oldTaskId, TaskType newTaskType, boolean createTask, Set<String> exclParams) {
        log.info("{} new task from old one newTaskType={}, oldTaskId={}", createTask? "creating" : "copying", newTaskType, oldTaskId);

        TaskV2DomainTask oldTask = tasksService.getTask(oldTenant, oldTaskId);
        exclParams = Stream.concat(exclParams.stream(), DEFAULT_EXCLUDED_PARAMETERS.stream()).collect(Collectors.toSet());
        Map<String, String> newParameters = filterParameters(exclParams, oldTask);


        final TaskV2DomainTask task = getTaskCopy(newTaskType, oldTask, newParameters);

        if (createTask) {
            return tasksService.createTask(infonovaLinkService.getCorrectTenant(newTaskType.getSourceTenant()), task);
        } else {
            return task;
        }
    }

    public TaskV2DomainTask createTask(TaskV2DomainTask task, TaskType newTaskType) {
        log.info("creating new task from previously prepared one: newTaskType={}", newTaskType);

        return tasksService.createTask(infonovaLinkService.getCorrectTenant(newTaskType.getSourceTenant()), task);
    }


    public TaskV2DomainTask createNewTaskFromOldOneWithClosingNote(String taskId, String taskTenant,
                                                                   TaskInfo noteTaskInfo,
                                                                   TaskType newTaskType) {
        return createNewTaskFromOldOneWithClosingNote(taskId, taskTenant, noteTaskInfo, newTaskType, true, emptySet());
    }
    public TaskV2DomainTask createNewTaskFromOldOneWithClosingNote(String taskId, String taskTenant,
            TaskInfo noteTaskInfo,
            TaskType newTaskType, boolean createTask, Set<String> exclParams) {
        log.info("creating new task from old one with closing note newTaskType={}, oldTaskId={}/{}, noteTaskId={}/{}",
                newTaskType, taskId, taskTenant, noteTaskInfo.getTaskId(), noteTaskInfo.getTenant());

        TaskV2DomainTask oldTask = tasksService.getTask(taskTenant, taskId);

        exclParams = Stream.concat(exclParams.stream(), DEFAULT_EXCLUDED_PARAMETERS.stream()).collect(Collectors.toSet());
        Map<String, String> newParameters = filterParameters(exclParams, oldTask);
        String taskResolutionText = tasksService.getTaskResolutionText(noteTaskInfo.getTenant(), noteTaskInfo.getTaskId());
        newParameters.put("NOTES", taskResolutionText); // backward compatibility can be removed later
        newParameters.put("TASK_RESOLUTION_COMMENT", taskResolutionText);

        final TaskV2DomainTask task = getTaskCopy(newTaskType, oldTask, newParameters);
        if (createTask) {
            return tasksService.createTask(infonovaLinkService.getCorrectTenant(newTaskType.getSourceTenant()), task);
        } else {
            return task;
        }
    }

    private TaskV2DomainTask getTaskCopy(TaskType newTaskType, TaskV2DomainTask oldTask, Map<String, String> newParameters) {
        return new TaskV2DomainTask()
                .taskDefinition(newTaskType.name())
                .billingAccount(oldTask.getBillingAccount())
                .relatedServiceOrders(oldTask.getRelatedServiceOrders())
                .tags(oldTask.getTags())
                .parameters(newParameters);
    }

    private TaskV2DomainTask getTaskCopy(TaskType newTaskType, TaskV2DomainTask oldTask) {
        return getTaskCopy(newTaskType, oldTask, oldTask.getParameters());
    }

    private static Map<String, String> filterParameters(Set<String> exclParams, TaskV2DomainTask oldTask) {
        return Optional.ofNullable(oldTask.getParameters()).orElse(emptyMap()).entrySet().stream()
                .filter(ent -> !exclParams.contains(ent.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
