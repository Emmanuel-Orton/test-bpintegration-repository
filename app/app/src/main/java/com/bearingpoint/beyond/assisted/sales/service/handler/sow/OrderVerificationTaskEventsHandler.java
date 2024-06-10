package com.bearingpoint.beyond.test-bpintegration.service.handler.sow;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainServiceCharacteristic;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.*;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.SalesAgentHandler;
import com.bearingpoint.beyond.test-bpintegration.service.tag.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderVerificationTaskEventsHandler {

    public static final String ORDER_VERIFICATION_TASK_DEFINITION = "TELUS_RT_ORDER_VERIFICATION";
    public static final String PRODUCT_NAME_PARAMETER = "PRODUCT_NAME";
    public static final String SALES_USERNAME_PARAMETER = "SALES_USERNAME";
    public static final String DRAFT_ORDER_ID = "DRAFT_ORDER_ID";
    public static final String RT_BILLING_ACCOUNT_ID_PARAMETER = "RT_BILLING_ACCOUNT_ID";
    private static final String DRAFT_ORDER_LINK = "DRAFT_ORDER_LINK";
    public static final String OPI_NUMBER = "opi_num";
    public static final String CUSTOMER_OPI_NUMBER = "CUSTOMER_OPI_NUMBER";
    public static final String CUSTOMER_BUSINESS_NAME = "CUSTOMER_BUSINESS_NAME";
    public static final String IS_FLOW_RESTARTED = "IS_FLOW_RESTARTED";

    private final TasksService tasksService;
    private final ProductOrdersService productOrdersService;
    private final InfonovaLinkService infonovaLinkService;
    private final TagService tagService;
    private final CustomerDataService customerDataService;
    private final SalesAgentHandler salesAgentHandler;

    public TaskV2DomainTask createOrderVerificationTask(String tenant, String wsTenant, String draftOrderId, Boolean isRestarted) {
        ProductorderingV1DomainDraftOrder draftOrder = productOrdersService.getDraftOrder(tenant, draftOrderId);
        List<String> draftOrderNamesList = draftOrder.getDraftProducts().stream().map(ProductorderingV1DomainDraftProduct::getInternalName)
                .collect(Collectors.toList());
        String draftOrderName = draftOrderNamesList.get(0);

        ProductorderingV1DomainShoppingCart draftCart = productOrdersService.createCartFromDraft(tenant, draftOrderId);

        if (draftCart.getProducts().isEmpty()) {
            throw new IllegalStateException("Draft cart has no products");
        }

        String opiNumber = draftCart.getProducts().get(0).getParameters().getOrDefault(OPI_NUMBER, "");
        String billingAccount = draftOrder.getBillingAccount();


        TaskV2DomainTask task = new TaskV2DomainTask()
                .taskDefinition(ORDER_VERIFICATION_TASK_DEFINITION)
                .billingAccount(draftOrder.getBillingAccount())
                .tags(tagService.getCommonTagsForSow(tenant, wsTenant, draftOrderName, draftOrder))
                .parameters(Map.of(
                        PRODUCT_NAME_PARAMETER, draftOrderName,
                        DRAFT_ORDER_ID, draftOrderId,
                        DRAFT_ORDER_LINK, infonovaLinkService.getDraftOrderLink(TenantType.RETAIL, draftOrderId, draftOrder.getBillingAccount()),
                        RT_BILLING_ACCOUNT_ID_PARAMETER, draftOrder.getBillingAccount(),
                        SALES_USERNAME_PARAMETER, draftOrder.getSavedBy(),
                        CUSTOMER_OPI_NUMBER,opiNumber,
                        CUSTOMER_BUSINESS_NAME, customerDataService.getCustomerTradingName(tenant, billingAccount)
                ));

        salesAgentHandler.addAgentDetailsFromShoppingCart(draftCart, task);

        if (isRestarted != null && isRestarted) {
            task.putParametersItem(IS_FLOW_RESTARTED, isRestarted.toString());
        }

        return tasksService.createTask(tenant, task);
    }
}
