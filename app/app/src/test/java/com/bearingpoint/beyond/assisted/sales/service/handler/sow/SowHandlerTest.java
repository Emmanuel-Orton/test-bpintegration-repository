package com.bearingpoint.beyond.test-bpintegration.service.handler.sow;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowStatus;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowTypes;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.create.CreateSowCreateTaskStep;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_IS_RESTARTED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TASK_RESOLUTION_ACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SowHandlerTest {

	private static final String DRAFT_ORDER = "draft_order";
	private static final String BUSINESS_KEY = "business_key";
	private static final String PROCESS_INSTANCE_ID = "341451";

	@Mock
	WorkflowUtil workflowUtil;

	@Mock
	RuntimeService runtimeService;

	@InjectMocks
    SowHandler unit;

	@Test
	void startWorkflow_processInstanceExists() {
		final ProcessInstance processInstance = mock(ProcessInstance.class);
		when(processInstance.getId()).thenReturn(PROCESS_INSTANCE_ID);
		when(workflowUtil.getWorkflow(DRAFT_ORDER)).thenReturn(processInstance);

		final MessageCorrelationBuilder mcb = mock(MessageCorrelationBuilder.class, Mockito.RETURNS_DEEP_STUBS);
		when(runtimeService.createMessageCorrelation("SOW_Restart_workflow")).thenReturn(mcb);
		when(mcb.processInstanceBusinessKey(DRAFT_ORDER)).thenReturn(mcb);
		when(mcb.setVariable(eq(WF_TASK_RESOLUTION_ACTION), isNull())).thenReturn(mcb);
		when(mcb.setVariable(WF_IS_RESTARTED, true)).thenReturn(mcb);

		unit.startWorkflow(DRAFT_ORDER);

		verify(runtimeService, times(1)).removeVariable(PROCESS_INSTANCE_ID,
				CreateSowCreateTaskStep.TASK_TELUS_WS_SOW_CREATE_ID);
		verify(runtimeService, times(1)).createMessageCorrelation("SOW_Restart_workflow");
		verify(mcb, times(1)).processInstanceBusinessKey(DRAFT_ORDER);
		verify(mcb, times(1)).setVariable(eq(WF_TASK_RESOLUTION_ACTION), isNull());
		verify(mcb, times(1)).setVariable(WF_IS_RESTARTED, true);
		verify(mcb, times(1)).correlateWithResult();

		verify(workflowUtil, times(0)).startSowWorkflow(anyString());
	}

	@Test
	void startWorkflow_processInstanceNotExists() {
		unit.startWorkflow(DRAFT_ORDER);

		verify(runtimeService, never()).removeVariable(anyString(), anyString());
		verify(runtimeService, never()).createMessageCorrelation(anyString());
		verify(workflowUtil, times(1)).startSowWorkflow(DRAFT_ORDER);
	}

	private static Stream<Arguments> provideParamsForGetWorkflowStatus_workflowStatusIsRunning() {
		return Stream.of(
				Arguments.of("sowEventRestartOrderVerification", true),
				Arguments.of("sowEventRestartWsSowUpdate", true),
				Arguments.of("Other", false),
				Arguments.of("", false)
		);
	}

	@ParameterizedTest
	@MethodSource("provideParamsForGetWorkflowStatus_workflowStatusIsRunning")
	void getWorkflowStatus_workflowStatusIsRunning(String stepId, Boolean canRestart) {
		final WorkflowStatus workflowStatus = mock(WorkflowStatus.class);
		when(workflowStatus.isRunning()).thenReturn(true);
		when(workflowStatus.getStepId()).thenReturn(stepId);
		when(workflowUtil.getWorkflowStatus(BUSINESS_KEY, WorkflowTypes.STATEMENT_OF_WORK)).thenReturn(workflowStatus);

		assertThat(unit.getWorkflowStatus(BUSINESS_KEY)).isEqualTo(workflowStatus);

		verify(workflowStatus, times(1)).setCanRestart(canRestart);
	}

	@Test
	void getWorkflowStatus_workflowStatusIsNotRunning() {
		final WorkflowStatus workflowStatus = mock(WorkflowStatus.class);
		when(workflowStatus.isRunning()).thenReturn(false);
		when(workflowUtil.getWorkflowStatus(BUSINESS_KEY, WorkflowTypes.STATEMENT_OF_WORK)).thenReturn(workflowStatus);

		assertThat(unit.getWorkflowStatus(BUSINESS_KEY)).isEqualTo(workflowStatus);
	}
}
