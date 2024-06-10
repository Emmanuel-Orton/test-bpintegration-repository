package com.bearingpoint.beyond.test-bpintegration.api.frontend;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowStatus;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.service.handler.sow.SowHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SowWorkflowControllerTest {

	private static final String TENANT_URL = "/SB_TELUS_SIT";
	private static final String TENANT = "SB_TELUS_SIT";
	private static final String DRAFT_ORDER = "draftOrder";

	MockMvc mockMvc;

	ObjectMapper om;

	@Mock
    SowHandler sowHandler;

	@Mock
	WorkflowUtil workflowUtil;

	@InjectMocks
	SowWorkflowController controller;

	@BeforeEach
	public void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		om = new ObjectMapper();
	}

	@Test
	void startSowWorkflow() throws Exception {

		mockMvc.perform(post(TENANT_URL + "/v1/startSowWorkflow/" + DRAFT_ORDER)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		verify(sowHandler, times(1)).startWorkflow(DRAFT_ORDER);

	}

	@Test
	void getSowWorkflowStatus() throws Exception {
		final WorkflowStatus status = new WorkflowStatus();
		when(sowHandler.getWorkflowStatus(DRAFT_ORDER)).thenReturn(status);

		mockMvc.perform(get(TENANT_URL + "/v1/workflowStatus/" + DRAFT_ORDER)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(status));
	}

	@Test
	void cancelSowWorkflow() throws Exception {
		mockMvc.perform(post(TENANT_URL + "/v1/cancelSowWorkflow/" + DRAFT_ORDER)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		verify(workflowUtil, times(1)).cancelWorkflow(DRAFT_ORDER, "SOW_Cancel_Request_Received",
				"SowWorkflow (draftOrderId=" + DRAFT_ORDER + ")");
	}
}
