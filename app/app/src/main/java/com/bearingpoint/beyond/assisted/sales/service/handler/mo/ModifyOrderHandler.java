package com.bearingpoint.beyond.test-bpintegration.service.handler.mo;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskAttachmentTypes;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrderItem;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceinventory.model.ServiceinventoryV1DomainService;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.*;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.TaskAttachmentsHandler;
import com.bearingpoint.beyond.test-bpintegration.service.tag.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_ID;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.sow.SowCreationTaskHandler.PARTNER_NAME;

@Component
@Slf4j
@RequiredArgsConstructor
public class ModifyOrderHandler {

	public static final String WS_ORDER_ID = "WS_ORDER_ID";
	public static final String WS_ORDER_LINK = "WS_ORDER_LINK";
	public static final String WS_CSM_ORDER_LINK = "WS_CSM_ORDER_LINK";

	public static final String PRODUCT_NAME = "PRODUCT_NAME";
	public static final String WS_BILLING_ACCOUNT_ID = "WS_BILLING_ACCOUNT_ID";

	public static final String RT_ORDER_ID = "RT_ORDER_ID";
	public static final String RT_ORDER_LINK = "RT_ORDER_LINK";
	protected static final String CUSTOMER_NAME = "CUSTOMER_NAME";
	public static final String TASK_TELUS_RT_MO_CREATION_ID = "TASK_TELUS_RT_MO_CREATION_ID";
	public static final String CSM_CREATOR = "csmCreator";
	public static final String SERVICE_NAME = "serviceName";
	public static final String SERVICE_INSTANCE_NAME = "SERVICE_INSTANCE_NAME";
	public static final String CUSTOMER_LEGAL_NAME = "customerLegalName";

	private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;
	private final TasksService tasksService;
	private final ObjectMapper objectMapper;
	private final CustomerDataService customerDataService;
	private final TaskAttachmentsHandler taskAttachmentsHandler;

	private final ProductOrdersService productOrdersService;

	private final TagService tagService;

	public TaskV2DomainTask createReviewTask(String wholesaleTenant, Long wholesaleCustomer,
											 String previousTaskId) {

		TaskV2DomainTask newTask = basedOnTheOtherTaskHandler.createNewTaskFromOldOne(
				wholesaleTenant,
				previousTaskId,
				TaskType.TELUS_WS_CO_DAY2_REVIEW,
				false
		);

        Map<String, String> allParameters = newTask.getParameters();

		String customerTradingName = customerDataService.getCustomerTradingName(wholesaleTenant, wholesaleCustomer.toString());

		allParameters.put(PARTNER_NAME, customerTradingName);

		newTask.setParameters(allParameters);

		return tasksService.createTask(wholesaleTenant, newTask);

	}


	public TaskV2DomainTask createInitiateMoTask(Long wsCustomer, String tenant, String legalName,
												 String serviceInstanceName, String serviceName,
												 String csmUser, String workflowId) {

		final ProductorderingV1DomainProductOrder wsOrderBase = productOrdersService.getProductOrderWithServiceIdentifier(serviceInstanceName, tenant);

		String wsOrderId = wsOrderBase.getId();
		final ProductorderingV1DomainProductOrder wsOrder = productOrdersService.getProductOrder(tenant, wsOrderId);

		final ProductorderingV1DomainProductOrder rtOrder = productOrdersService.getRetailOrderForTheWholesale(wsOrder);

		final String productName =rtOrder.getOrderItems().get(0).getProduct().getProductOffering().getName();

		final TaskV2DomainTask task = new TaskV2DomainTask()
				.taskDefinition(TaskType.TELUS_WS_CO_DAY2_INITIATE.name())
				.billingAccount(wsCustomer.toString())
				.tags(tagService.getCommonTagsForCo(tenant, wsCustomer.toString(), productName, wsOrder))
				.parameters(Map.of(
						SERVICE_INSTANCE_NAME, serviceInstanceName,
						SERVICE_NAME, serviceName,
						CUSTOMER_LEGAL_NAME, legalName,
						CUSTOMER_NAME, legalName,
						CSM_CREATOR, csmUser,
						WORKFLOW_ID, workflowId,
						WS_ORDER_ID, wsOrder.getId()
				));

		return tasksService.createTask(tenant, task);
	}


	public TaskV2DomainTask copyTaskWithAttachments(TaskInfo newTaskInfo, TaskInfo previousTaskInfo,
													String taskCustomerId, Map<TaskType, List<TaskAttachmentTypes>> attachmentTypesToCopy) throws IOException {

		TaskV2DomainTask newTaskDef = basedOnTheOtherTaskHandler.createNewTaskFromOldOne(previousTaskInfo.getTenant(), previousTaskInfo.getTaskId(),
				TaskType.valueOf(newTaskInfo.getTaskDefinition()), false);

		newTaskDef.setBillingAccount(taskCustomerId);

		TaskV2DomainTask newTask = tasksService.createTask(newTaskInfo.getTenant(), newTaskDef);

		taskAttachmentsHandler.copyFirstAvailableAttachment(previousTaskInfo.getTenant(),
				previousTaskInfo.getTaskId(),
				newTask.getId(),
				previousTaskInfo.getTaskDefinition(),
				attachmentTypesToCopy
		);

		return newTask;
	}


	public TaskV2DomainTask createFinalizeTask(String wholesaleTenant,
											   String reviewTaskId,
											   String serviceIdentifier) {

		TaskV2DomainTask newTask = basedOnTheOtherTaskHandler.createNewTaskFromOldOne(
				wholesaleTenant,
				reviewTaskId,
				TaskType.TELUS_WS_CO_DAY2_FINALIZE,
				false
		);

		Map<String,String> allParameters = newTask.getParameters();

		if (!allParameters.containsKey(WS_ORDER_ID)) {
			tasksService.addAdditionalParameterToTask(WS_ORDER_ID,
					getWsOrderIdWithServiceIdentifier(serviceIdentifier, wholesaleTenant), newTask);
		}

		return tasksService.createTask(wholesaleTenant, newTask);

	}

	private String getWsOrderIdWithServiceIdentifier(String serviceIdentifier, String tenant) {
		ProductorderingV1DomainProductOrder order = productOrdersService.getProductOrderWithServiceIdentifier(serviceIdentifier, tenant);

		return order.getId();
	}

}
