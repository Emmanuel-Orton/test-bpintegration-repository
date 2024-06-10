package com.bearingpoint.beyond.test-bpintegration.api.frontend;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.CoStartParameters;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowStatus;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowTypes;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CoWorkflowControllerTest {

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@Mock
	private WorkflowUtil workflowUtil;

	@InjectMocks
	private CoWorkflowController coWorkflowController;

	@BeforeEach
	public void setup() {
		objectMapper = new ObjectMapper();
		mockMvc = MockMvcBuilders.standaloneSetup(coWorkflowController)
				.build();
	}

	@Test
	@DisplayName("Should Start Change Order Camunda Workflow and Return workflowId")
	void startCoWorkflow() throws Exception {
		when(workflowUtil.startCoWorkflow(new CoStartParameters(1L, 2L))).thenReturn("111");

		mockMvc.perform(post("/test_tenant/v1/changeOrder/startChangeOrder")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"wholesaleOrderId", "1",
								"billingAccountId", "2")))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.workflowId").value("111"))
				.andExpect(jsonPath("$.started").value(true))
				.andExpect(jsonPath("$.alreadyRunning").value(false));
	}

	@Test
	@DisplayName("Should Not Start Change Order Camunda Workflow if it's already running and return that the WF has already started")
	void startCoWorkflowWfExist() throws Exception {
		mockMvc.perform(post("/test_tenant/v1/changeOrder/startChangeOrder")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of(
								"wholesaleOrderId", "1",
								"billingAccountId", "2")))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.workflowId").value("CO_1"))
				.andExpect(jsonPath("$.started").value(false))
				.andExpect(jsonPath("$.alreadyRunning").value(true));
		verify(workflowUtil, times(1)).startCoWorkflow(new CoStartParameters(1L, 2L));
	}

	@Test
	void getWorkflowStatus() throws Exception {
		final String wholesaleOrderId = "123213";

		final WorkflowStatus workflowStatus = WorkflowStatus.builder()
				.running(true)
				.canRestart(true)
				.stepId("21")
				.businessKey("22")
				.workflowType("Unknown")
				.processInstanceId("23")
				.processDefinitionId("24")
				.build();
		when(workflowUtil.getWorkflowStatus("CO_" + wholesaleOrderId, WorkflowTypes.CHANGE_ORDER)).thenReturn(workflowStatus);

		mockMvc.perform(get("/test_tenant/v1/changeOrder/workflowStatus/{wholesaleOrderId}", wholesaleOrderId)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.running").value(true))
				.andExpect(jsonPath("$.canRestart").value(true))
				.andExpect(jsonPath("$.stepId").value("21"))
				.andExpect(jsonPath("$.businessKey").value("22"))
				.andExpect(jsonPath("$.workflowType").value("Unknown"))
				.andExpect(jsonPath("$.processInstanceId").value("23"))
				.andExpect(jsonPath("$.processDefinitionId").value("24"));
	}
}