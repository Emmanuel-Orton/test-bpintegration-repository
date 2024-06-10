package com.bearingpoint.beyond.test-bpintegration.camunda.service.provisioning;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_ORDER;

@Slf4j
@Component
public class CreateWholesaleProvisioningTasks extends AbstractBaseStep {

    private final ProvisioningHandler provisioningHandler;

    public CreateWholesaleProvisioningTasks(ObjectMapper objectMapper, WorkflowUtil workflowUtil, ProvisioningHandler provisioningHandler) {
        super(objectMapper, workflowUtil);
        this.provisioningHandler = provisioningHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {
        String wholesaleId = getMandatoryVariable(execution, WF_WS_ORDER, String.class);
        provisioningHandler.raiseOrderServiceProvisioningTask(Long.parseLong(wholesaleId));
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }

}