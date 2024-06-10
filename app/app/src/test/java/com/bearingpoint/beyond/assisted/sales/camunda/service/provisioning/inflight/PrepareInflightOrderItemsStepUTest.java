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

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PrepareInflightOrderItemsStepUTest extends AbstractWorkflowStepTest {

    private static final String CURRENT_TENANT = "SB_TELUS_SIT";
    private static final String RT_ORDER = "7167093";
    private static final String RT_CUSTOMER = "1192803532";
    private static final String WS_ORDER = "7167099";
    private static final String WS_PROVISIONING_ID = "1965282";
    private static final String WS_CUSTOMER = "874374734";
    private static final String ADDED_ORDER_ITEMS = "1, 2, 3";


    @Mock
    ProvisioningHandler provisioningHandler;

    @InjectMocks
    PrepareInflightOrderItemsStep unitToTest;

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
    public void executeStep_addItemsFound() throws Exception {

        expectGetMandatoryVariable(WF_ADDED_ORDER_ITEMS, ADDED_ORDER_ITEMS);
        expectGetMandatoryVariable(WF_RT_ORDER, RT_ORDER);
        expectGetMandatoryVariable(WF_RT_CUSTOMER, RT_CUSTOMER);
        expectGetMandatoryVariable(WF_WS_ORDER, WS_ORDER);
        expectGetMandatoryVariable(WF_WS_CUSTOMER, WS_CUSTOMER);
        expectGetMandatoryVariable(WF_WS_PROVISIONING_ID, WS_PROVISIONING_ID);
        expectGetMandatoryVariable(WF_CURRENT_TENANT, CURRENT_TENANT);

        doNothing().when(provisioningHandler)
                .createInflightWholesaleProvisioningDatabaseEntries(RT_ORDER,
                RT_CUSTOMER, WS_ORDER, WS_CUSTOMER, WS_PROVISIONING_ID, CURRENT_TENANT, ADDED_ORDER_ITEMS);

        unitToTest.executeStep(execution);
    }


    @Test
    public void executeStep_noAddItemsFound() throws Exception {
        expectGetMandatoryVariable(WF_ADDED_ORDER_ITEMS, "");
        unitToTest.executeStep(execution);
    }


}