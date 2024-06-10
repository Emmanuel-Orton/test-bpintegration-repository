package com.bearingpoint.beyond.test-bpintegration.service.handler.sow;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowStatus;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowTypes;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.create.CreateSowCreateTaskStep;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
import static com.bearingpoint.beyond.test-bpintegration.service.defs.TaskParameters.OPERATOR_NAME;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler.offsetDateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SowHandler {

    private final WorkflowUtil workflowUtil;
    private final RuntimeService runtimeService;
    private final ProductOrdersService productOrdersService;

    private final Set<String> restartableSteps = Set.of("sowEventRestartOrderVerification", "sowEventRestartWsSowUpdate");

    public void startWorkflow(String draftOrder) {
        if (StringUtils.isBlank(draftOrder)) {
            throw new IllegalArgumentException(String.format("Error starting SoW workflow: Invalid draftOrder '%s'.", draftOrder));
        }
        ProcessInstance wfInstance = workflowUtil.getWorkflow(draftOrder);
        if (null != wfInstance) {
            log.warn("Workflow for draftOrder '{}' already exists. Restarting workflow.", draftOrder);
            runtimeService.removeVariable(wfInstance.getId(), CreateSowCreateTaskStep.TASK_TELUS_WS_SOW_CREATE_ID);
            runtimeService.createMessageCorrelation("SOW_Restart_workflow")
                    .processInstanceBusinessKey(draftOrder)
                    .setVariable(WF_TASK_RESOLUTION_ACTION, null)
                    .setVariable(WF_IS_RESTARTED, true)
                    .correlateWithResult();
            return;
        }
        workflowUtil.startSowWorkflow(draftOrder);
    }

    public WorkflowStatus getWorkflowStatus(String businessKey) {

        WorkflowStatus workflowStatus = workflowUtil.getWorkflowStatus(businessKey, WorkflowTypes.STATEMENT_OF_WORK);

        if (workflowStatus.isRunning()) {
            workflowStatus.setCanRestart(restartableSteps.contains(workflowStatus.getStepId()));
        }

        return workflowStatus;
    }

    public void placeOrderFromDraftOrder(String draftOrderId, String tenant, String draftOrderPlaced, String operatorName) {
        ProductorderingV1DomainShoppingCart cartFromDraft = productOrdersService.createCartFromDraft(
                tenant,
                draftOrderId);

        log.debug("Created shopping cart {} from draftOrderId {} and now submitting it.", cartFromDraft.getId(),
                cartFromDraft.getDraftId());


        log.debug("Patching shopping cart by adding parameter operator_name with value {}", operatorName);
        Map<String, String> parameters = cartFromDraft.getParameters();

        if (parameters==null) {
            parameters = new HashMap<>();
        }

        if (operatorName!=null) {
            parameters.put(OPERATOR_NAME.name().toLowerCase(), operatorName);
        }

        OffsetDateTime draftOrderPlacedDT;

        if (StringUtils.isBlank(draftOrderPlaced)) {
            draftOrderPlacedDT = OffsetDateTime.now();
            draftOrderPlaced = offsetDateTimeFormatter.format(draftOrderPlacedDT);
        } else {
            draftOrderPlacedDT = OffsetDateTime.parse(draftOrderPlaced);
        }

        parameters.put(WF_DRAFT_ORDER_PLACED_DT.toLowerCase(), draftOrderPlaced);

        ProductorderingV1DomainShoppingCart patchCart = new ProductorderingV1DomainShoppingCart();
        patchCart.setParameters(parameters);
        patchCart.setId(cartFromDraft.getId());

        cartFromDraft = productOrdersService.patchShoppingCart(tenant, patchCart);

        ProductorderingV1DomainProductOrder productorderingV1DomainProductOrder = productOrdersService.submitShoppingCartWithStartDate(tenant, cartFromDraft, draftOrderPlacedDT);

        log.info("Submitted cart with completionDate {}, got order id {}", draftOrderPlaced, productorderingV1DomainProductOrder.getId());

    }
}
