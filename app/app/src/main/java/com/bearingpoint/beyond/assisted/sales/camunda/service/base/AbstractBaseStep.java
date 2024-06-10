package com.bearingpoint.beyond.test-bpintegration.camunda.service.base;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_NAME;

@Slf4j
@Component
public abstract class AbstractBaseStep implements JavaDelegate {

    public static final String BPMN_ERROR = "BPMN_ERROR";
    public static final String WF_ERROR_MESSAGE = "WF_ERROR_MESSAGE";
    public static final String WF_ERROR_TEMPLATE = "WF_ERROR_TEMPLATE";
    public static final String WF_CURRENT_TASK = "WF_CURRENT_TASK";
    public static final String WF_PREVIOUS_TASK = "WF_PREVIOUS_TASK";
    public static final String WF_CANCELLED = "WF_CANCELLED";

    protected ObjectMapper objectMapper;
    protected WorkflowUtil workflowUtil;

    public AbstractBaseStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil) {
        this.objectMapper = objectMapper;
        this.workflowUtil = workflowUtil;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.debug("Executing step:{}", execution.getCurrentActivityId());

        if (execution.hasVariable(WF_CANCELLED)) {
            log.debug("Workflow was cancelled, step {} WON'T be executed.", execution.getCurrentActivityId());
            return;
        }

        if (hasRetry()) {
            executeStepWithRetry(execution);
        } else {
            executeStep(execution);
        }
    }

    public abstract void executeStep(DelegateExecution execution) throws Exception;

    public abstract boolean hasRetry();

    public void executeStepWithRetry(DelegateExecution execution) {
        try {
            executeStep(execution);
        } catch (Exception ex) {
            String errorMessage;
            try {
                errorMessage = objectMapper.writeValueAsString(ex);
            } catch (JsonProcessingException jpEx) {
                errorMessage = String.format("Workflow error: %s", ex.getMessage());
            }
            errorMessage = errorMessage.length() > 2000 ? errorMessage.substring(0, 2000) : errorMessage;
            execution.setVariable(WF_ERROR_MESSAGE, errorMessage);
            log.error("Raise Infonova Task for process instance:{} activity:{}\n", execution.getProcessInstanceId(), execution.getCurrentActivityId(), ex);

            workflowUtil.getWorkflowErrorsAndRetries().raiseWorkflowErrorTask(ex.toString(), ExceptionUtils.getStackTrace(ex), execution, getStepTenantType());
            throw new BpmnError(BPMN_ERROR, (String)execution.getVariable(WORKFLOW_NAME) + "_ERROR", ex);
        }
    }

    protected <T> T getMandatoryVariable(DelegateExecution execution, String name, Class<T> returnType) {
        if (!execution.hasVariable(name)) {
            throw new IllegalArgumentException("No context variable is found:" + name);
        }
        return returnType.cast(execution.getVariable(name));
    }

    protected <T> T getVariable(DelegateExecution execution, String name, Class<T> returnType) {
        if (!execution.hasVariable(name)) {
            return null;
        }
        return returnType.cast(execution.getVariable(name));
    }

    @SneakyThrows
    protected TaskInfo setCurrentTaskInfo(String taskId, String taskDefinition, String tenant, DelegateExecution execution) {
        final TaskInfo taskInfo = getTaskInfo(taskId, taskDefinition, tenant);

        execution.setVariable(WF_CURRENT_TASK, objectMapper.writeValueAsString(taskInfo));
        return taskInfo;
    }

    @SneakyThrows
    protected TaskInfo setPreviousTaskInfo(String taskId, String taskDefinition, String tenant, DelegateExecution execution) {
        final TaskInfo taskInfo = getTaskInfo(taskId, taskDefinition, tenant);

        execution.setVariable(WF_PREVIOUS_TASK, objectMapper.writeValueAsString(taskInfo));

        return taskInfo;
    }

    @SneakyThrows
    protected void setTaskInfo(String taskId, String taskDefinition, String tenant, DelegateExecution execution) {
        setTaskInfo(taskId, taskDefinition, tenant, execution, true);
    }

    @SneakyThrows
    protected TaskInfo setTaskInfo(String taskId, String taskDefinition, String tenant, DelegateExecution execution, boolean setPreviousTask) {
        final TaskInfo taskInfo = getTaskInfo(taskId, taskDefinition, tenant);

        execution.setVariable(WF_CURRENT_TASK, objectMapper.writeValueAsString(taskInfo));

        if (setPreviousTask) {
            execution.setVariable(WF_PREVIOUS_TASK, objectMapper.writeValueAsString(taskInfo));
        }
        return taskInfo;
    }

    private TaskInfo getTaskInfo(String taskId, String taskDefinition, String tenant) {
        return TaskInfo.builder()
                .taskDefinition(taskDefinition)
                .tenant(tenant)
                .taskId(taskId)
                .build();
    }

    public abstract TenantType getStepTenantType();
}
