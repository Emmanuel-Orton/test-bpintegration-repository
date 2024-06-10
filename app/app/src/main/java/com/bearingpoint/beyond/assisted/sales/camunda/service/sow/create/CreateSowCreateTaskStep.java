package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.create;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.sow.OrderSummaryHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.sow.SowCreationTaskHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_DRAFT_ORDER;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;


@Slf4j
@Component
public class CreateSowCreateTaskStep extends AbstractBaseStep {

    public static final String TASK_TELUS_WS_SOW_CREATE_ID = "TASK_TELUS_WS_SOW_CREATE_ID";
    private final SowCreationTaskHandler sowCreationTaskHandler;
    private final OrderSummaryHandler orderSummaryHandler;

    public CreateSowCreateTaskStep(ObjectMapper objectMapper,
                                   WorkflowUtil workflowUtil,
                                   SowCreationTaskHandler sowCreationTaskHandler,
                                   OrderSummaryHandler orderSummaryHandler) {
        super(objectMapper, workflowUtil);
        this.sowCreationTaskHandler = sowCreationTaskHandler;
        this.orderSummaryHandler = orderSummaryHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws IOException {
        final String tenant = getMandatoryVariable(execution, WF_TENANT, String.class);
        final String wsTenant = getMandatoryVariable(execution, WF_TENANT_WS, String.class);

        final String draftOrderId = getMandatoryVariable(execution, WF_DRAFT_ORDER, String.class);

        if (!execution.hasVariable(TASK_TELUS_WS_SOW_CREATE_ID)) {
            TaskV2DomainTask task = sowCreationTaskHandler.createSowCreationTask(tenant, wsTenant, draftOrderId);

            final String taskId = task.getId();
            execution.setVariable(TASK_TELUS_WS_SOW_CREATE_ID, taskId);
            setTaskInfo(taskId, task.getTaskDefinition(), wsTenant,
                    execution);
        }

        orderSummaryHandler.attachOrderSummaryToTask(tenant, (String) execution.getVariable(TASK_TELUS_WS_SOW_CREATE_ID),
                draftOrderId);
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }
}
