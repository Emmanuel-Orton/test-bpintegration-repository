package com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.main;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DafOrderItem;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;


@Slf4j
@Component
public class PrepareServiceOrderItemsStep extends AbstractBaseStep {

    private final DafHandler dafHandler;

    public PrepareServiceOrderItemsStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil, DafHandler dafHandler) {
        super(objectMapper, workflowUtil);
        this.dafHandler = dafHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {

        String inputData = getMandatoryVariable(execution, WF_WS_PROVISIONING_ITEMS_REQUEST_DATA, String.class);
        List<DafOrderItem> dafOrderItems = objectMapper.readerForListOf(DafOrderItem.class).readValue(inputData);

        dafHandler.prepareServiceOrderItems(
                getMandatoryVariable(execution, WORKFLOW_ID, String.class),
                dafOrderItems
        );
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }
}