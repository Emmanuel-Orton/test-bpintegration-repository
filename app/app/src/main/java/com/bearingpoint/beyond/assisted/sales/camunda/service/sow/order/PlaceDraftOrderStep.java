package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.order;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.sow.SowHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_DRAFT_ORDER;
import static com.bearingpoint.beyond.test-bpintegration.service.defs.TaskParameters.OPERATOR_NAME;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler.offsetDateTimeFormatter;


@Slf4j
@Component
public class PlaceDraftOrderStep extends AbstractBaseStep {


    private final SowHandler sowHandler;

    public PlaceDraftOrderStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil, SowHandler sowHandler) {
        super(objectMapper, workflowUtil);
        this.sowHandler = sowHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @SneakyThrows
    @Override
    public void executeStep(DelegateExecution execution) {

        String operatorName = getMandatoryVariable(execution, OPERATOR_NAME.name(), String.class);
        String tenant = getMandatoryVariable(execution, WF_TENANT, String.class);

        String draftOrderPlaced = getMandatoryVariable(execution, WF_DRAFT_ORDER_PLACED_DT, String.class);
        String draftOrderId = getMandatoryVariable(execution, WF_DRAFT_ORDER, String.class);

        sowHandler.placeOrderFromDraftOrder(draftOrderId, tenant, draftOrderPlaced, operatorName);

    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.RETAIL;
    }

}
