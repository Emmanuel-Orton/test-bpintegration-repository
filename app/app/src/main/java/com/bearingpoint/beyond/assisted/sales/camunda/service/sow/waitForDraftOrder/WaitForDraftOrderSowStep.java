package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.waitForDraftOrder;


import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftOrder;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler.offsetDateTimeFormatter;


@Slf4j
@Component
public class WaitForDraftOrderSowStep extends AbstractBaseStep {

    private final ProductOrdersService productOrdersService;

    public WaitForDraftOrderSowStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
                                    ProductOrdersService productOrdersService) {
        super(objectMapper, workflowUtil);
        this.productOrdersService = productOrdersService;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.RETAIL;
    }

    @Override
    public void executeStep(DelegateExecution execution) {
        final String draftOrderId = getMandatoryVariable(execution, WF_DRAFT_ORDER, String.class);
        AtomicReference<ProductorderingV1DomainDraftOrder> draftOrder = new AtomicReference<>(null);

        try {
            Awaitility.await().pollInterval(10, TimeUnit.SECONDS)
                    .ignoreException(IllegalArgumentException.class)
                    .atMost(5, TimeUnit.MINUTES)
                    .until(() -> {
                        draftOrder.set(productOrdersService.getDraftOrder(getMandatoryVariable(execution, WF_TENANT, String.class),
                                draftOrderId));
                        return draftOrder.get() != null;
                    });
        } catch (ConditionTimeoutException e) {
            throw new IllegalArgumentException(String.format("Draft order %s not found", draftOrderId));
        }

        execution.setVariable(WF_DRAFT_ORDER_PLACED_DT, offsetDateTimeFormatter.format(OffsetDateTime.now()));
    }
}
