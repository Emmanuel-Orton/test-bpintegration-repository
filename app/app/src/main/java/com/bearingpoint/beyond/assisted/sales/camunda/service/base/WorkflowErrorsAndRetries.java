package com.bearingpoint.beyond.test-bpintegration.camunda.service.base;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.api.TasksV2Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEvent;
import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.Execution;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep.WF_ERROR_TEMPLATE;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowErrorsAndRetries {

    public static final String ERROR_MSG = "ERROR_MSG";
    public static final String ERROR_STACK_TRACE = "ERROR_STACK_TRACE";
    public static final String CAMUNDA_PROC_INST_ID = "CAMUNDA_PROC_INST_ID";
    public static final String CAMUNDA_ACT_ID = "CAMUNDA_ACT_ID";

    private final TasksV2Api tasksV2Api;
    private final ObjectMapper objectMapper;
    private final RuntimeService runtimeService;
    private final InfonovaLinkService infonovaLinkService;


    public void raiseWorkflowErrorTask(String errorMsg, String stackTrace, DelegateExecution execution, TenantType stepTenantType) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(CAMUNDA_PROC_INST_ID, execution.getProcessInstanceId());
        parameters.put(CAMUNDA_ACT_ID, execution.getCurrentActivityId());
        parameters.put(ERROR_MSG, errorMsg);
        parameters.put(WORKFLOW_NAME_SHORT, (String)execution.getVariable(WORKFLOW_NAME_SHORT));
        parameters.put(WORKFLOW_NAME, (String)execution.getVariable(WORKFLOW_NAME));

        List<String> stackTraceList = splitString(stackTrace);
        for(int i=0; i<4; i++) {
            int index = i+1;
            if (index > stackTraceList.size()) {
                parameters.put(ERROR_STACK_TRACE + "_" + index, "");
            } else {
                parameters.put(ERROR_STACK_TRACE + "_" + index, stackTraceList.get(i));
            }
        }

        raiseTask(infonovaLinkService.getCorrectTenant(stepTenantType), parameters, (String) execution.getVariable(WF_ERROR_TEMPLATE));
    }

    public List<String> splitString(String stackTrace) {
        if (stackTrace!=null) {
            return Splitter.fixedLength(3800).splitToList(stackTrace);
        } else {
            return List.of("No stacktrace found.");
        }
    }

    private void raiseTask(String tenant, Map<String,String> parameters, String taskDefinition){
        TaskV2DomainTask body = new TaskV2DomainTask()
                .parameters(parameters)
                .taskDefinition(taskDefinition);
        try {
            log.debug("Raising task: {}", objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException e) {
            log.debug("Json serialization failed, calling toString: Raising task: {}", body.toString());
        }

        TaskV2DomainTask task = tasksV2Api.postTasksV2Tasks(tenant, body, UUID.randomUUID().toString()).block();
        log.debug("Workflow Error task raised with id: {} on tenant: {}", task.getId(), tenant);
    }


    public void retry(TaskV2DomainTaskEvent taskResolvedEvent) {

        Map<String, String> taskParams = taskResolvedEvent.getEvent().getCurrent().getParameters();
        String processInstanceId = taskParams.get(CAMUNDA_PROC_INST_ID);
        String activityId = taskParams.get(CAMUNDA_ACT_ID);
        Execution execution = runtimeService.createExecutionQuery()
                .processInstanceId(processInstanceId).activityId(activityId + "_retry").singleResult();
        if (null == execution || null == execution.getId()) {
            throw new IllegalArgumentException("No execution has found for process " + processInstanceId + " and activity " + activityId);
        }
        runtimeService.signal(execution.getId());
    }

}
