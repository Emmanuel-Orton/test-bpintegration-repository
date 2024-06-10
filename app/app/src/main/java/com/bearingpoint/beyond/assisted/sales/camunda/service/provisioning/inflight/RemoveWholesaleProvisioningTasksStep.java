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
@Deprecated
public class RemoveWholesaleProvisioningTasksStep extends AbstractBaseStep {

    // IMPORTANT: since we still might have running workflows, please leave this step in until end of 2023.

    private final ProvisioningHandler provisioningHandler;

    public RemoveWholesaleProvisioningTasksStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil, ProvisioningHandler provisioningHandler) {
        super(objectMapper, workflowUtil);
        this.provisioningHandler = provisioningHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {
        log.error("RemoveWholesaleProvisioningTasksStep NOT IMPLEMENTED.");
        // TODO test after SAAS-5697
        String removedItems = getMandatoryVariable(execution, WF_REMOVED_ORDER_ITEMS, String.class);

        if (StringUtils.isBlank(removedItems)) {
            log.debug("No order items were removed. Skipping step.");
            return;
        }

        Set<String> removedIds = new HashSet<String>(Arrays.asList(removedItems.split(", ")));

//        provisioningHandler.removeWholesaleProvisioningTasks(
//                getMandatoryVariable(execution, WF_WS_ORDER, String.class),
//                removedIds
//        );

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