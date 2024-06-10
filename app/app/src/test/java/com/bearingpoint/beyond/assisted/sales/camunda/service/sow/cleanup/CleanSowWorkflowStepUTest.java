package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.cleanup;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.CleanupHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep.WF_CANCELLED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CleanSowWorkflowStepUTest extends AbstractWorkflowStepTest {

    protected static final String TENANT_VAL = "TENANT_RT";
    private static final String TASK_DEFINITION = "TASK_DEFINITION";
    private static final String TASK_ID = "TASK_ID";
    @Mock
    CleanupHandler cleanupHandler;
    @InjectMocks
    CleanSowWorkflowStep unitToTest;

    @BeforeEach
    public void setUp() {
        setObjectMapper(unitToTest);
    }

    @Test
    public void hasRetry() {
        expectHasRetry(unitToTest, false);
    }


    @Test
    public void executeStep_hasNoCurrentTask() throws Exception {
        expectHasVariable(WF_CURRENT_TASK, false);

        unitToTest.executeStep(execution);

        Mockito.verify(execution, times(1)).setVariable(WF_CANCELLED, true);
    }


    @Test
    public void executeStep_currentTask() throws Exception {
        TaskInfo currentTaskInfo = new TaskInfo(TASK_ID, TASK_DEFINITION, TENANT_VAL);
        expectGetVariable(WF_CURRENT_TASK, currentTaskInfo);

        unitToTest.executeStep(execution);

        Mockito.verify(execution, times(1)).setVariable(WF_CANCELLED, true);
        Mockito.verify(cleanupHandler, times(1)).cleanupTask(any());
    }


    @Test
    public void getStepTenantType() {
        expectStepTenantType(unitToTest, TenantType.RETAIL);
    }
}