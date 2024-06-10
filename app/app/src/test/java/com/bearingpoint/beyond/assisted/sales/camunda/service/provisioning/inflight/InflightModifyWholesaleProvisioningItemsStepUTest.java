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
public class InflightModifyWholesaleProvisioningItemsStepUTest extends AbstractWorkflowStepTest {

    private static final String WS_ORDER = "7167099";
    private static final String MODIFIED_ORDER_ITEMS = "4=3, 6=2";

    @Mock
    ProvisioningHandler provisioningHandler;

    @InjectMocks
    InflightModifyWholesaleProvisioningItemsStep unitToTest;

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
    public void executeStep_modifyItemsFound() throws Exception {

        expectGetVariable(WF_MODIFIED_ORDER_ITEMS, MODIFIED_ORDER_ITEMS);
        expectGetMandatoryVariable(WF_WS_ORDER, WS_ORDER);

        Set<String> modifyIds = new HashSet<String>(Arrays.asList(MODIFIED_ORDER_ITEMS.split(", ")));

        doNothing().when(provisioningHandler)
                .inflightModifyWholesaleProvisioningItems(WS_ORDER, modifyIds);

        unitToTest.executeStep(execution);
    }


    @Test
    public void executeStep_noModifyItemsFound() throws Exception {
        expectGetVariable(WF_MODIFIED_ORDER_ITEMS, "");
        unitToTest.executeStep(execution);
    }


}