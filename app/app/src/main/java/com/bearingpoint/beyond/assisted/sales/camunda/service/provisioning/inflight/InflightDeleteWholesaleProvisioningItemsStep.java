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

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_REMOVED_ORDER_ITEMS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_ORDER;


@Slf4j
@Component
public class InflightDeleteWholesaleProvisioningItemsStep extends AbstractBaseStep {

    private final ProvisioningHandler provisioningHandler;

    public InflightDeleteWholesaleProvisioningItemsStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil, ProvisioningHandler provisioningHandler) {
        super(objectMapper, workflowUtil);
        this.provisioningHandler = provisioningHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {

        String removedItems = getMandatoryVariable(execution, WF_REMOVED_ORDER_ITEMS, String.class);

        if (StringUtils.isBlank(removedItems)) {
            log.debug("No order items were removed. Skipping InflightDeleteWholesaleProvisioningItemsStep.");
            return;
        }

        Set<String> removedIds = new HashSet<String>(Arrays.asList(removedItems.split(", ")));

        provisioningHandler.removeProvisioningItemsFromDatabase(
                getMandatoryVariable(execution, WF_WS_ORDER, String.class),
                removedIds
        );
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }

}