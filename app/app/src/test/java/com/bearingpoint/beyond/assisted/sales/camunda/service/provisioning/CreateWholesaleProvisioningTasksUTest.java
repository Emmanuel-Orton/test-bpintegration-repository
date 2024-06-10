package com.bearingpoint.beyond.test-bpintegration.camunda.service.provisioning;


import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.provisioning.inflight.PrepareInflightOrderItemsStep;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CreateWholesaleProvisioningTasksUTest extends AbstractWorkflowStepTest {

    private static final String WS_ORDER = "7167099";


    @Mock
    ProvisioningHandler provisioningHandler;

    @InjectMocks
    CreateWholesaleProvisioningTasks unitToTest;

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
        expectGetMandatoryVariable(WF_WS_ORDER, WS_ORDER);

        doNothing().when(provisioningHandler)
                .raiseOrderServiceProvisioningTask(Long.parseLong(WS_ORDER));

        unitToTest.executeStep(execution);
    }

}

