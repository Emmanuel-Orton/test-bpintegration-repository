package com.bearingpoint.beyond.test-bpintegration.camunda.service.mo;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.TaskAttachmentsHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.mo.ModifyOrderHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_RT_CUSTOMER_LEGAL_NAME;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_START_TASK_ID;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_CUSTOMER;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_ID;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.mo.CreateWsMoReviewTaskStep.TASK_TELUS_WS_MO_REVIEW_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateWsMoReviewTaskStepUTest extends AbstractWorkflowStepTest {

    private static final String WS_ORDER = "7167099";

    private static final String CURRENT_TENANT = "SB_TELUS_SIT";
    private static final String CURRENT_TENANT_WS = "SB_TELUS_SIT_WS";

    private static final String PREVIOUS_TASK_ID = "88748573";
    private static final Long TASK_ID = 88748574L;

    private static final String TASK_ID_NEW = "88748575";
    private static final String PREVIOUS_TASK_DEFINITION = "PREVIOUS_TASK_DEFINITION";

    private static final String WORKFLOW_ID_VALUE = "MO_88748574";
    private static final Long CUSTOMER_ID = 375734878L;
    private static final String CUSTOMER_LEGAL_NAME = "Customer Legal Name";


    @Mock
    ModifyOrderHandler modifyOrderHandler;
    @Mock
    BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;
    @Mock
    TaskAttachmentsHandler taskAttachmentsHandler;

    @InjectMocks
    CreateWsMoReviewTaskStep unitToTest;

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
    @Disabled
    public void executeStep_noReviewTaskFound() throws Exception {
        // TODO fix tests

        expectGetVariable("TASK_TELUS_WS_MO_REVIEW_ID", null);
        expectGetMandatoryVariable(WF_TENANT_WS, CURRENT_TENANT_WS);
        expectGetMandatoryVariable(WF_START_TASK_ID, TASK_ID);
        expectGetMandatoryVariable(WF_WS_CUSTOMER, CUSTOMER_ID);
        expectGetMandatoryVariable(WF_RT_CUSTOMER_LEGAL_NAME, CUSTOMER_LEGAL_NAME);
        expectGetMandatoryVariable(WORKFLOW_ID, WORKFLOW_ID_VALUE);

        when(execution.hasVariable(WF_PREVIOUS_TASK)).thenReturn(false, true);
        when(execution.hasVariable(WF_CURRENT_TASK)).thenReturn(false, true);

        final TaskInfo previousTask = mock(TaskInfo.class);
        when(previousTask.getTaskId()).thenReturn(TASK_ID.toString());
        when(previousTask.getTenant()).thenReturn(CURRENT_TENANT_WS);
        when(previousTask.getTaskDefinition()).thenReturn(TaskType.TELUS_WS_CO_DAY2_INITIATE.name());

        final TaskInfo currentTask = mock(TaskInfo.class);
        when(workflowUtil.getTaskInfo(WF_CURRENT_TASK)).thenReturn(currentTask);
        when(currentTask.getTaskId()).thenReturn(TASK_ID_NEW);
        when(currentTask.getTaskDefinition()).thenReturn(TaskType.TELUS_WS_CO_DAY2_REVIEW.name());

        when(workflowUtil.getTaskInfo(any())).thenReturn(null, previousTask, null, currentTask);

        when(modifyOrderHandler.createReviewTask(CURRENT_TENANT_WS, CUSTOMER_ID, TASK_ID.toString()))
                .thenReturn(new TaskV2DomainTask().id(TASK_ID_NEW).taskDefinition(TaskType.TELUS_WS_CO_DAY2_REVIEW.name()));
        unitToTest.executeStep(execution);

        verifySetVariable(TASK_TELUS_WS_MO_REVIEW_ID, TASK_ID_NEW);
        //verifySetTaskInfo(TASK_ID_NEW, TaskType.TELUS_WS_CO_DAY2_REVIEW.name(), CURRENT_TENANT_WS, true);
    }


    @Test
    @Disabled
    public void executeStep_reviewTaskFound() throws Exception {
        // TODO fix test
        expectGetVariable("TASK_TELUS_WS_MO_REVIEW_ID", TASK_ID.toString());
        expectGetMandatoryVariable(WF_TENANT_WS, CURRENT_TENANT_WS);
        expectGetMandatoryVariable(WF_START_TASK_ID, TASK_ID);

        when(execution.hasVariable(WF_PREVIOUS_TASK)).thenReturn(false, true);
        when(execution.hasVariable(WF_CURRENT_TASK)).thenReturn(false, true);

        final TaskInfo previousTask = mock(TaskInfo.class);
        when(previousTask.getTaskId()).thenReturn(TASK_ID.toString());
        when(previousTask.getTenant()).thenReturn(CURRENT_TENANT_WS);
        when(previousTask.getTaskDefinition()).thenReturn(TaskType.TELUS_WS_CO_DAY2_INITIATE.name());

        final TaskInfo currentTask = mock(TaskInfo.class);
        when(workflowUtil.getTaskInfo(WF_CURRENT_TASK)).thenReturn(currentTask);
        when(currentTask.getTaskId()).thenReturn(TASK_ID_NEW);
        when(currentTask.getTaskDefinition()).thenReturn(TaskType.TELUS_WS_CO_DAY2_REVIEW.name());

        when(workflowUtil.getTaskInfo(any())).thenReturn(null, previousTask, null, currentTask);

        when(basedOnTheOtherTaskHandler.createNewTaskFromOldOne(CURRENT_TENANT_WS,
                TASK_ID.toString(),
                TaskType.TELUS_WS_CO_DAY2_REVIEW)).thenReturn(new TaskV2DomainTask().id(TASK_ID_NEW));

        unitToTest.executeStep(execution);

        verifySetVariable(TASK_TELUS_WS_MO_REVIEW_ID, TASK_ID_NEW);
        //verifySetTaskInfo(TASK_ID_NEW, TaskType.TELUS_WS_CO_DAY2_REVIEW.name(), CURRENT_TENANT_WS, true);
    }
}

