package com.bearingpoint.beyond.test-bpintegration.api.frontend;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DafOrderItem;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DafStartParameters;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.ProvisioningOrderItemDto;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowStatus;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowTypes;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@ExtendWith(MockitoExtension.class)
class DafWorkflowControllerTest {

	private static final String TENANT = "/SB_TELUS_SIT";
	private static final String WORKFLOW_ID = "123";

	MockMvc mockMvc;

	ObjectMapper om;

	@Mock
	ObjectMapper objectMapper;

	@Mock
	DafHandler dafHandler;

	@Mock
	ProvisioningHandler provisioningHandler;

	@Mock
	WorkflowUtil workflowUtil;

	@InjectMocks
	DafWorkflowController controller;

	@BeforeEach
	public void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		om = new ObjectMapper();
	}

	@Test
	void startDafProcessWorkflow() throws Exception {

		List<DafOrderItem> requestList = List.of(DafOrderItem.builder().id(1L).build(), DafOrderItem.builder().id(2L).build());

		when(dafHandler.startWorkflow(new DafStartParameters(1L, requestList))).thenReturn("111");
		mockMvc.perform(post(TENANT + "/v1/daf/DAFProcessStart")
						.contentType(MediaType.APPLICATION_JSON)
						.content(om.writeValueAsString(
								Map.of(
										"wholesaleOrderId", 1L,
										"selectedOrderItems", requestList)))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.workflowId").value("111"));
	}

	@Test
	void getDafProcessStatus() throws Exception {
		final String workflowId = "123";
		final ProvisioningOrderItemDto orderItemDto = new ProvisioningOrderItemDto();
		when(provisioningHandler.getWholesaleOrderItems(workflowId)).thenReturn(List.of(orderItemDto));
		mockMvc.perform(get(TENANT + "/v1/daf/DAFProcessStatus/" + workflowId)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0]").value(orderItemDto));
	}

	@Test
	void getDafWorkflowStatus() throws Exception {
		final WorkflowStatus workflowStatus = new WorkflowStatus();
		when(workflowUtil.getWorkflowStatus(WORKFLOW_ID, WorkflowTypes.DELIVERY_ACCEPTANCE_FORM)).thenReturn(workflowStatus);
		mockMvc.perform(get(TENANT + "/v1/daf/workflowStatus/" + WORKFLOW_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(workflowStatus));
	}
}