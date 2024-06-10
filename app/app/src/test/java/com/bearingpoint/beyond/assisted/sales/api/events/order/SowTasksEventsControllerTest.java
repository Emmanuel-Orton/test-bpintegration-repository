package com.bearingpoint.beyond.test-bpintegration.api.events.order;

import com.bearingpoint.beyond.test-bpintegration.api.common.MockMvcMockitoTest;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEvent;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.http.MediaType;

import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.SowCamundaMessages.SOW_APPROVE_TASK_RESOLVED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.SowCamundaMessages.SOW_CREATE_TASK_RESOLVED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.SowCamundaMessages.SOW_REVIEW_TASK_RESOLVED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.SowCamundaMessages.SOW_RT_APPROVE_TASK_RESOLVED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.SowCamundaMessages.SOW_UPDATE_TASK_RESOLVED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.SowCamundaMessages.SOW_VERIFICATION_TASK_RESOLVED;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SowTasksEventsControllerTest extends MockMvcMockitoTest {

	@InjectMocks
	private SowTasksEventsController sowTasksEventsController;

	@Override
	protected Object getController() {
		return sowTasksEventsController;
	}

	@Test
	void wsSowCreationTaskResolved() throws Exception {
		final TaskV2DomainTaskEvent taskEvent = prepareTaskEvent();

		mockMvc.perform(post("/" + TENANT + "/event/v1/sowCreationTaskResolved")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(taskEvent))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		verify(workflowUtil).handleTaskResolutionEvent(taskEvent, WorkflowUtil.DRAFT_ORDER_ID,
				SOW_CREATE_TASK_RESOLVED.getMessage(), TENANT);
	}

	@Test
	void wsSowReviewCloseAction() throws Exception {
		final TaskV2DomainTaskEvent taskEvent = prepareTaskEvent();

		mockMvc.perform(post("/" + TENANT + "/event/v1/telusWsSowReviewTaskResolved")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(taskEvent))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		verify(workflowUtil).handleTaskResolutionEvent(taskEvent, WorkflowUtil.DRAFT_ORDER_ID,
				SOW_REVIEW_TASK_RESOLVED.getMessage(), TENANT);
	}

	@Test
	void wsSowApproveCloseAction() throws Exception {
		final TaskV2DomainTaskEvent taskEvent = prepareTaskEvent();

		mockMvc.perform(post("/" + TENANT + "/event/v1/telusWsSowApproveTaskResolved")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(taskEvent))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		verify(workflowUtil).handleTaskResolutionEvent(taskEvent, WorkflowUtil.DRAFT_ORDER_ID,
				SOW_APPROVE_TASK_RESOLVED.getMessage(), TENANT);
	}

	@Test
	void createRetailSowApproveCloseAction() throws Exception {
		final TaskV2DomainTaskEvent taskEvent = prepareTaskEvent();

		mockMvc.perform(post("/" + TENANT + "/event/v1/telusRetailSowApproveTaskResolved")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(taskEvent))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		verify(workflowUtil).handleTaskResolutionEvent(taskEvent, WorkflowUtil.DRAFT_ORDER_ID,
				SOW_RT_APPROVE_TASK_RESOLVED.getMessage(), TENANT);

	}

	@Test
	void sowWsUpdateTaskResolved() throws Exception {
		final TaskV2DomainTaskEvent taskEvent = prepareTaskEvent();

		mockMvc.perform(post("/" + TENANT + "/event/v1/telusWsSowUpdateTaskResolved")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(taskEvent))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		verify(workflowUtil).handleTaskResolutionEvent(taskEvent, WorkflowUtil.DRAFT_ORDER_ID,
				SOW_UPDATE_TASK_RESOLVED.getMessage(), TENANT);
	}

	@Test
	void orderVerificationTaskResolved() throws Exception {
		final TaskV2DomainTaskEvent taskEvent = prepareTaskEvent();

		mockMvc.perform(post("/" + TENANT + "/event/v1/orderVerificationTaskResolved")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(taskEvent))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		verify(workflowUtil).handleTaskEventAndCreateMessage(taskEvent, WorkflowUtil.DRAFT_ORDER_ID, true,
				SOW_VERIFICATION_TASK_RESOLVED.getMessage(), TENANT);
	}
}