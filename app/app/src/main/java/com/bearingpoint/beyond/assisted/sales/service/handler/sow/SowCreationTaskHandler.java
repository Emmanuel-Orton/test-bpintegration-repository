package com.bearingpoint.beyond.test-bpintegration.service.handler.sow;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.CustomerDataService;
import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.SalesAgentHandler;
import com.bearingpoint.beyond.test-bpintegration.service.tag.TagService;
import com.bearingpoint.beyond.test-bpintegration.util.ShoppingCartUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class SowCreationTaskHandler {

    public static final String OPI_NUM = "opi_num";
    public static final String TELUS_WS_SOW_CREATE = "TELUS_WS_SOW_CREATE";
    public static final String DRAFT_ORDER_ID = "DRAFT_ORDER_ID";
    public static final String PRODUCT_NAME = "PRODUCT_NAME";
    public static final String CUSTOMER_OPI_NUMBER = "CUSTOMER_OPI_NUMBER";
    public static final String OPERATOR_GIVEN_NAME = "OPERATOR_GIVEN_NAME";
    public static final String BILLING_ACCOUNT_ID = "RETAIL_BILLING_ACCOUNT_ID";
    public static final String WS_BILLING_ACCOUNT_ID = "WS_BILLING_ACCOUNT_ID";
    public static final String PARTNER_NAME = "PARTNER_NAME";
    public static final String CUSTOMER_BUSINESS_NAME = "CUSTOMER_BUSINESS_NAME";
    private static final String DRAFT_ORDER_LINK = "DRAFT_ORDER_LINK";
    public static final String CUSTOMER_ACCOUNT_LINK = "CUSTOMER_ACCOUNT_LINK";
    public static final String DEFAULT_EMPTY_STRING = "";


    private final TasksService tasksService;
    private final ProductOrdersService productOrdersService;
    private final CustomerDataService customerDataService;
    private final InfonovaLinkService infonovaLinkService;
    private final TagService tagService;
    private final SalesAgentHandler salesAgentHandler;

    @Value("${tenant_ws_name}")
    private String wsTenant;

    public TaskV2DomainTask createSowCreationTask(String tenant, String wsTenant, String draftOrderId) {
        ProductorderingV1DomainShoppingCart draftCart = productOrdersService.createCartFromDraft(tenant, draftOrderId);

        if (draftCart.getProducts().isEmpty()) {
            throw new IllegalStateException("Draft cart has no products");
        }

        ProductorderingV1DomainDraftOrder draftOrder = productOrdersService.getDraftOrder(tenant, draftOrderId);
        ProductorderingV1DomainDraftProduct draftProduct = draftOrder.getDraftProducts().get(0);
        ProductorderingV1DomainProduct product = draftCart.getProducts().get(0);
        String opiNumber = Optional.ofNullable(product.getParameters()).map(p -> p.getOrDefault(OPI_NUM,
                DEFAULT_EMPTY_STRING)).orElse(DEFAULT_EMPTY_STRING);
        String billingAccount = draftOrder.getBillingAccount();

        // to get wholesale billing account, we look at the offers of the bundle (not at bundle itself).
        ProductorderingV1DomainProduct offerProduct = ShoppingCartUtil.getOfferProduct(draftCart);
        String wsBillingAccount = customerDataService.getWholesaleBillingAccount(tenant, offerProduct.getProductOffering().getName());

        TaskV2DomainTask taskBody = new TaskV2DomainTask();
        taskBody.taskDefinition(TELUS_WS_SOW_CREATE)
                .billingAccount(wsBillingAccount)
                .tags(tagService.getCommonTagsForSow(tenant, wsTenant, draftProduct.getInternalName(), draftOrder,
                        wsBillingAccount
                ))
                .parameters(Map.ofEntries(
                        Map.entry(DRAFT_ORDER_ID, draftOrderId),
                        Map.entry(DRAFT_ORDER_LINK, infonovaLinkService.getDraftOrderLink(TenantType.RETAIL, draftOrderId, billingAccount)),
                        Map.entry(PRODUCT_NAME, draftProduct.getInternalName()),
                        Map.entry(CUSTOMER_OPI_NUMBER, opiNumber),
                        Map.entry(OPERATOR_GIVEN_NAME, draftOrder.getSavedBy()),
                        Map.entry(BILLING_ACCOUNT_ID, billingAccount),
                        Map.entry(CUSTOMER_BUSINESS_NAME, customerDataService.getCustomerTradingName(tenant, billingAccount)),
                        Map.entry(CUSTOMER_ACCOUNT_LINK, infonovaLinkService.getRetailCustomerAccountLink(billingAccount)),
                        Map.entry(WS_BILLING_ACCOUNT_ID, wsBillingAccount),
                        Map.entry(PARTNER_NAME, customerDataService.getCustomerTradingName(wsTenant, wsBillingAccount))
                        ));

        salesAgentHandler.addAgentDetailsFromShoppingCart(draftCart, taskBody);

        return tasksService.createTask(this.wsTenant, taskBody);
    }
}
