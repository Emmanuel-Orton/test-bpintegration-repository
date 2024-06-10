package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.create;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.sow.OrderSummaryHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.sow.SowCreationTaskHandler;
import helpers.ExecutionMockBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSowCreateTaskStepTest extends AbstractWorkflowStepTest {

	private static final String TENANT = "TENANT";
	private static final String TENANT_WS = "TENANT_WS";
	private static final String DRAFT_ORDER = "DRAFT_ORDER";
	private static final String DRAFT_ORDER_ID = "DRAFT_ORDER_ID";
	private static final String TASK_DEFINITION = "TASK_DEFINITION";
	private static final String TASK_TELUS_WS_SOW_CREATE_ID = "TASK_TELUS_WS_SOW_CREATE_ID";

	@Mock
	SowCreationTaskHandler sowCreationTaskHandler;
	@Mock
	OrderSummaryHandler orderSummaryHandler;

	@InjectMocks
	CreateSowCreateTaskStep unit;

	@BeforeEach
	public void setUp() {
		setObjectMapper(unit);
	}


	@Test
	void executestep_hasSowCreateId() throws Exception {
		execution = new ExecutionMockBuilder()
				.mockGetMandatoryVariable(TENANT, TENANT)
				.mockGetMandatoryVariable(TENANT_WS, TENANT_WS)
				.mockGetMandatoryVariable(DRAFT_ORDER, DRAFT_ORDER_ID)
				.mockGetVariable(TASK_TELUS_WS_SOW_CREATE_ID, TASK_TELUS_WS_SOW_CREATE_ID)
				.build();

		unit.executeStep(execution);

		verify(orderSummaryHandler).attachOrderSummaryToTask(TENANT, TASK_TELUS_WS_SOW_CREATE_ID, DRAFT_ORDER_ID);
	}

	@Test
	@MockitoSettings(strictness = Strictness.LENIENT)
	void executestep_hasNotSowCreateId() throws Exception {
		execution = new ExecutionMockBuilder()
				.mockGetMandatoryVariable(TENANT, TENANT)
				.mockGetMandatoryVariable(DRAFT_ORDER, DRAFT_ORDER_ID)
				.mockGetMandatoryVariable(TENANT_WS, TENANT_WS)
				.build();

		when(execution.hasVariable(TASK_TELUS_WS_SOW_CREATE_ID)).thenReturn(false, true);
		when(execution.getVariable(TASK_TELUS_WS_SOW_CREATE_ID)).thenReturn(TASK_TELUS_WS_SOW_CREATE_ID);

		final TaskV2DomainTask task = mock(TaskV2DomainTask.class);
		when(task.getId()).thenReturn(TASK_TELUS_WS_SOW_CREATE_ID);
		when(task.getTaskDefinition()).thenReturn(TASK_DEFINITION);


		when(sowCreationTaskHandler.createSowCreationTask(TENANT, TENANT_WS, DRAFT_ORDER_ID)).thenReturn(task);

		unit.executeStep(execution);

		verifySetTaskInfo(TASK_TELUS_WS_SOW_CREATE_ID, TASK_DEFINITION, TENANT_WS, true);
		verify(orderSummaryHandler).attachOrderSummaryToTask(TENANT, TASK_TELUS_WS_SOW_CREATE_ID, DRAFT_ORDER_ID);
	}


	@Test
	public void getStepTenantType() {
		expectStepTenantType(unit, TenantType.WHOLESALE);
	}

	@Test
	public void hasRetry() {
		expectHasRetry(unit, true);
	}
}