package com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_ORDER;
import static org.mockito.Mockito.doNothing;
import org.junit.jupiter.api.Disabled;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CreateDafApproveTaskStepUTest extends AbstractWorkflowStepTest {

    private static final String WS_ORDER = "7167099";
    @Mock
    DafHandler dafHandler;


    @Mock
    ProvisioningHandler provisioningHandler;
    @Mock
    TaskService taskService;

    @InjectMocks
    CreateDafApproveTaskStep unitToTest;

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
    public void executeStep() throws Exception {
        expectGetMandatoryVariable(WF_WS_ORDER, WS_ORDER);

        doNothing().when(provisioningHandler)
                .raiseOrderServiceProvisioningTask(Long.parseLong(WS_ORDER));
// TODO Andy implement
        unitToTest.executeStep(execution);
    }

}

