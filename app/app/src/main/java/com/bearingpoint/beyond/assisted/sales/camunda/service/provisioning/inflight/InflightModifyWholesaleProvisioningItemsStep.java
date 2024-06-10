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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;


@Slf4j
@Component
public class InflightModifyWholesaleProvisioningItemsStep extends AbstractBaseStep {

    private final ProvisioningHandler provisioningHandler;

    public InflightModifyWholesaleProvisioningItemsStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil, ProvisioningHandler provisioningHandler) {
        super(objectMapper, workflowUtil);
        this.provisioningHandler = provisioningHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {

        String modifyItems = getVariable(execution, WF_MODIFIED_ORDER_ITEMS, String.class);

        if (StringUtils.isBlank(modifyItems)) {
            log.debug("No order items were modified. Skipping InflightModifyWholesaleProvisioningItemsStep.");
            return;
        }

        Set<String> modifyIds = new HashSet<String>(Arrays.asList(modifyItems.split(", ")));

        provisioningHandler.inflightModifyWholesaleProvisioningItems(
                getMandatoryVariable(execution, WF_WS_ORDER, String.class),
                modifyIds
        );

    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }

}