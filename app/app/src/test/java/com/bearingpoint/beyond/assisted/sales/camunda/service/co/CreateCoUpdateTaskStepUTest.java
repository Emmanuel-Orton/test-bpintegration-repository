package com.bearingpoint.beyond.test-bpintegration.camunda.service.co;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.co.CreateWsCoReviewTaskStep.TASK_TELUS_WS_CO_REVIEW_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CreateCoUpdateTaskStepUTest extends AbstractWorkflowStepTest {

    private static final String CURRENT_TENANT = "SB_TELUS_SIT_WS";
    private static final String PREVIOUS_TASK_ID = "88748573";
    private static final String TASK_ID = "88748574";


    @Mock
    BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;

    @InjectMocks
    CreateCoUpdateTaskStep unitToTest;

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

    public void executeStep() throws Exception {

        TaskInfo taskInfoPrev = TaskInfo.builder()
                .taskId(PREVIOUS_TASK_ID)
                .tenant(CURRENT_TENANT).build();

        expectGetMandatoryVariable(WF_PREVIOUS_TASK, objectMapper.writeValueAsString(taskInfoPrev));
        when(workflowUtil.getTaskInfo(objectMapper.writeValueAsString(taskInfoPrev))).thenReturn(taskInfoPrev);
        expectGetMandatoryVariable(WF_TENANT_WS, CURRENT_TENANT);
        expectGetMandatoryVariable(TASK_TELUS_WS_CO_REVIEW_ID, TASK_ID);

        when(basedOnTheOtherTaskHandler.createNewTaskFromOldOneWithClosingNote(
                TASK_ID,
                CURRENT_TENANT,
                taskInfoPrev,
                TaskType.TELUS_WS_CO_UPDATE
        )).thenReturn(new TaskV2DomainTask().id(TASK_ID).taskDefinition(TaskType.TELUS_WS_CO_UPDATE.name()));

        unitToTest.executeStep(execution);

        verifySetTaskInfo(TASK_ID, TaskType.TELUS_WS_CO_UPDATE.name(), CURRENT_TENANT, false);
    }




}