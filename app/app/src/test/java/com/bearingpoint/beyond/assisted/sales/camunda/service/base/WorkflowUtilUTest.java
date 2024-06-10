package com.bearingpoint.beyond.test-bpintegration.camunda.service.base;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.ModifyOrderStartParameters;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowTypes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowUtilUTest {


    private static final String CURRENT_TENANT = "SB_TELUS_SIT";
    private static final String RT_ORDER = "7167093";
    private static final Long RT_CUSTOMER_ID = 1192803532L;
    private static final String RT_CUSTOMER_LEGAL_NAME = "Customer Legal Name";

    private static final String WS_ORDER = "7167099";
    private static final String WS_PROVISIONING_ID = "1965282";
    private static final Long WS_CUSTOMER_ID = 874374734L;
    private static final String ADDED_ORDER_ITEMS = "1, 2, 3";
    private static final String PREVIOUS_TASK_ID = "88748573";
    private static final String TASK_ID = "88748574";
    private static final String SERVICE_INSTANCE_NAME = "1192598181_b4b3aed47cf343e19d6bfa3d44ab68d0";

    @Mock
    RuntimeService runtimeService;
    @Mock
    ProcessInstanceQuery processInstanceQuery;
    @Mock
    ProcessInstance processInstance;

    protected ObjectMapper objectMapper;

    @InjectMocks
    WorkflowUtil unitToTest;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(unitToTest, "objectMapper", objectMapper);
    }


    @Test
    public void  startModifyOrderWorkflow() throws Exception {

        startProcessInstance(WorkflowTypes.MODIFY_ORDER, "MO_");

        expectWorkflowExists("MO_" + SERVICE_INSTANCE_NAME, false);

        String id = unitToTest.startModifyOrderWorkflow(CURRENT_TENANT, ModifyOrderStartParameters.builder()
                .wholesaleBillingAccountId(WS_CUSTOMER_ID)
                .serviceInstanceName(SERVICE_INSTANCE_NAME)
                .csmUser("jolera_user")
                .build());

        Assertions.assertTrue(id.startsWith("MO_"));
    }

    private void expectWorkflowExists(String id, boolean exists) {
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.processInstanceBusinessKey(any())).thenReturn(processInstanceQuery);

        if (exists) {
            when(processInstanceQuery.singleResult()).thenReturn(processInstance);
            when(processInstance.getBusinessKey()).thenReturn(id);
        } else {
            when(processInstanceQuery.singleResult()).thenReturn(null);
        }
    }

    private void startProcessInstance(WorkflowTypes workflowType, String id) {
        when(runtimeService.startProcessInstanceByKey(eq(workflowType.getWorkflowName()), any(), any(Map.class)))
                .thenReturn(processInstance);
        when(processInstance.getBusinessKey()).thenReturn(id);
    }


}
