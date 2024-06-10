package com.bearingpoint.beyond.test-bpintegration.api.events.order;

import com.bearingpoint.beyond.test-bpintegration.api.common.MockMvcMockitoTest;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEvent;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.DafCamundaMessages.DAF_APPROVE_TASK_RESOLVED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.DafCamundaMessages.DAF_REVIEW_TASK_RESOLVED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.DafCamundaMessages.DAF_UPDATE_TASK_RESOLVED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_ID;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler.WF_APPROVAL_WARNING_TIMER_CANCELLED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DafTasksEventsControllerTest extends MockMvcMockitoTest {


	@Mock
	private DafHandler dafHandler;

	@InjectMocks
	private DafTasksEventsController dafTasksEventsController;

	@Override
	protected Object getController() {
		return dafTasksEventsController;
	}

//	@Test
//	void wsDafReviewCloseAction() throws Exception {
//		final TaskV2DomainTaskEvent taskEvent = prepareTaskEvent();
//
//		mockMvc.perform(post(TENANT_URL + "/event/v1/telusWsDafReviewTaskResolved")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(objectMapper.writeValueAsString(taskEvent))
//						.accept(MediaType.APPLICATION_JSON))
//				.andExpect(status().isOk());
//
//		Mockito.verify(workflowUtil).handleTaskResolutionEvent(taskEvent, WORKFLOW_ID,
//				DAF_REVIEW_TASK_RESOLVED.getMessage(), TENANT);
//	}

//	@Test
//	void wsDafApproveCloseAction() throws Exception {
//		final TaskV2DomainTaskEvent taskEvent = prepareTaskEvent();
//
//		mockMvc.perform(post(TENANT_URL +"/event/v1/telusWsDafApproveTaskResolved")
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(objectMapper.writeValueAsString(taskEvent))
//						.accept(MediaType.APPLICATION_JSON))
//				.andExpect(status().isOk());
//
//		Mockito.verify(workflowUtil).handleTaskEventAndCreateMessageWithVariableSet(taskEvent, WORKFLOW_ID, true,
//				DAF_APPROVE_TASK_RESOLVED.getMessage(), TENANT, WF_APPROVAL_WARNING_TIMER_CANCELLED, "true");
//	}

	@Test
	void wsDafApproveUpdateAction() throws Exception {
		final TaskV2DomainTaskEvent taskEvent = prepareTaskEvent();
		Mockito.when(dafHandler.hasApprovalDueDateChanged(taskEvent)).thenReturn(true);

		mockMvc.perform(post(TENANT_URL +"/event/v1/telusWsDafApproveTaskUpdated")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(taskEvent))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		Mockito.verify(dafHandler).updateDafApprovalWarningTimer(taskEvent.getEvent().getCurrent());
	}

	@Test
	void dafWsUpdateTaskResolved() throws Exception {
		final TaskV2DomainTaskEvent taskEvent = prepareTaskEvent();

		mockMvc.perform(post(TENANT_URL +"/event/v1/telusWsDafUpdateTaskResolved")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(taskEvent))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		Mockito.verify(workflowUtil).handleTaskResolutionEvent(taskEvent, WORKFLOW_ID,
				DAF_UPDATE_TASK_RESOLVED.getMessage(), TENANT);
	}
}