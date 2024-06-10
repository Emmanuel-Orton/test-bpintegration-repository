package com.bearingpoint.beyond.test-bpintegration.camunda.service.mo;

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

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskAttachmentTypes.CHANGE_ORDER_DAY_2_DRAFT;
import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskAttachmentTypes.CHANGE_ORDER_DAY_2_REVISED_DRAFT;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_CUSTOMER;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.mo.CreateWsMoReviewTaskStep.TASK_TELUS_WS_MO_REVIEW_ID;

@Slf4j
@Component
public class CreateWsMoUpdateTaskStep extends AbstractBaseStep {

    private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;
    private final TaskAttachmentsHandler taskAttachmentsHandler;

    public CreateWsMoUpdateTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
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
    public void executeStep(DelegateExecution execution) throws Exception {

        TaskInfo previousTask = workflowUtil.getTaskInfo(getMandatoryVariable(execution, WF_PREVIOUS_TASK, String.class));
        TaskInfo previousTaskNote = workflowUtil.getTaskInfo(getMandatoryVariable(execution, WF_PREVIOUS_TASK, String.class));
        TaskInfo currentTask = workflowUtil.getTaskInfo(getVariable(execution, WF_CURRENT_TASK, String.class));
        String wfTenantWs = getMandatoryVariable(execution, WF_TENANT_WS, String.class);

        if (TaskType.TELUS_RT_CO_DAY2_CREATE.name().equals(previousTask.getTaskDefinition())) {
            String reviewId = getMandatoryVariable(execution, TASK_TELUS_WS_MO_REVIEW_ID, String.class);
            previousTask = TaskInfo.builder()
                    .tenant(wfTenantWs)
                    .taskId(reviewId)
                    .taskDefinition(TaskType.TELUS_WS_CO_DAY2_REVIEW.name())
                    .build();
        }

        TaskV2DomainTask task;
        if (currentTask == null) {
            task = basedOnTheOtherTaskHandler.createNewTaskFromOldOneWithClosingNote(
                    previousTask.getTaskId(),
                    previousTask.getTenant(),
                    previousTaskNote,
                    TaskType.TELUS_WS_CO_DAY2_UPDATE,
                    false,
                    Set.of("isValid")
            );

            Long wsCustomerId = getMandatoryVariable(execution, WF_WS_CUSTOMER, Long.class);

            task.setBillingAccount(wsCustomerId.toString());

            task = basedOnTheOtherTaskHandler.createTask(task, TaskType.TELUS_WS_CO_DAY2_UPDATE);
            
            currentTask = setCurrentTaskInfo(task.getId(), task.getTaskDefinition(), wfTenantWs, execution);
            log.info("Created task {}", currentTask);
        }

        taskAttachmentsHandler.copyFirstAvailableAttachment(previousTask.getTenant(),
                previousTask.getTaskId(),
                currentTask.getTaskId(),
                previousTask.getTaskDefinition(),
                Map.of(TaskType.TELUS_WS_CO_DAY2_REVIEW, List.of(CHANGE_ORDER_DAY_2_REVISED_DRAFT, CHANGE_ORDER_DAY_2_DRAFT),
                        TaskType.TELUS_WS_CO_DAY2_FINALIZE, List.of(CHANGE_ORDER_DAY_2_REVISED_DRAFT, CHANGE_ORDER_DAY_2_DRAFT)
                )
        );

        setTaskInfo(currentTask.getTaskId(), currentTask.getTaskDefinition(), wfTenantWs, execution, true);
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }
}