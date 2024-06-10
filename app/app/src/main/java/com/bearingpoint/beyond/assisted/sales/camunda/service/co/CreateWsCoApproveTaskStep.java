package com.bearingpoint.beyond.test-bpintegration.camunda.service.co;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.co.CreateWsCoReviewTaskStep.TASK_TELUS_WS_CO_REVIEW_ID;


@Slf4j
@Component
public class CreateWsCoApproveTaskStep extends AbstractBaseStep {

    private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;

    public CreateWsCoApproveTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil, BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler) {
        super(objectMapper, workflowUtil);
        this.basedOnTheOtherTaskHandler = basedOnTheOtherTaskHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {
        String tenant = getMandatoryVariable(execution, WF_TENANT_WS, String.class);
        String reviewTaskId = getVariable(execution, TASK_TELUS_WS_CO_REVIEW_ID, String.class);

        TaskV2DomainTask task = basedOnTheOtherTaskHandler.createNewTaskFromOldOne(
                tenant,
                reviewTaskId,
                TaskType.TELUS_WS_CO_PARTNER_APPROVE
        );

        if (task != null) {
            setTaskInfo(task.getId(), task.getTaskDefinition(), tenant, execution, true);
        }
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }
}
