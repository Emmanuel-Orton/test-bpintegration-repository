package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.create;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.TaskAttachmentsHandler;
import helpers.ExecutionMockBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.reset;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSowUpdateTaskStepTest extends AbstractWorkflowStepTest {

	private static final String TENANT_WS = "TENANT_WS";
	private static final String PREVIOUS_TASK_ID = "PREVIOUS_TASK_ID";
	private static final String PREVIOUS_TENANT = "PREVIOUS_TENANT";
	private static final String PREVIOUS_TASK_DEFINITION = "PREVIOUS_TASK_DEFINITION";
	private static final String CURRENT_TASK_ID = "CURRENT_TASK_ID";
	private static final String TASK_TELUS_WS_SOW_CREATE_ID = "TASK_TELUS_WS_SOW_CREATE_ID";
	private static final String NEW_TASK_FROM_OLD_ONE_TASK_ID = "NEW_TASK_FROM_OLD_ONE_TASK_ID";
	private static final String NEW_TASK_FROM_OLD_ONE_TASK_DEFINITION = "NEW_TASK_FROM_OLD_ONE_TASK_DEFINITION";

	@Mock
	BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;
	@Mock
	TaskAttachmentsHandler taskAttachmentsHandler;

	@InjectMocks
	CreateSowUpdateTaskStep unit;

	@BeforeEach
	public void setUp() {
		setObjectMapper(unit);
		reset(taskAttachmentsHandler);
	}

	@Test
	void executeStep_currentTask() throws Exception {
		execution = new ExecutionMockBuilder()
				.mockGetVariable(WF_PREVIOUS_TASK, WF_PREVIOUS_TASK)
				.mockGetMandatoryVariable(TENANT_WS, TENANT_WS)
				.mockGetMandatoryVariable(WF_CURRENT_TASK, WF_CURRENT_TASK)
				.build();

		final TaskInfo previousTask = mock(TaskInfo.class);
		when(workflowUtil.getTaskInfo(WF_PREVIOUS_TASK)).thenReturn(previousTask);
		when(previousTask.getTaskId()).thenReturn(PREVIOUS_TASK_ID);
		when(previousTask.getTenant()).thenReturn(PREVIOUS_TENANT);
		when(previousTask.getTaskDefinition()).thenReturn(PREVIOUS_TASK_DEFINITION);

		final TaskInfo currentTask = mock(TaskInfo.class);
		when(workflowUtil.getTaskInfo(WF_CURRENT_TASK)).thenReturn(currentTask);
		when(currentTask.getTaskId()).thenReturn(CURRENT_TASK_ID);
		when(currentTask.getTaskDefinition()).thenReturn(TaskType.TELUS_WS_SOW_UPDATE.name());

		unit.executeStep(execution);


		verify(taskAttachmentsHandler, times(1)).copyFirstAvailableAttachment(
				PREVIOUS_TENANT,
				TENANT_WS,
				PREVIOUS_TASK_ID,
				CURRENT_TASK_ID,
				PREVIOUS_TASK_DEFINITION,
				Map.of(TaskType.TELUS_WS_SOW_REVIEW, TaskAttachmentsHandler.SOW_DEFAULT_ATTACHMENTS_TO_COPY,
						TaskType.TELUS_WS_SOW_APPROVE, TaskAttachmentsHandler.SOW_DEFAULT_ATTACHMENTS_TO_COPY));

		verifySetTaskInfo(CURRENT_TASK_ID, TaskType.TELUS_WS_SOW_UPDATE.name(), TENANT_WS, true);
	}

	@Test
	void executeStep_hasNoCurrentTask() throws Exception {
		execution = new ExecutionMockBuilder()
				.mockGetVariable(WF_PREVIOUS_TASK, WF_PREVIOUS_TASK)
				.mockGetMandatoryVariable(TENANT_WS, TENANT_WS)
				.mockGetMandatoryVariable(WF_CURRENT_TASK, WF_CURRENT_TASK)
				.build();

		final TaskInfo previousTask = mock(TaskInfo.class);
		when(workflowUtil.getTaskInfo(WF_PREVIOUS_TASK)).thenReturn(previousTask);
		when(previousTask.getTaskId()).thenReturn(PREVIOUS_TASK_ID);

		final TaskInfo currentTask = mock(TaskInfo.class);
		when(workflowUtil.getTaskInfo(WF_CURRENT_TASK)).thenReturn(RETURN_NULL_FIRST_TIME, currentTask);
		when(currentTask.getTaskId()).thenReturn(CURRENT_TASK_ID);
		when(currentTask.getTaskDefinition()).thenReturn(TaskType.TELUS_WS_SOW_APPROVE.name());


		final TaskV2DomainTask newTaskFromOldOne = mock(TaskV2DomainTask.class);
		when(newTaskFromOldOne.getId()).thenReturn(NEW_TASK_FROM_OLD_ONE_TASK_ID);
		when(newTaskFromOldOne.getTaskDefinition()).thenReturn(NEW_TASK_FROM_OLD_ONE_TASK_DEFINITION);

		when(basedOnTheOtherTaskHandler.createNewTaskFromOldOneWithClosingNote(
				PREVIOUS_TASK_ID,
				TENANT_WS,
				previousTask,
				TaskType.TELUS_WS_SOW_UPDATE
		)).thenReturn(newTaskFromOldOne);

		unit.executeStep(execution);

		verify(basedOnTheOtherTaskHandler, times(1)).createNewTaskFromOldOneWithClosingNote(
				PREVIOUS_TASK_ID,
				TENANT_WS,
				previousTask,
				TaskType.TELUS_WS_SOW_UPDATE
		);

		verifySetTaskInfo(CURRENT_TASK_ID, TaskType.TELUS_WS_SOW_APPROVE.name(), TENANT_WS, true);
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