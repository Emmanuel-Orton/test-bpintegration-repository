package com.bearingpoint.beyond.test-bpintegration.service.handler.daf;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.CustomerDataService;
import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOfferingsService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.tag.TagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_ID;
import static com.bearingpoint.beyond.test-bpintegration.constant.OrderParametersNames.OPI_NUM_ORDER_PARAMETER;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.sow.SowCreationTaskHandler.PARTNER_NAME;

@Component
@Slf4j
@RequiredArgsConstructor
public class DafReviewTaskHandler {
    public static final String TELUS_WS_DAF_REVIEW_TASK_DEFINITION = "TELUS_WS_DAF_REVIEW";

    public static final String TELUS_WS_DAF_APPROVE_TASK_DEFINITION = "TELUS_WS_DAF_APPROVE";
    public static final String WS_ORDER_ID = "WS_ORDER_ID";
    public static final String PRODUCT_NAME = "PRODUCT_NAME";
    public static final String CUSTOMER_OPI_NUMBER = "CUSTOMER_OPI_NUMBER";
    public static final String WS_BILLING_ACCOUNT_ID = "WS_BILLING_ACCOUNT_ID";
    public static final String CUSTOMER_BUSINESS_NAME = "CUSTOMER_BUSINESS_NAME";
    public static final String WS_ORDER_LINK = "WS_ORDER_LINK";
    public static final String RT_ORDER_LINK = "RT_ORDER_LINK";
    public static final String RT_ORDER_ID = "RT_ORDER_ID";
    private static final String WS_CSM_ORDER_LINK = "WS_CSM_ORDER_LINK";

    public static final String DAF_SERVICE_ITEMS = "DAF_SERVICE_ITEMS";

    private final TasksService tasksService;
    private final ProductOrdersService productOrdersService;
    private final CustomerDataService customerDataService;
    private final InfonovaLinkService infonovaLinkService;
    private final ProductOfferingsService productOfferingsService;
    private final DafHandler dafHandler;
    private final ObjectMapper objectMapper;
    private final TagService tagService;

    @Value("${tenant_name}")
    private String rtTenant;

    public TaskV2DomainTask createDafReviewTask(String tenant, Long wsOrderIdLong, String workflowId) throws JsonProcessingException {
        String wsOrderId = wsOrderIdLong.toString();
        final ProductorderingV1DomainProductOrder wsOrder = productOrdersService.getProductOrder(tenant, wsOrderId);
        final ProductorderingV1DomainProductOrder rtOrder = productOrdersService.getRetailOrderForTheWholesale(wsOrder);

        final String wsBillingAccount = wsOrder.getBillingAccount();
        final String rtBillingAcount = rtOrder.getBillingAccount();

       String jsonObjectOfServiceItems = objectMapper.writeValueAsString(dafHandler.getDafServiceOrderItemsList(workflowId));

        final String productName = productOfferingsService.getProductOfferingInternalName(rtTenant,
                rtOrder.getOrderItems().get(0).getProduct().getProductOffering().getName());

        TaskV2DomainTask taskBody = new TaskV2DomainTask()
                .taskDefinition(TELUS_WS_DAF_REVIEW_TASK_DEFINITION)
                .billingAccount(wsBillingAccount)
                .tags(tagService.getCommonTagsForDaf(tenant, wsBillingAccount, productName, wsOrder))
                .parameters(Map.ofEntries(
                        Map.entry(WS_ORDER_ID, wsOrderId),
                        Map.entry(WS_ORDER_LINK, infonovaLinkService.getOrderLink(TenantType.WHOLESALE, wsOrderId, wsBillingAccount)),
                        Map.entry(WS_CSM_ORDER_LINK, infonovaLinkService.getOrderLink(TenantType.WHOLESALE_CSM, wsOrderId, wsBillingAccount)),
                        Map.entry(PRODUCT_NAME, productName),
                        Map.entry(CUSTOMER_OPI_NUMBER, rtOrder.getParameters().get(OPI_NUM_ORDER_PARAMETER)),
                        Map.entry(WS_BILLING_ACCOUNT_ID, wsBillingAccount),
                        Map.entry(WORKFLOW_ID, workflowId),
                        Map.entry(DAF_SERVICE_ITEMS, jsonObjectOfServiceItems),
                        Map.entry(CUSTOMER_BUSINESS_NAME, customerDataService.getCustomerTradingName(rtTenant, rtOrder.getBillingAccount())),
                        Map.entry(PARTNER_NAME, customerDataService.getCustomerTradingName(tenant, wsBillingAccount)),
                        Map.entry(RT_ORDER_ID, rtOrder.getId()),
                        Map.entry(RT_ORDER_LINK, infonovaLinkService.getOrderLink(TenantType.RETAIL, rtOrder.getId(), rtBillingAcount))
                ));

        return tasksService.createTask(tenant, taskBody);
    }

}
