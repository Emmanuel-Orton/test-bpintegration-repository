package com.bearingpoint.beyond.test-bpintegration.service.handler.sow;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainServiceCharacteristic;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.users.model.UserV1DomainUser;
import com.bearingpoint.beyond.test-bpintegration.service.CustomerDataService;
import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.UserService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.SalesAgentHandler;
import com.bearingpoint.beyond.test-bpintegration.service.tag.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RetailSowCreationTaskHandler {
    public static final String RETAIL_SOW_CREATION_TASK_DEFINITION = "TELUS_RT_SOW_CREATE";
    public static final String PRODUCT_NAME_PARAMETER = "PRODUCT_NAME";
    public static final String SALES_USERNAME_PARAMETER = "SALES_USERNAME";
    public static final String CUSTOMER_BUSINESS_NAME_PARAMETER = "CUSTOMER_BUSINESS_NAME";
    public static final String OPERATOR_EMAIL_PARAMETER = "OPERATOR_EMAIL";

    public static final String DRAFT_ORDER_ID = "DRAFT_ORDER_ID";
    public static final String DRAFT_ORDER_LINK = "DRAFT_ORDER_LINK";
    public static final String REVIEW_TASK_ID = "REVIEW_TASK_ID";
    public static final String RT_BILLING_ACCOUNT_ID_PARAMETER = "RT_BILLING_ACCOUNT_ID";

    private final TasksService tasksService;
    private final ProductOrdersService productOrdersService;
    private final InfonovaLinkService infonovaLinkService;
    private final CustomerDataService customerDataService;
    private final UserService userService;
    private final TagService tagService;
    private final SalesAgentHandler salesAgentHandler;

    public TaskV2DomainTask createRetailSowCreationTask(String tenant, String wsTenant, String draftOrderId, String reviewTaskId) {
        ProductorderingV1DomainDraftOrder draftOrder = productOrdersService.getDraftOrder(tenant, draftOrderId);
        List<String> draftOrderNamesList = draftOrder.getDraftProducts().stream()
                .map(ProductorderingV1DomainDraftProduct::getInternalName)
                .collect(Collectors.toList());
        String draftOrderName = draftOrderNamesList.get(0);
        String customerTradingName = customerDataService.getCustomerTradingName(tenant, draftOrder.getBillingAccount());

        String operatorName = draftOrder.getSavedBy();
        UserV1DomainUser operatorUser = userService.retrieveUserByUserName(tenant, operatorName);

        if (operatorUser == null) {
            throw new IllegalArgumentException("Operator " + operatorName + " couldn't be found.");
        }

        if (operatorUser.getEmail() == null) {
            throw new IllegalArgumentException("Operator " + operatorName + " doesn't have email set in Infonova.");
        }

        //Getting Cart
        ProductorderingV1DomainShoppingCart draftCart = productOrdersService.createCartFromDraft(tenant, draftOrderId);

        if (draftCart.getProducts().isEmpty()) {
            throw new IllegalStateException("Draft cart has no products");
        }

        final TaskV2DomainTask task = new TaskV2DomainTask()
                .taskDefinition(RETAIL_SOW_CREATION_TASK_DEFINITION)
                .billingAccount(draftOrder.getBillingAccount())
                .tags(tagService.getCommonTagsForSow(tenant, wsTenant, draftOrderName, draftOrder))
                .parameters(Map.ofEntries(
                        Map.entry(PRODUCT_NAME_PARAMETER, draftOrderName),
                        Map.entry(DRAFT_ORDER_ID, draftOrderId),
                        Map.entry(CUSTOMER_BUSINESS_NAME_PARAMETER, customerTradingName),
                        Map.entry(OPERATOR_EMAIL_PARAMETER, operatorUser.getEmail()),
                        Map.entry(DRAFT_ORDER_LINK, infonovaLinkService.getDraftOrderLink(TenantType.RETAIL, draftOrderId, draftOrder.getBillingAccount())),
                        Map.entry(RT_BILLING_ACCOUNT_ID_PARAMETER, draftOrder.getBillingAccount()),
                        Map.entry(SALES_USERNAME_PARAMETER, operatorName),
                        Map.entry(REVIEW_TASK_ID, reviewTaskId)
                ));

        salesAgentHandler.addAgentDetailsFromShoppingCart(draftCart, task);

        return tasksService.createTask(tenant, task);
    }
}