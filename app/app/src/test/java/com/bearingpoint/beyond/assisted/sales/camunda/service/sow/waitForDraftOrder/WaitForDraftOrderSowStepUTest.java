package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.waitForDraftOrder;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.cleanup.CleanSowWorkflowStep;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class WaitForDraftOrderSowStepUTest extends AbstractWorkflowStepTest {

    @InjectMocks
    WaitForDraftOrderSowStep unitToTest;

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
        expectStepTenantType(unitToTest, TenantType.RETAIL);
    }

    @Disabled
    @Test
    public void executeStep_success() {
        // TODO Maria
    }

    @Disabled
    @Test
    public void executeStep_timeouted() {
        // TODO Maria
    }


}