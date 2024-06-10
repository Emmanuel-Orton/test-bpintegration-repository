package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.review;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskAttachmentTypes;
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
import java.util.List;
import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;

@Slf4j
@Component
public class CreateSowReviewTaskStep extends AbstractBaseStep {

    public static final String WS_SOW_REVIEW_TASK_ID = "WS_SOW_REVIEW_TASK_ID";
    private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;
    private final TaskAttachmentsHandler taskAttachmentsHandler;

    public CreateSowReviewTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
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
                    previousTask.getTenant(),
                    previousTask.getTaskId(),
                    TaskType.TELUS_WS_SOW_REVIEW
            );

            setCurrentTaskInfo(task.getId(), task.getTaskDefinition(), wfTenantWs, execution);
        }

        currentTask = workflowUtil.getTaskInfo(getMandatoryVariable(execution, WF_CURRENT_TASK, String.class));

        taskAttachmentsHandler.copyFirstAvailableAttachment(previousTask.getTenant(),
                previousTask.getTaskId(),
                currentTask.getTaskId(),
                previousTask.getTaskDefinition(),
                Map.of(TaskType.TELUS_WS_SOW_CREATE, List.of(TaskAttachmentTypes.STATEMENT_OF_WORK_DRAFT),
                        TaskType.TELUS_WS_SOW_UPDATE, List.of(TaskAttachmentTypes.STATEMENT_OF_WORK_REVISED_DRAFT))
        );

        execution.setVariable(WS_SOW_REVIEW_TASK_ID, currentTask.getTaskId());
        setTaskInfo(currentTask.getTaskId(), currentTask.getTaskDefinition(), wfTenantWs, execution);
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }
}
