package com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.main;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.DafStatus;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_ORDER;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_ID;
import static org.mockito.Mockito.doNothing;
import org.junit.jupiter.api.Disabled;

@Slf4j
@ExtendWith(MockitoExtension.class)
class FinalizeServiceOrderItemsStepUTest extends AbstractWorkflowStepTest {

    private static final String WORKFLOW_ID_DATA = "DAF_43748738748";

    @Mock
    DafHandler dafHandler;

    @InjectMocks
    FinalizeServiceOrderItemsStep unitToTest;

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
        expectGetMandatoryVariable(WORKFLOW_ID, WORKFLOW_ID_DATA);

        doNothing().when(dafHandler)
                .updateStatusOfItems(WORKFLOW_ID_DATA, DafStatus.COMPLETE);

        unitToTest.executeStep(execution);
    }

}

