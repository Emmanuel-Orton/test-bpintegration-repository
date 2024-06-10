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

import java.util.Locale;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class SetServiceOrderToInProgressStepUTest extends AbstractWorkflowStepTest {

    private static final String CURRENT_TENANT = "SB_TELUS_SIT";
    private static final String WS_ORDER = "7167099";
    private static final String WS_PROVISIONING_ID = "1965282";


    @Mock
    ProvisioningHandler provisioningHandler;

    @InjectMocks
    SetServiceOrderToInProgressStep unitToTest;

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
        expectGetMandatoryVariable(WF_WS_PROVISIONING_ID, WS_PROVISIONING_ID);
        expectGetMandatoryVariable(WF_CURRENT_TENANT, CURRENT_TENANT);

        doNothing().when(provisioningHandler)
                .setServiceOrderInProgress(WS_ORDER,
                        WS_PROVISIONING_ID,
                        CURRENT_TENANT);

        unitToTest.executeStep(execution);
    }

}