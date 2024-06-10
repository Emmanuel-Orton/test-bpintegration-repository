package com.bearingpoint.beyond.test-bpintegration.camunda.service.mo;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskAttachmentTypes;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.mo.ModifyOrderHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.TaskAttachmentsHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_CUSTOMER;


@Slf4j
@Component
public class CreateWsMoReviewTaskStep extends AbstractBaseStep {
    public static final String TASK_TELUS_WS_MO_REVIEW_ID = "TASK_TELUS_WS_MO_REVIEW_ID";
    private final ModifyOrderHandler modifyOrderHandler;
    private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;
    private final TaskAttachmentsHandler taskAttachmentsHandler;

    public CreateWsMoReviewTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
                                    ModifyOrderHandler modifyOrderHandler,
                                    BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler,
                                    TaskAttachmentsHandler taskAttachmentsHandler) {
        super(objectMapper, workflowUtil);
        this.modifyOrderHandler = modifyOrderHandler;
        this.basedOnTheOtherTaskHandler = basedOnTheOtherTaskHandler;
        this.taskAttachmentsHandler = taskAttachmentsHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws IOException {

        String tenantWs = getMandatoryVariable(execution, WF_TENANT_WS, String.class);
        TaskInfo previousTask = workflowUtil.getTaskInfo(getVariable(execution, WF_PREVIOUS_TASK, String.class));
        TaskInfo currentTask = workflowUtil.getTaskInfo(getVariable(execution, WF_CURRENT_TASK, String.class));

        TaskV2DomainTask task;
        if (currentTask == null) {
            task = modifyOrderHandler.createReviewTask(tenantWs,
                    getMandatoryVariable(execution, WF_WS_CUSTOMER, Long.class),
                    previousTask.getTaskId());

            currentTask = setCurrentTaskInfo(task.getId(), task.getTaskDefinition(), tenantWs, execution);
            log.info("Created task {}", currentTask);
        }

        taskAttachmentsHandler.copyFirstAvailableAttachment(previousTask.getTenant(),
                previousTask.getTaskId(),
                currentTask.getTaskId(),
                previousTask.getTaskDefinition(),
                Map.of(TaskType.TELUS_WS_CO_DAY2_INITIATE, List.of(TaskAttachmentTypes.CHANGE_ORDER_DAY_2_DRAFT),
                        TaskType.TELUS_WS_CO_DAY2_UPDATE, List.of(TaskAttachmentTypes.CHANGE_ORDER_DAY_2_REVISED_DRAFT))
        );

        setTaskInfo(currentTask.getTaskId(), currentTask.getTaskDefinition(), tenantWs, execution, true);
        execution.setVariable(TASK_TELUS_WS_MO_REVIEW_ID, currentTask.getTaskId());

    }


    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }
}
