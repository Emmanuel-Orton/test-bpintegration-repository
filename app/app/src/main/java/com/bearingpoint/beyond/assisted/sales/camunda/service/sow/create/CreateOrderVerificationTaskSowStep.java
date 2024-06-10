package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.create;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.sow.OrderVerificationTaskEventsHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_DRAFT_ORDER;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_IS_RESTARTED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.service.defs.TaskParameters.OPERATOR_NAME;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.sow.OrderVerificationTaskEventsHandler.SALES_USERNAME_PARAMETER;


@Slf4j
@Component
public class CreateOrderVerificationTaskSowStep extends AbstractBaseStep {

    public CreateOrderVerificationTaskSowStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil, OrderVerificationTaskEventsHandler orderVerificationTaskEventsHandler) {
        super(objectMapper, workflowUtil);
        this.orderVerificationTaskEventsHandler = orderVerificationTaskEventsHandler;
    }

    private final OrderVerificationTaskEventsHandler orderVerificationTaskEventsHandler;

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) {
        TaskV2DomainTask task = orderVerificationTaskEventsHandler.createOrderVerificationTask(
                getMandatoryVariable(execution, WF_TENANT, String.class),
                getMandatoryVariable(execution, WF_TENANT_WS, String.class),
                getMandatoryVariable(execution, WF_DRAFT_ORDER, String.class),
                getVariable(execution, WF_IS_RESTARTED, Boolean.class)
        );
        setTaskInfo(task.getId(), task.getTaskDefinition(), getMandatoryVariable(execution, WF_TENANT, String.class), execution);

        String operator = task.getParameters().get(SALES_USERNAME_PARAMETER);
        execution.setVariable(OPERATOR_NAME.name(), operator);
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.RETAIL;
    }
}
