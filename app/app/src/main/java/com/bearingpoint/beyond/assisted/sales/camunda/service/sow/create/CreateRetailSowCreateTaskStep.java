package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.create;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.sow.RetailSowCreationTaskHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_DRAFT_ORDER;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.review.CreateSowReviewTaskStep.WS_SOW_REVIEW_TASK_ID;

@Slf4j
@Component
public class CreateRetailSowCreateTaskStep extends AbstractBaseStep {

    private final RetailSowCreationTaskHandler retailSowCreationTaskHandler;

    public CreateRetailSowCreateTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
                                         RetailSowCreationTaskHandler retailSowCreationTaskHandler) {
        super(objectMapper, workflowUtil);
        this.retailSowCreationTaskHandler = retailSowCreationTaskHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws IOException {
        final String wfTenant = getMandatoryVariable(execution, WF_TENANT, String.class);
        final String wsTenant = getMandatoryVariable(execution, WF_TENANT_WS, String.class);

        TaskV2DomainTask task = retailSowCreationTaskHandler.createRetailSowCreationTask(
                wfTenant,
                wsTenant,
                getMandatoryVariable(execution, WF_DRAFT_ORDER, String.class),
                getMandatoryVariable(execution, WS_SOW_REVIEW_TASK_ID, String.class)
        );

        setTaskInfo(task.getId(), task.getTaskDefinition(), wfTenant, execution);
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.RETAIL;
    }
}
