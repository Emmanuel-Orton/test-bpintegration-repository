package com.bearingpoint.beyond.test-bpintegration.service.handler.sow;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskAttachmentTypes;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1SummaryDomainCustomerOrderSummary;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.TemplateDataFilteringService;
import com.bearingpoint.beyond.test-bpintegration.service.TemplateRenderingService;
import com.bearingpoint.beyond.test-bpintegration.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderSummaryHandler {
    public static final String ORDER_SUMMARY_TEMPLATE_NAME = "ORDER_DEFAULT";
    public static final String PRE_SOW_ATTACHMENT_NAME_TEMPLATE = "Pre-SOW Order Summary_%s.pdf";

    private final ProductOrdersService productOrdersService;
    private final UserService userService;
    private final TemplateRenderingService templateRenderingService;
    private final TasksService tasksService;
    private final TemplateDataFilteringService templateDataFilteringService;

    @Value("${tenant_ws_name}")
    private String wsTenant;


    public void attachOrderSummaryToTask(String tenant, String taskId, String draftOrderId) throws IOException {
        final String shoppingCartId = getShoppingCartIdWithOperatorEmail(draftOrderId, tenant);
        final ProductorderingV1SummaryDomainCustomerOrderSummary orderSummary = productOrdersService
                .getOrderSummary(shoppingCartId, tenant);
        templateDataFilteringService.filterOrderSummaryServiceCharacteristics(orderSummary,
                draftOrderId,
                Set.of("RT_CUSTOMER", "SALES_AGENT_USERNAME", "SALES_AGENT_NAME", "SALES_AGENT_EMAIL", "OPI_NUMBER"));
        final byte[] pdf = templateRenderingService.renderPdf(tenant, ORDER_SUMMARY_TEMPLATE_NAME, orderSummary);
        tasksService.addAttachmentToTask(wsTenant, taskId, TaskAttachmentTypes.PRE_SOW_ORDER_SUMMARY.name(),
                getPreSowAttachmentName(draftOrderId), pdf, null);
    }

    private String getShoppingCartIdWithOperatorEmail(String draftOrderId, String tenant) {
        final ProductorderingV1DomainDraftOrder draftOrder = productOrdersService.getDraftOrder(tenant, draftOrderId);
        final String operatorName = draftOrder.getSavedBy();
        final String operatorEmail = userService.retrieveUserEmailByUserName(tenant, operatorName);
        final ProductorderingV1DomainShoppingCart shoppingCart = productOrdersService.createCartFromDraft(tenant, draftOrderId);

        final ProductorderingV1DomainShoppingCart patchedShoppingCart = productOrdersService.patchShoppingCart(tenant,
                shoppingCart.getId(), new ProductorderingV1DomainShoppingCart()
                        .addProductsItem(new ProductorderingV1DomainProduct()
                                .id(shoppingCart.getProducts().get(0).getId())
                                .putParametersItem("operator_email", operatorEmail)
                                .putParametersItem("operator_name", operatorName)));

        return patchedShoppingCart.getId();
    }

    private String getPreSowAttachmentName(String draftOrderId) {
        return String.format(PRE_SOW_ATTACHMENT_NAME_TEMPLATE, draftOrderId);
    }
}
