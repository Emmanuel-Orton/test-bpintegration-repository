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
import com.bearingpoint.beyond.test-bpintegration.service.handler.co.ChangeOrderHandler;
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

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.mo.CreateWsMoReviewTaskStep.TASK_TELUS_WS_MO_REVIEW_ID;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.mo.ModifyOrderHandler.TASK_TELUS_RT_MO_CREATION_ID;


@Slf4j
@Component
public class CreateRetailMoCreateTaskStep extends AbstractBaseStep {

    private final ModifyOrderHandler modifyOrderHandler;
    private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;
    private final TaskAttachmentsHandler taskAttachmentsHandler;

    public CreateRetailMoCreateTaskStep(ObjectMapper objectMapper,
                                        WorkflowUtil workflowUtil,
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

        String tenantRt = getMandatoryVariable(execution, WF_TENANT, String.class);
        TaskInfo previousTask = workflowUtil.getTaskInfo(getVariable(execution, WF_PREVIOUS_TASK, String.class));
        String reviewTaskId = getMandatoryVariable(execution, TASK_TELUS_WS_MO_REVIEW_ID, String.class);
        Long retailCustomerId = getMandatoryVariable(execution, WF_RT_CUSTOMER, Long.class);

        TaskV2DomainTask task = basedOnTheOtherTaskHandler.createNewTaskFromOldOne(
                    previousTask.getTenant(),
                    previousTask.getTaskId(),
                    TaskType.TELUS_RT_CO_DAY2_CREATE,
                    false,
                    Set.of("isValid")
            );
        task.setBillingAccount(retailCustomerId.toString());

        task.getParameters().put("REVIEW_TASK_ID", reviewTaskId);
        task = basedOnTheOtherTaskHandler.createTask(task, TaskType.TELUS_RT_CO_DAY2_CREATE);

        log.info("Created task {}", task);

        setTaskInfo(task.getId(), task.getTaskDefinition(), tenantRt, execution, true);

    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.RETAIL;
    }

}
