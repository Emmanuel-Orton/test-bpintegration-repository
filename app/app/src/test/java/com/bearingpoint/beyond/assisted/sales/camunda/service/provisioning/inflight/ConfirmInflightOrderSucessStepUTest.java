package com.bearingpoint.beyond.test-bpintegration.camunda.service.provisioning.inflight;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_CURRENT_TENANT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ConfirmInflightOrderSucessStepUTest extends AbstractWorkflowStepTest {

    private static final String CURRENT_TENANT = "SB_TELUS_SIT";
    private static final String WS_PROVISIONING_ID = "1965282";

    @Mock
    ProvisioningHandler provisioningHandler;

    @InjectMocks
    ConfirmInflightOrderSucessStep unitToTest;

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
        expectGetMandatoryVariable(WF_WS_PROVISIONING_ID, WS_PROVISIONING_ID);
        expectGetMandatoryVariable(WF_CURRENT_TENANT, CURRENT_TENANT);

        doNothing().when(provisioningHandler).completeInflightOrder(
                WS_PROVISIONING_ID,
                CURRENT_TENANT);

        unitToTest.executeStep(execution);
    }


}
