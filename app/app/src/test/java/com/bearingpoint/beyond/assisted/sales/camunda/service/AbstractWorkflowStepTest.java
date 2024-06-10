package com.bearingpoint.beyond.test-bpintegration.camunda.service;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve.CreateDafApproveTaskStep.WF_DAF_APPROVAL_TASK_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
public class AbstractWorkflowStepTest {

    protected static final String WF_CURRENT_TASK = "WF_CURRENT_TASK";
    protected static final String WF_PREVIOUS_TASK = "WF_PREVIOUS_TASK";
    protected static final TaskInfo RETURN_NULL_FIRST_TIME = null;

    @Mock
    protected DelegateExecution execution;
    @Mock
    protected WorkflowUtil workflowUtil;

    protected ObjectMapper objectMapper;

    public void setObjectMapper(AbstractBaseStep baseStep) {
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(baseStep, "objectMapper", objectMapper);
    }


    protected void expectHasRetry(AbstractBaseStep baseStep, boolean expectedResult) {
        boolean retryActual = baseStep.hasRetry();

        assertThat(retryActual).isEqualTo(expectedResult);
    }


    protected void expectStepTenantType(AbstractBaseStep baseStep, TenantType tenantType) {
        TenantType stepTenantType = baseStep.getStepTenantType();

        assertThat(stepTenantType).isEqualTo(tenantType);
    }


    public <V> void expectGetVariable(String variableName, V val) {
        when(execution.hasVariable(variableName)).thenReturn(true);
        when(execution.getVariable(variableName)).thenReturn(val);
    }

    public <V> void expectGetVariableNotFound(String variableName) {
        when(execution.hasVariable(variableName)).thenReturn(false);
    }

    public void expectHasVariable(String variableName, boolean expectedResult) {
        when(execution.hasVariable(variableName)).thenReturn(expectedResult);
    }

    public <V> void expectGetMandatoryVariable(String variableName, V val) {
        when(execution.hasVariable(variableName)).thenReturn(true);
        when(execution.getVariable(variableName)).thenReturn(val);
    }

    public void expectGetVariableReturnNull(String variableName) {
        when(execution.hasVariable(variableName)).thenReturn(false);
    }

    public void verifySetTaskInfo(String taskId, String taskDefinition, String tenant, boolean setPreviousTask) {
        final String variableValue = String.format("{\"taskId\":\"%s\",\"taskDefinition\":\"%s\",\"tenant\":\"%s\"}", taskId, taskDefinition, tenant);
        verify(execution, times(1)).setVariable(WF_CURRENT_TASK, variableValue);
        if (setPreviousTask) {
            verify(execution, times(1)).setVariable(WF_PREVIOUS_TASK, variableValue);
        }
    }

    public void verifySetVariable(String variableName, Object variableValue) {
        verify(execution, times(1)).setVariable(variableName, variableValue);
    }



    public TaskInfo expectGetTaskInfo(String workflowVariable, String taskId, String taskTenant) {
        expectGetMandatoryVariable(workflowVariable, "{}");
        TaskInfo taskInfo = TaskInfo.builder()
                .taskId(taskId)
                .tenant(taskTenant)
                .build();
        when(workflowUtil.getTaskInfo(any())).thenReturn(taskInfo);
        return taskInfo;
    }


    public void expectRemoveVariable(String variableName) {
        execution.removeVariable(variableName);
    }


}
