package com.bearingpoint.beyond.test-bpintegration.camunda.service.mo;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.mo.ModifyOrderHandler;
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
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CreateInitiateChangeOrderTaskStepUTest extends AbstractWorkflowStepTest {


    private static final String WS_ORDER = "7167099";

    private static final String CURRENT_TENANT = "SB_TELUS_SIT";
    private static final String CURRENT_TENANT_WS = "SB_TELUS_SIT_WS";

    private static final String PREVIOUS_TASK_ID = "88748573";
    private static final Long TASK_ID = 88748574L;

    private static final String TASK_ID_NEW = "88748575";
    private static final String PREVIOUS_TASK_DEFINITION = "PREVIOUS_TASK_DEFINITION";

    private static final String WORKFLOW_ID_VALUE = "MO_1192598181_b4b3aed47cf343e19d6bfa3d44ab68d0";
    private static final Long CUSTOMER_ID = 375734878L;
    private static final String CUSTOMER_LEGAL_NAME = "Customer Legal Name";
    private static final String SERVICE_INSTANCE_NAME = "1192598181_b4b3aed47cf343e19d6bfa3d44ab68d0";
    private static final String SERVICE_NAME = "WSO Monitor It Service";
    private static final String CSM_USER = "jolera_user";

    public static final Long WS_ORDER_ID_LONG = 12345L;



    @Mock
    ModifyOrderHandler modifyOrderHandler;

    @InjectMocks
    CreateInitiateChangeOrderTaskStep unitToTest;

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
        // TODO fix test
        expectGetMandatoryVariable(WF_TENANT_WS, CURRENT_TENANT_WS);
        expectGetMandatoryVariable(WF_WS_CUSTOMER, CUSTOMER_ID);
        expectGetMandatoryVariable(WF_RT_CUSTOMER_LEGAL_NAME, CUSTOMER_LEGAL_NAME);
        expectGetMandatoryVariable(WF_SERVICE_INSTANCE_NAME, SERVICE_INSTANCE_NAME);
        expectGetMandatoryVariable(WF_CSM_USER, CSM_USER);
        expectGetMandatoryVariable(WORKFLOW_ID, WORKFLOW_ID_VALUE);

        when(modifyOrderHandler.createInitiateMoTask(
                CUSTOMER_ID,
                CURRENT_TENANT_WS,
                CUSTOMER_LEGAL_NAME,
                SERVICE_INSTANCE_NAME,
                SERVICE_NAME,
                CSM_USER,
                WORKFLOW_ID_VALUE
        )).thenReturn(new TaskV2DomainTask().id(TASK_ID_NEW).taskDefinition(TaskType.TELUS_WS_CO_DAY2_INITIATE.name()));

        unitToTest.executeStep(execution);

        verifySetVariable(WF_START_TASK_ID, TASK_ID_NEW);
        verifySetTaskInfo(TASK_ID_NEW, TaskType.TELUS_WS_CO_DAY2_INITIATE.name(), CURRENT_TENANT_WS, false);
    }



}
