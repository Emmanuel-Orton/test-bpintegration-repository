package com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_ORDER;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve.CreateDafApproveTaskStep.WF_DAF_APPROVAL_TASK_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Disabled;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Date;
import java.util.HashMap;

@Slf4j
@ExtendWith(MockitoExtension.class)
class SendApprovalWarningStepUTest extends AbstractWorkflowStepTest {

    private static final String DAF_APPROVAL_TASK_ID_VALUE = "37487248";
    private static final String CURRENT_TENANT = "SB_TELUS_SIT";

    @Mock
    TasksService tasksService;

    @InjectMocks
    SendApprovalWarningStep unitToTest;

    @BeforeEach
    public void setUp() {
        setObjectMapper(unitToTest);
    }

    @Test
    public void hasRetry() {
        expectHasRetry(unitToTest, true);
    }


    @Test
    public void getStepTenantType() {
        expectStepTenantType(unitToTest, TenantType.WHOLESALE);
    }


    @Test
    public void executeStep_approvalTaskIdNotFound() throws Exception {
        expectGetVariableNotFound(WF_DAF_APPROVAL_TASK_ID);
        unitToTest.executeStep(execution);
    }

    @Test
    public void executeStep_taskNotFound()  throws Exception {
        expectGetVariable(WF_DAF_APPROVAL_TASK_ID, DAF_APPROVAL_TASK_ID_VALUE);
        expectGetMandatoryVariable(WF_TENANT_WS, CURRENT_TENANT);

        when(tasksService.getTask(CURRENT_TENANT, DAF_APPROVAL_TASK_ID_VALUE))
                .thenThrow(new WebClientResponseException(401, "Unknown Error", null, null, null));

        unitToTest.executeStep(execution);
    }

    @Test
    public void executeStep_taskFoundInResolvedState()  throws Exception {

        expectGetVariable(WF_DAF_APPROVAL_TASK_ID, DAF_APPROVAL_TASK_ID_VALUE);
        expectGetMandatoryVariable(WF_TENANT_WS, CURRENT_TENANT);

        when(tasksService.getTask(CURRENT_TENANT, DAF_APPROVAL_TASK_ID_VALUE))
                .thenReturn(new TaskV2DomainTask().state("Resolved"));

        unitToTest.executeStep(execution);
    }

    @Test
    public void executeStep_taskFoundInProgressState_updateFailed()  throws Exception {

        expectGetVariable(WF_DAF_APPROVAL_TASK_ID, DAF_APPROVAL_TASK_ID_VALUE);
        expectGetMandatoryVariable(WF_TENANT_WS, CURRENT_TENANT);

        when(tasksService.getTask(CURRENT_TENANT, DAF_APPROVAL_TASK_ID_VALUE))
                .thenReturn(new TaskV2DomainTask().state("InProgress").parameters(new HashMap<>()));

        doThrow(new WebClientResponseException(401, "Unknown Error", null, null, null))
                .when(tasksService)
                .updateTask(eq(CURRENT_TENANT), any(TaskV2DomainTask.class));

        unitToTest.executeStep(execution);
    }

    @Test
    public void executeStep_taskFoundAndUpdated()  throws Exception {
        expectGetVariable(WF_DAF_APPROVAL_TASK_ID, DAF_APPROVAL_TASK_ID_VALUE);
        expectGetMandatoryVariable(WF_TENANT_WS, CURRENT_TENANT);

        when(tasksService.getTask(CURRENT_TENANT, DAF_APPROVAL_TASK_ID_VALUE))
                .thenReturn(new TaskV2DomainTask().state("InProgress").parameters(new HashMap<>()));

        doNothing().when(tasksService)
                .updateTask(eq(CURRENT_TENANT), any(TaskV2DomainTask.class));

        unitToTest.executeStep(execution);
    }

}

