package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.approve;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.TaskAttachmentsHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.create.CreateSowCreateTaskStep.TASK_TELUS_WS_SOW_CREATE_ID;


@Slf4j
@Component
public class CreateSowApproveTaskStep extends AbstractBaseStep {

    private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;
    private final TaskAttachmentsHandler taskAttachmentsHandler;

    public CreateSowApproveTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
                                    BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler,
                                    TaskAttachmentsHandler taskAttachmentsHandler) {
        super(objectMapper, workflowUtil);
        this.basedOnTheOtherTaskHandler = basedOnTheOtherTaskHandler;
        this.taskAttachmentsHandler = taskAttachmentsHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws IOException {
        TaskInfo previousTask = workflowUtil.getTaskInfo(getMandatoryVariable(execution, WF_PREVIOUS_TASK, String.class));
        TaskInfo currentTask = workflowUtil.getTaskInfo(getVariable(execution, WF_CURRENT_TASK, String.class));
        final String wfTenantWs = getMandatoryVariable(execution, WF_TENANT_WS, String.class);

        TaskV2DomainTask task;
        if (currentTask == null) {
            task = basedOnTheOtherTaskHandler.createNewTaskFromOldOne(
                    wfTenantWs,
                    getMandatoryVariable(execution, TASK_TELUS_WS_SOW_CREATE_ID, String.class),
                    TaskType.TELUS_WS_SOW_APPROVE
            );

            setCurrentTaskInfo(task.getId(), task.getTaskDefinition(), wfTenantWs, execution);
        }

        currentTask = workflowUtil.getTaskInfo(getMandatoryVariable(execution, WF_CURRENT_TASK, String.class));

        taskAttachmentsHandler.copyFirstAvailableAttachment(previousTask.getTenant(),
                wfTenantWs,
                previousTask.getTaskId(),
                currentTask.getTaskId(),
                previousTask.getTaskDefinition(),
                Map.of(TaskType.TELUS_RT_SOW_CREATE, TaskAttachmentsHandler.SOW_DEFAULT_ATTACHMENTS_TO_COPY)
        );

        setTaskInfo(currentTask.getTaskId(), currentTask.getTaskDefinition(), wfTenantWs, execution);
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }
}
