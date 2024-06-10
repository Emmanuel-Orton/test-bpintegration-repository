package com.bearingpoint.beyond.test-bpintegration.camunda.service.mo;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskAttachmentTypes;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.TaskAttachmentsHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.mo.ModifyOrderHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskAttachmentTypes.CHANGE_ORDER_DAY_2_DRAFT;
import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskAttachmentTypes.CHANGE_ORDER_DAY_2_REVISED_DRAFT;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_SERVICE_INSTANCE_NAME;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.mo.CreateWsMoReviewTaskStep.TASK_TELUS_WS_MO_REVIEW_ID;


@Slf4j
@Component
public class CreateWsMoFinalizeTaskStep extends AbstractBaseStep {
    private final TaskAttachmentsHandler taskAttachmentsHandler;
    private final ModifyOrderHandler modifyOrderHandler;

    public CreateWsMoFinalizeTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
                                      ModifyOrderHandler modifyOrderHandler,
                                      TaskAttachmentsHandler taskAttachmentsHandler) {
        super(objectMapper, workflowUtil);
        this.modifyOrderHandler = modifyOrderHandler;
        this.taskAttachmentsHandler = taskAttachmentsHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws IOException {

        String tenantWs = getMandatoryVariable(execution, WF_TENANT_WS, String.class);
        TaskInfo currentTask = workflowUtil.getTaskInfo(getVariable(execution, WF_CURRENT_TASK, String.class));
        String reviewTaskId = getMandatoryVariable(execution, TASK_TELUS_WS_MO_REVIEW_ID, String.class);

        if (currentTask == null) {
            TaskV2DomainTask task = modifyOrderHandler.createFinalizeTask(tenantWs,
                    reviewTaskId,
                    getMandatoryVariable(execution, WF_SERVICE_INSTANCE_NAME, String.class));

            currentTask = setCurrentTaskInfo(task.getId(), task.getTaskDefinition(), tenantWs, execution);
            log.info("Created task {}", currentTask);
        }

        taskAttachmentsHandler.copyFirstAvailableAttachment(tenantWs,
                reviewTaskId,
                currentTask.getTaskId(),
                TaskType.TELUS_WS_CO_DAY2_REVIEW.name(),
                Map.of(TaskType.TELUS_WS_CO_DAY2_REVIEW, List.of(CHANGE_ORDER_DAY_2_REVISED_DRAFT, CHANGE_ORDER_DAY_2_DRAFT))
        );

        setTaskInfo(currentTask.getTaskId(), currentTask.getTaskDefinition(), tenantWs, execution, true);

    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }
}
