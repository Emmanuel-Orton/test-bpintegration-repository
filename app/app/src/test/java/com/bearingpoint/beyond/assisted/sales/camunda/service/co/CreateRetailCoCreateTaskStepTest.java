package com.bearingpoint.beyond.test-bpintegration.camunda.service.co;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.service.handler.co.ChangeOrderHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.ExecutionMockBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CreateRetailCoCreateTaskStepTest extends AbstractWorkflowStepTest {

    private static final String RT_TENANT = "RT_TENANT";
    private static final String TASK_ID = "TASK_ID";
    private static final String TENANT_WS = "TENANT_WS";
    private static final String WS_TENANT = "WS_TENANT";
    private static final String WS_ORDER_KEY = "WS_ORDER";
    private static final Long WS_ORDER_VAL = 21823L;

    CreateRetailCoCreateTaskStep createRetailCoCreateTaskStep;

    ChangeOrderHandler changeOrderHandler;

    WorkflowUtil workflowUtil;

    @BeforeEach
    public void setUp() {
        changeOrderHandler = mock(ChangeOrderHandler.class);
        final ObjectMapper objectMapper = new ObjectMapper();
        workflowUtil = mock(WorkflowUtil.class);
        createRetailCoCreateTaskStep = new CreateRetailCoCreateTaskStep(objectMapper, workflowUtil, changeOrderHandler);
    }

    @Test
    public void hasRetry() {
        expectHasRetry(createRetailCoCreateTaskStep, true);
    }

    @Test
    public void getStepTenantType() {
        expectStepTenantType(createRetailCoCreateTaskStep, TenantType.RETAIL);
    }

    @Test
    public void executeStepNewTask() throws Exception {
        execution = new ExecutionMockBuilder()
                .mockGetVariable(WF_TENANT, RT_TENANT)
                .mockGetVariable(TENANT_WS, WS_TENANT)
                .mockGetVariable(WS_ORDER_KEY, WS_ORDER_VAL)
                .mockGetVariable(WORKFLOW_ID, WORKFLOW_ID)
                .build();


        final TaskV2DomainTask task = mock(TaskV2DomainTask.class);
        when(changeOrderHandler.createRetailCOCreateTask(
                RT_TENANT,
                WS_TENANT,
                WS_ORDER_VAL,
                WORKFLOW_ID
        )).thenReturn(task);
        when(task.getId()).thenReturn(TASK_ID);
        when(task.getTaskDefinition()).thenReturn(TaskType.TELUS_RT_CO_CREATION.name());

        createRetailCoCreateTaskStep.executeStep(execution);

        verifySetTaskInfo(TASK_ID, TaskType.TELUS_RT_CO_CREATION.name(), RT_TENANT, true);
    }
}
