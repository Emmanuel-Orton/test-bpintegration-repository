package com.bearingpoint.beyond.test-bpintegration.service.handler.co;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainServiceCharacteristic;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.users.model.UserV1DomainUser;
import com.bearingpoint.beyond.test-bpintegration.service.CustomerDataService;
import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOfferingsService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.UserService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.SalesAgentHandler;
import com.bearingpoint.beyond.test-bpintegration.service.tag.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_ID;
import static com.bearingpoint.beyond.test-bpintegration.service.defs.TaskParameters.OPERATOR_NAME;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChangeOrderHandler {
	public static final String TELUS_WS_CO_REVIEW_DEFINITION = "TELUS_WS_CO_REVIEW";
	public static final String WS_ORDER_ID = "WS_ORDER_ID";
	public static final String WS_ORDER_LINK = "WS_ORDER_LINK";
	public static final String WS_CSM_ORDER_LINK = "WS_CSM_ORDER_LINK";

	public static final String PRODUCT_NAME = "PRODUCT_NAME";
	public static final String CUSTOMER_OPI_NUMBER = "CUSTOMER_OPI_NUMBER";
	public static final String WS_BILLING_ACCOUNT_ID = "WS_BILLING_ACCOUNT_ID";
	public static final String CUSTOMER_BUSINESS_NAME = "CUSTOMER_BUSINESS_NAME";
	public static final String OPI_NUM_ORDER_PARAMETER = "opi_num";
	public static final String OPERATOR_EMAIL = "OPERATOR_EMAIL";
	public static final String TELUS_RT_CO_CREATION_DEFINITION = "TELUS_RT_CO_CREATION";
	public static final String RT_ORDER_ID = "RT_ORDER_ID";
	public static final String RT_ORDER_LINK = "RT_ORDER_LINK";

	private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;
	private final TasksService tasksService;
	private final ProductOrdersService productOrdersService;
	private final InfonovaLinkService infonovaLinkService;
	private final ProductOfferingsService productOfferingsService;
	private final CustomerDataService customerDataService;
	private final UserService userService;
	private final TagService tagService;
	private final SalesAgentHandler salesAgentHandler;

	@Value("${tenant_name}")
	private String rtTenant;


	public TaskV2DomainTask createReviewTask(String tenant, Long wsOrderIdLong, String workflowId) {
		String wsOrderId = wsOrderIdLong.toString();
		final ProductorderingV1DomainProductOrder wsOrder = productOrdersService.getProductOrder(tenant, wsOrderId);
		final ProductorderingV1DomainProductOrder rtOrder = productOrdersService.getRetailOrderForTheWholesale(wsOrder);

		final String wsBillingAccount = wsOrder.getBillingAccount();

		final String productName = productOfferingsService.getProductOfferingInternalName(rtTenant,
				rtOrder.getOrderItems().get(0).getProduct().getProductOffering().getName());

		TaskV2DomainTask taskBody = new TaskV2DomainTask()
				.taskDefinition(TELUS_WS_CO_REVIEW_DEFINITION)
				.billingAccount(wsBillingAccount)
				.tags(tagService.getCommonTagsForCo(tenant, wsBillingAccount, productName, wsOrder))
				.parameters(Map.ofEntries(
								Map.entry(WS_ORDER_ID, wsOrderId),
								Map.entry(WS_ORDER_LINK, infonovaLinkService.getOrderLink(TenantType.WHOLESALE, wsOrderId, wsBillingAccount)),
								Map.entry(WS_CSM_ORDER_LINK, infonovaLinkService.getOrderLink(TenantType.WHOLESALE_CSM, wsOrderId, wsBillingAccount)),
								Map.entry(PRODUCT_NAME, productOfferingsService.getProductOfferingInternalName(rtTenant,
										rtOrder.getOrderItems().get(0).getProduct().getProductOffering().getName())),
								Map.entry(CUSTOMER_OPI_NUMBER, rtOrder.getParameters().get(OPI_NUM_ORDER_PARAMETER)),
								Map.entry(WS_BILLING_ACCOUNT_ID, wsBillingAccount),
								Map.entry(WORKFLOW_ID, workflowId),
								Map.entry(CUSTOMER_BUSINESS_NAME, customerDataService.getCustomerTradingName(rtTenant, rtOrder.getBillingAccount()))
						));

		salesAgentHandler.addAgentDetailsFromProductOrder(wsOrder, taskBody);

		return tasksService.createTask(tenant, taskBody);

	}

	public TaskV2DomainTask createRetailCOCreateTask(String rtTenant, String wsTenant, Long wsOrderIdLong, String workflowId){
		String wsOrderId = wsOrderIdLong.toString();

		final ProductorderingV1DomainProductOrder wsOrder = productOrdersService.getProductOrder(wsTenant, wsOrderId);
		final ProductorderingV1DomainProductOrder rtOrder = productOrdersService.getRetailOrderForTheWholesale(wsOrder);

		String operatorName = rtOrder.getOperator();

		// when order is placed through SOW workflow we have wrong operator (its operator of test-bpintegration), instead of one that
		// placed the draft order, so that parameter is stored as parameter
		if (!CollectionUtils.isEmpty(rtOrder.getParameters()) && rtOrder.getParameters().containsKey(OPERATOR_NAME.name().toLowerCase())) {
			operatorName = rtOrder.getParameters().get(OPERATOR_NAME.name().toLowerCase());
		}

		UserV1DomainUser operatorUser = userService.retrieveUserByUserName(rtTenant, operatorName);

		final String wsBillingAccount = wsOrder.getBillingAccount();
		final String rtBillingAccount = rtOrder.getBillingAccount();

		final String productName = productOfferingsService.getProductOfferingInternalName(rtTenant,
				rtOrder.getOrderItems().get(0).getProduct().getProductOffering().getName());

		TaskV2DomainTask taskBody = new TaskV2DomainTask()
				.taskDefinition(TELUS_RT_CO_CREATION_DEFINITION)
				.billingAccount(rtBillingAccount)
				.tags(tagService.getCommonTagsForCo(wsTenant, wsBillingAccount, productName, wsOrder))
				.parameters(Map.ofEntries(
								Map.entry(RT_ORDER_ID, rtOrder.getId()),
								Map.entry(RT_ORDER_LINK, infonovaLinkService.getOrderLink(TenantType.RETAIL, rtOrder.getId(), rtBillingAccount)),
								Map.entry(WORKFLOW_ID, workflowId),
								Map.entry(CUSTOMER_BUSINESS_NAME, customerDataService.getCustomerTradingName(rtTenant, rtBillingAccount)),
								Map.entry(OPERATOR_EMAIL, operatorUser.getEmail()),
								Map.entry(WS_ORDER_ID, wsOrderId),
								Map.entry(WS_ORDER_LINK, infonovaLinkService.getOrderLink(TenantType.WHOLESALE, wsOrderId, wsBillingAccount)),
								Map.entry(WS_CSM_ORDER_LINK, infonovaLinkService.getOrderLink(TenantType.WHOLESALE_CSM, wsOrderId, wsBillingAccount))
								));

		salesAgentHandler.addAgentDetailsFromProductOrder(wsOrder, taskBody);

		return tasksService.createTask(rtTenant, taskBody);
	}



}
