package com.bearingpoint.beyond.test-bpintegration.camunda.service.co;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.api.TasksV2Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.ExecutionMockBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderModificationTaskStepTest extends AbstractWorkflowStepTest {

	private static final String TENANT_KEY = "TENANT";
	private static final String TENANT_VAL = "TENANT_RT";
	private static final String TASK_TELUS_RT_CO_CREATION_ID = "TASK_TELUS_RT_CO_CREATION_ID";
	private static final String BILLING_ACCOUNT_ID = "TELUS_RT_ORDER_MODIFICATION_TASK_ID";
	private static final String TELUS_RT_ORDER_MODIFICATION_TASK_ID = "TELUS_RT_ORDER_MODIFICATION_TASK_ID";

	@Mock
	private InfonovaLinkService infonovaLinkService;
	@Mock
	private TasksV2Api tasksV2Api;

	private CreateOrderModificationTaskStep createOrderModificationTaskStep;


	@BeforeEach
	void setUp() {
		ObjectMapper mapper = new ObjectMapper();
		TasksService tasksService = new TasksService(tasksV2Api, mapper);
		BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler = new BasedOnTheOtherTaskHandler(tasksService,
				infonovaLinkService);
		createOrderModificationTaskStep = new CreateOrderModificationTaskStep(mapper, workflowUtil,
				basedOnTheOtherTaskHandler);
	}

	@Test
	void hasRetry() {
		assertThat(createOrderModificationTaskStep.hasRetry()).isTrue();
	}

	@Test
	void executeStepNewTask() throws Exception {
		execution = new ExecutionMockBuilder()
				.mockGetVariable(TASK_TELUS_RT_CO_CREATION_ID, TASK_TELUS_RT_CO_CREATION_ID)
				.mockGetMandatoryVariable(TENANT_KEY, TENANT_VAL)
				.build();

		when(tasksV2Api.getTasksV2TasksId(TENANT_VAL, TASK_TELUS_RT_CO_CREATION_ID))
				.thenReturn(Mono.just(new TaskV2DomainTask()
						.billingAccount(BILLING_ACCOUNT_ID)));

		when(tasksV2Api.postTasksV2Tasks(eq(TENANT_VAL), any(TaskV2DomainTask.class), any(String.class)))
				.thenReturn(Mono.just(new TaskV2DomainTask()
						.id(TELUS_RT_ORDER_MODIFICATION_TASK_ID)
						.taskDefinition(TaskType.TELUS_RT_ORDER_MODIFICATION.name())));

		when(infonovaLinkService.getCorrectTenant(TenantType.RETAIL)).thenReturn(TENANT_VAL);

		createOrderModificationTaskStep.executeStep(execution);

		verifySetTaskInfo(TELUS_RT_ORDER_MODIFICATION_TASK_ID, TaskType.TELUS_RT_ORDER_MODIFICATION.name(), TENANT_VAL,
				true);
	}

	@Test
	void getStepTenantType() {
		assertThat(createOrderModificationTaskStep.getStepTenantType()).isEqualTo(TenantType.RETAIL);
	}
}