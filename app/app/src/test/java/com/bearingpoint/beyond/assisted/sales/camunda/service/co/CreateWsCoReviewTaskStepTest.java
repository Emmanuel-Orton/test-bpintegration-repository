package com.bearingpoint.beyond.test-bpintegration.camunda.service.co;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.co.ChangeOrderHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.ExecutionMockBuilder;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateWsCoReviewTaskStepTest {

	CreateWsCoReviewTaskStep createWsCoReviewTaskStep;

	ChangeOrderHandler changeOrderHandler;
	BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;

	WorkflowUtil workflowUtil;

	@BeforeEach
	void setUp() {
		changeOrderHandler = mock(ChangeOrderHandler.class);
		basedOnTheOtherTaskHandler = mock(BasedOnTheOtherTaskHandler.class);
		final ObjectMapper objectMapper = new ObjectMapper();
		workflowUtil = mock(WorkflowUtil.class);
		createWsCoReviewTaskStep = new CreateWsCoReviewTaskStep(objectMapper, workflowUtil, changeOrderHandler,
				basedOnTheOtherTaskHandler);
	}

	@Test
	void hasRetry() {
		assertThat(createWsCoReviewTaskStep.hasRetry()).isTrue();
	}

	@Test
	void executeStepNewTask() throws Exception {
		final ExecutionEntity execution = new ExecutionMockBuilder()
				.mockGetVariableReturnNull("TASK_TELUS_WS_CO_REVIEW_ID")
				.mockGetMandatoryVariable("TENANT_WS", "tenantws")
				.mockGetMandatoryVariable("WS_ORDER", 1L)
				.mockGetMandatoryVariable("WORKFLOW_ID", "123")
				.build();

		final TaskV2DomainTask task = mock(TaskV2DomainTask.class);
		when(changeOrderHandler.createReviewTask(
				"tenantws",
				1L,
				"123"
		)).thenReturn(task);
		when(task.getId()).thenReturn("111");
		when(task.getTaskDefinition()).thenReturn("TASK_TELUS_WS_CO_REVIEW");

		createWsCoReviewTaskStep.executeStep(execution);

		verify(execution, times(1)).setVariable("TASK_TELUS_WS_CO_REVIEW_ID", "111");
		verifySetTaskInfo(execution);
	}

	@Test
	void getStepTenantType() {
		assertThat(createWsCoReviewTaskStep.getStepTenantType()).isEqualTo(TenantType.WHOLESALE);
	}

	private void verifySetTaskInfo(ExecutionEntity execution) {
		verify(execution, times(1)).setVariable("WF_CURRENT_TASK", "{\"taskId\":\"111\",\"taskDefinition\":\"TASK_TELUS_WS_CO_REVIEW\",\"tenant\":\"tenantws\"}");
		verify(execution, times(1)).setVariable("WF_PREVIOUS_TASK", "{\"taskId\":\"111\",\"taskDefinition\":\"TASK_TELUS_WS_CO_REVIEW\",\"tenant\":\"tenantws\"}");
	}
}