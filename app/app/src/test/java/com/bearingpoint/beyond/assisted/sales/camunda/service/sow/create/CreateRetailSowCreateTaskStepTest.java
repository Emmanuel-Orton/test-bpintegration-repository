package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.create;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.sow.RetailSowCreationTaskHandler;
import helpers.ExecutionMockBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateRetailSowCreateTaskStepTest extends AbstractWorkflowStepTest {

    private static final String DRAFT_ORDER = "DRAFT_ORDER";
    private static final String NEW_TASK_FROM_OLD_ONE_TASK_ID = "NEW_TASK_FROM_OLD_ONE_TASK_ID";
    private static final String TENANT_WS = "TENANT_WS";
    private static final String CURRENT_TASK_ID = "crntTaskId";
    private static final String WS_SOW_REVIEW_TASK_ID = "WS_SOW_REVIEW_TASK_ID";
    private static final String TENANT = "TENANT";

    @Mock
    RetailSowCreationTaskHandler retailSowCreationTaskHandler;

    @InjectMocks
    CreateRetailSowCreateTaskStep unit;

    @BeforeEach
    public void setUp() {
        setObjectMapper(unit);
    }

    @Test
    void executeStep_currentTask() throws Exception {
        execution = new ExecutionMockBuilder()
                .mockGetMandatoryVariable(TENANT, TENANT_WS)
                .mockGetMandatoryVariable(TENANT_WS, TENANT_WS)
                .mockGetMandatoryVariable(DRAFT_ORDER, DRAFT_ORDER)
                .mockGetMandatoryVariable(WS_SOW_REVIEW_TASK_ID, WS_SOW_REVIEW_TASK_ID)
                .build();

        final TaskV2DomainTask task = mock(TaskV2DomainTask.class);
        when(task.getId()).thenReturn(CURRENT_TASK_ID);
        when(task.getTaskDefinition()).thenReturn(TaskType.TELUS_WS_SOW_APPROVE.name());
        when(retailSowCreationTaskHandler.createRetailSowCreationTask(TENANT_WS, TENANT_WS, DRAFT_ORDER, WS_SOW_REVIEW_TASK_ID))
                .thenReturn(task);
        unit.executeStep(execution);
        verifySetTaskInfo(CURRENT_TASK_ID, TaskType.TELUS_WS_SOW_APPROVE.name(), TENANT_WS, true);
    }

    @Test
    void executeStep_hasNoCurrentTask() throws Exception {
        execution = new ExecutionMockBuilder()
                .mockGetMandatoryVariable(TENANT, TENANT_WS)
                .mockGetMandatoryVariable(TENANT_WS, TENANT_WS)
                .mockGetMandatoryVariable(DRAFT_ORDER, DRAFT_ORDER)
                .mockGetMandatoryVariable(WS_SOW_REVIEW_TASK_ID, WS_SOW_REVIEW_TASK_ID)
                .build();


        final TaskV2DomainTask newTaskFromOldOne = mock(TaskV2DomainTask.class);
        when(newTaskFromOldOne.getId()).thenReturn(NEW_TASK_FROM_OLD_ONE_TASK_ID);
        when(newTaskFromOldOne.getTaskDefinition()).thenReturn(TaskType.TELUS_WS_SOW_APPROVE.name());

        when(retailSowCreationTaskHandler.createRetailSowCreationTask(TENANT_WS, TENANT_WS, DRAFT_ORDER, WS_SOW_REVIEW_TASK_ID))
                .thenReturn(newTaskFromOldOne);

        unit.executeStep(execution);

        verify(retailSowCreationTaskHandler, times(1))
                .createRetailSowCreationTask(TENANT_WS, TENANT_WS, DRAFT_ORDER, WS_SOW_REVIEW_TASK_ID);
        verifySetTaskInfo(NEW_TASK_FROM_OLD_ONE_TASK_ID, TaskType.TELUS_WS_SOW_APPROVE.name(), TENANT_WS, true);
    }

    @Test
    public void getStepTenantType() {
        expectStepTenantType(unit, TenantType.RETAIL);
    }

    @Test
    public void hasRetry() {
        expectHasRetry(unit, true);
    }
}
