package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.create;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.review.CreateSowReviewTaskStep;
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
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.review.CreateSowReviewTaskStep.WS_SOW_REVIEW_TASK_ID;


@Slf4j
@Component
public class CreateSowUpdateTaskStep extends AbstractBaseStep {

    private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;
    private final TaskAttachmentsHandler taskAttachmentsHandler;

    public CreateSowUpdateTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
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
        TaskInfo previousTaskNote = workflowUtil.getTaskInfo(getMandatoryVariable(execution, WF_PREVIOUS_TASK, String.class));
        TaskInfo currentTask = workflowUtil.getTaskInfo(getVariable(execution, WF_CURRENT_TASK, String.class));
        final String wfTenantWs = getMandatoryVariable(execution, WF_TENANT_WS, String.class);

        if (TaskType.TELUS_RT_SOW_CREATE.name().equals(previousTask.getTaskDefinition())) {
            String reviewId = getMandatoryVariable(execution, WS_SOW_REVIEW_TASK_ID, String.class);
            previousTask = TaskInfo.builder()
                    .tenant(wfTenantWs)
                    .taskId(reviewId)
                    .taskDefinition(TaskType.TELUS_WS_SOW_REVIEW.name())
                    .build();
        }

        TaskV2DomainTask task;
        if (currentTask == null) {
            task = basedOnTheOtherTaskHandler.createNewTaskFromOldOneWithClosingNote(
                    previousTask.getTaskId(),
                    wfTenantWs,
                    previousTaskNote,
                    TaskType.TELUS_WS_SOW_UPDATE
            );

            setCurrentTaskInfo(task.getId(), task.getTaskDefinition(), wfTenantWs, execution);
        }

        currentTask = workflowUtil.getTaskInfo(getMandatoryVariable(execution, WF_CURRENT_TASK, String.class));

        taskAttachmentsHandler.copyFirstAvailableAttachment(previousTask.getTenant(),
                wfTenantWs,
                previousTask.getTaskId(),
                currentTask.getTaskId(),
                previousTask.getTaskDefinition(),
                Map.of(TaskType.TELUS_WS_SOW_REVIEW, TaskAttachmentsHandler.SOW_DEFAULT_ATTACHMENTS_TO_COPY,
                        TaskType.TELUS_WS_SOW_APPROVE, TaskAttachmentsHandler.SOW_DEFAULT_ATTACHMENTS_TO_COPY)
        );

        setTaskInfo(currentTask.getTaskId(), currentTask.getTaskDefinition(), wfTenantWs, execution);
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }
}
