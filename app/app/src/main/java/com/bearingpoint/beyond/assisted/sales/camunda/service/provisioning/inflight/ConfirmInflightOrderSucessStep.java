package com.bearingpoint.beyond.test-bpintegration.camunda.service.provisioning.inflight;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_CURRENT_TENANT;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_PROVISIONING_ID;


@Slf4j
@Component
public class ConfirmInflightOrderSucessStep extends AbstractBaseStep {

    private final ProvisioningHandler provisioningHandler;

    public ConfirmInflightOrderSucessStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil, ProvisioningHandler provisioningHandler) {
        super(objectMapper, workflowUtil);
        this.provisioningHandler = provisioningHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {
        provisioningHandler.completeInflightOrder(
                getMandatoryVariable(execution, WF_WS_PROVISIONING_ID, String.class),
                getMandatoryVariable(execution, WF_CURRENT_TENANT, String.class));
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }

}