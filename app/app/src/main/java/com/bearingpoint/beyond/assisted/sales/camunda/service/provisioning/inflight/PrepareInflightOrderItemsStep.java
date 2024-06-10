package com.bearingpoint.beyond.test-bpintegration.camunda.service.provisioning.inflight;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;


@Slf4j
@Component
public class PrepareInflightOrderItemsStep extends AbstractBaseStep {

    private final ProvisioningHandler provisioningHandler;

    public PrepareInflightOrderItemsStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil, ProvisioningHandler provisioningHandler) {
        super(objectMapper, workflowUtil);
        this.provisioningHandler = provisioningHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {

        String addedItems = getMandatoryVariable(execution, WF_ADDED_ORDER_ITEMS, String.class);

        if (StringUtils.isBlank(addedItems)) {
            log.debug("No order items were added. Skipping PrepareInflightOrderItemsStep.");
            return;
        }

        provisioningHandler.createInflightWholesaleProvisioningDatabaseEntries(
                getMandatoryVariable(execution, WF_RT_ORDER, String.class),
                getMandatoryVariable(execution, WF_RT_CUSTOMER, String.class),
                getMandatoryVariable(execution, WF_WS_ORDER, String.class),
                getMandatoryVariable(execution, WF_WS_CUSTOMER, String.class),
                getMandatoryVariable(execution, WF_WS_PROVISIONING_ID, String.class),
                getMandatoryVariable(execution, WF_CURRENT_TENANT, String.class),
                addedItems
        );

    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }

}