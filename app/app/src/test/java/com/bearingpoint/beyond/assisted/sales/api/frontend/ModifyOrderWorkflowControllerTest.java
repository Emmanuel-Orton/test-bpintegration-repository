package com.bearingpoint.beyond.test-bpintegration.api.frontend;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.ModifyOrderStartParameters;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ModifyOrderWorkflowControllerTest {

	private static final String CURRENT_TENANT = "SB_TELUS_SIT";
	private static final Long START_TASK_ID = 8584785748L;
	private static final String SERVICE_INSTANCE_NAME = "1192598181_b4b3aed47cf343e19d6bfa3d44ab68d0";
	private static final String SERVICE_NAME = "WSO Monitor It Service";
	private static final Long WS_CUSTOMER_ID = 1192685046L;
	private static final Long RT_CUSTOMER_ID = 1192685040L;

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@Mock
	private WorkflowUtil workflowUtil;

	@InjectMocks
	private ModifyOrderWorkflowController modifyOrderWorkflowController;

	@BeforeEach
	public void setup() {
		objectMapper = new ObjectMapper();
		mockMvc = MockMvcBuilders.standaloneSetup(modifyOrderWorkflowController)
				.build();
	}

	@Test
	@DisplayName("Should Start Modify Order Camunda Workflow and Return workflowId")
	void startMoWorkflow() throws Exception {

		ModifyOrderStartParameters startParameters = new ModifyOrderStartParameters(2L, SERVICE_INSTANCE_NAME, SERVICE_NAME, "jolera_user");
		when(workflowUtil.startModifyOrderWorkflow(CURRENT_TENANT,
				startParameters)).thenReturn("MO_" + SERVICE_INSTANCE_NAME);

		mockMvc.perform(post("/" + CURRENT_TENANT+ "/v1/modifyOrder/startModifyOrder")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(startParameters))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.workflowId").value("MO_" + SERVICE_INSTANCE_NAME))
				.andExpect(jsonPath("$.started").value(true))
				.andExpect(jsonPath("$.alreadyRunning").value(false));
	}

	@Test
	@DisplayName("Should Not Start Modify Order Camunda Workflow if it's already running and return that the WF has already started")
	void startCoWorkflowWfExist() throws Exception {
		ModifyOrderStartParameters startParameters = new ModifyOrderStartParameters(2L, SERVICE_INSTANCE_NAME, SERVICE_NAME, "jolera_user");

		mockMvc.perform(post("/" + CURRENT_TENANT+ "/v1/modifyOrder/startModifyOrder")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(startParameters))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.workflowId").value("MO_" + SERVICE_INSTANCE_NAME))
				.andExpect(jsonPath("$.started").value(false))
				.andExpect(jsonPath("$.alreadyRunning").value(true));
		verify(workflowUtil, times(1)).startModifyOrderWorkflow(CURRENT_TENANT, startParameters);
	}

	@Test
	void getWorkflowStatus() throws Exception {
		final WorkflowStatus workflowStatus = WorkflowStatus.builder()
				.running(true)
				.canRestart(true)
				.stepId("21")
				.businessKey("MO_" + SERVICE_INSTANCE_NAME)
				.workflowType("Unknown")
				.processInstanceId("23")
				.processDefinitionId("24")
				.build();
		when(workflowUtil.getWorkflowStatus("MO_" + SERVICE_INSTANCE_NAME, WorkflowTypes.MODIFY_ORDER)).thenReturn(workflowStatus);

		mockMvc.perform(get("/" + CURRENT_TENANT+ "/v1/modifyOrder/workflowStatus/{serviceInstanceName}", SERVICE_INSTANCE_NAME)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.running").value(true))
				.andExpect(jsonPath("$.canRestart").value(true))
				.andExpect(jsonPath("$.stepId").value("21"))
				.andExpect(jsonPath("$.businessKey").value("MO_" + SERVICE_INSTANCE_NAME))
				.andExpect(jsonPath("$.workflowType").value("Unknown"))
				.andExpect(jsonPath("$.processInstanceId").value("23"))
				.andExpect(jsonPath("$.processDefinitionId").value("24"));
	}
}
