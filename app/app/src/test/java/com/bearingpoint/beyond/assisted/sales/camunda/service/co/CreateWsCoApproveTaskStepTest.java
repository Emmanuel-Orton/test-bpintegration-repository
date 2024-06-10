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
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWsCoApproveTaskStepTest extends AbstractWorkflowStepTest {

	private static final String TASK_TELUS_WS_CO_REVIEW_ID_KEY = "TASK_TELUS_WS_CO_REVIEW_ID";
	private static final String TENANT_WS = "TENANT_WS";
	private static final String BILLING_ACCOUNT_ID = "BILLING_ACCOUNT_ID";
	private static final String TELUS_WS_CO_PARTNER_APPROVE_TASK_ID = "TELUS_WS_CO_PARTNER_APPROVE_TASK_ID";

	CreateWsCoApproveTaskStep createWsCoApproveTaskStep;
	@Mock
	private InfonovaLinkService infonovaLinkService;
	@Mock
	private TasksV2Api tasksV2Api;

	@BeforeEach
	void setUp() {
		ObjectMapper mapper = new ObjectMapper();
		TasksService tasksService = new TasksService(tasksV2Api, mapper);
		BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler = new BasedOnTheOtherTaskHandler(tasksService,
				infonovaLinkService);
		createWsCoApproveTaskStep = new CreateWsCoApproveTaskStep(mapper, workflowUtil, basedOnTheOtherTaskHandler);
	}

	@Test
	void hasRetry() {
		assertThat(createWsCoApproveTaskStep.hasRetry()).isTrue();
	}

	@Test
	void executeStepNewTask() throws Exception {
		execution = new ExecutionMockBuilder()
				.mockGetVariable(TASK_TELUS_WS_CO_REVIEW_ID_KEY, TASK_TELUS_WS_CO_REVIEW_ID_KEY)
				.mockGetMandatoryVariable(TENANT_WS, TENANT_WS)
				.build();

		when(tasksV2Api.getTasksV2TasksId(TENANT_WS, TASK_TELUS_WS_CO_REVIEW_ID_KEY))
				.thenReturn(Mono.just(new TaskV2DomainTask()
						.billingAccount(BILLING_ACCOUNT_ID)));

		when(tasksV2Api.postTasksV2Tasks(eq(TENANT_WS), any(TaskV2DomainTask.class), any(String.class)))
				.thenReturn(Mono.just(new TaskV2DomainTask()
						.id(TELUS_WS_CO_PARTNER_APPROVE_TASK_ID)
						.taskDefinition(TaskType.TELUS_WS_CO_PARTNER_APPROVE.name())));

		when(infonovaLinkService.getCorrectTenant(TenantType.WHOLESALE)).thenReturn(TENANT_WS);

		createWsCoApproveTaskStep.executeStep(execution);

		verifySetTaskInfo(TELUS_WS_CO_PARTNER_APPROVE_TASK_ID, TaskType.TELUS_WS_CO_PARTNER_APPROVE.name(), TENANT_WS,
				true);
	}

	@Test
	void getStepTenantType() {
		AssertionsForClassTypes.assertThat(createWsCoApproveTaskStep.getStepTenantType())
				.isEqualTo(TenantType.WHOLESALE);
	}
}