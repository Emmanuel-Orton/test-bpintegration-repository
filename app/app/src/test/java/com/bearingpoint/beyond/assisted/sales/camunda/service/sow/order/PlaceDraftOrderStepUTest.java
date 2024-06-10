package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.order;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.sow.SowHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
import static com.bearingpoint.beyond.test-bpintegration.service.defs.TaskParameters.OPERATOR_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class PlaceDraftOrderStepUTest extends AbstractWorkflowStepTest {

    private static final String TENANT = "SB_TELUS_SIT";
    private static final String DRAFT_ORDER_ID = "84753875";
    private static final String OPERATOR_NAME_VALUE = "sb_telus_admin_sit";
    private static final String DRAFT_ORDER_PLACED_DT = "2023-01-11T10:07:10.380138Z";

    @Mock
    SowHandler sowHandler;

    @InjectMocks
    PlaceDraftOrderStep unitToTest;

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

    @Test
    public void executeStep() {

        expectGetMandatoryVariable(OPERATOR_NAME.name(), OPERATOR_NAME_VALUE);
        expectGetMandatoryVariable(WF_TENANT, TENANT);
        expectGetMandatoryVariable(WF_DRAFT_ORDER_PLACED_DT, DRAFT_ORDER_PLACED_DT);
        expectGetMandatoryVariable(WF_DRAFT_ORDER, DRAFT_ORDER_ID);

        doNothing().when(sowHandler)
                .placeOrderFromDraftOrder(DRAFT_ORDER_ID, TENANT, DRAFT_ORDER_PLACED_DT, OPERATOR_NAME_VALUE);

        unitToTest.executeStep(execution);
    }

}