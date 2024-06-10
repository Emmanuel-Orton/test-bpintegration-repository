package com.bearingpoint.beyond.test-bpintegration.camunda.service.base;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowTypes;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.co.CreateOrderModificationTaskStep;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.api.TasksV2Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEvent;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEventBody;
import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.dbunit.Assertion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep.WF_ERROR_TEMPLATE;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowErrorsAndRetries.*;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_NAME;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_NAME_SHORT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowErrorsAndRetriesUTest {

    private static final String TENANT = "TELUS_SIT";
    private static final String TASK_ID = "438783";
    private static final String BILLING_ACCOUNT_ID = "12334554";

    private static final String CAMUNDA_PROC_INST_ID_VALUE = "71726";
    private static final String CAMUNDA_ACT_ID_VALUE = "coCreateWsCoReviewTaskStep";
    private static final String EXECUTION_ID_VALUE = "4378438";


    @Mock
    private DelegateExecution delegateExecution;
    @Mock
    private TasksV2Api tasksV2Api;
//    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RuntimeService runtimeService;
    @Mock
    private InfonovaLinkService infonovaLinkService;
    @Mock
    private ExecutionQuery executionQuery;
    @Mock
    private Execution execution;

    @InjectMocks
    private WorkflowErrorsAndRetries unitToTest;

    @Captor
    private ArgumentCaptor<TaskV2DomainTask> taskV2DomainTaskArgumentCaptor;


    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(unitToTest, "objectMapper", objectMapper);

    }



    @Test
    public void raiseWorkflowErrorTask_withNullStacktrace() throws Exception {

        prepareDataForTask();

        when(tasksV2Api.postTasksV2Tasks(any(), taskV2DomainTaskArgumentCaptor.capture(), any()))
                .thenReturn(Mono.just(new TaskV2DomainTask().id(TASK_ID)));

        unitToTest.raiseWorkflowErrorTask("Problem couldn't be found.",
                null, delegateExecution, TenantType.RETAIL);

        TaskV2DomainTask capturedTask = taskV2DomainTaskArgumentCaptor.getValue();

        //System.out.println(taskV2DomainTaskArgumentCaptor.getAllValues());
        Assertions.assertEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_2"));
        Assertions.assertEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_3"));
        Assertions.assertEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_4"));
    }


    @Test
    public void raiseWorkflowErrorTask_with1ErrorText() throws Exception {

        prepareDataForTask();

        when(tasksV2Api.postTasksV2Tasks(any(), taskV2DomainTaskArgumentCaptor.capture(), any()))
                .thenReturn(Mono.just(new TaskV2DomainTask().id(TASK_ID)));


        unitToTest.raiseWorkflowErrorTask("Problem couldn't be found.",
                createStringOfSize(2000), delegateExecution, TenantType.RETAIL);

        TaskV2DomainTask capturedTask = taskV2DomainTaskArgumentCaptor.getValue();

        //System.out.println(taskV2DomainTaskArgumentCaptor.getAllValues());
        Assertions.assertEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_2"));
        Assertions.assertEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_3"));
        Assertions.assertEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_4"));
    }




    @Test
    public void raiseWorkflowErrorTask_with2ErrorText() throws Exception {
        prepareDataForTask();

        when(tasksV2Api.postTasksV2Tasks(any(), taskV2DomainTaskArgumentCaptor.capture(), any()))
                .thenReturn(Mono.just(new TaskV2DomainTask().id(TASK_ID)));


        unitToTest.raiseWorkflowErrorTask("Problem couldn't be found.",
                createStringOfSize(4000), delegateExecution, TenantType.RETAIL);

        TaskV2DomainTask capturedTask = taskV2DomainTaskArgumentCaptor.getValue();

        Assertions.assertNotEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_2"));
        Assertions.assertEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_3"));
        Assertions.assertEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_4"));
    }

    @Test
    public void raiseWorkflowErrorTask_with3ErrorText() throws Exception {
        prepareDataForTask();

        when(tasksV2Api.postTasksV2Tasks(any(), taskV2DomainTaskArgumentCaptor.capture(), any()))
                .thenReturn(Mono.just(new TaskV2DomainTask().id(TASK_ID)));


        unitToTest.raiseWorkflowErrorTask("Problem couldn't be found.",
                createStringOfSize(8000), delegateExecution, TenantType.RETAIL);

        TaskV2DomainTask capturedTask = taskV2DomainTaskArgumentCaptor.getValue();

        Assertions.assertNotEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_2"));
        Assertions.assertNotEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_3"));
        Assertions.assertEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_4"));
    }

    @Test
    public void raiseWorkflowErrorTask_with4ErrorText() throws Exception {
        prepareDataForTask();

        when(tasksV2Api.postTasksV2Tasks(any(), taskV2DomainTaskArgumentCaptor.capture(), any())).thenReturn(Mono.just(new TaskV2DomainTask()));

        unitToTest.raiseWorkflowErrorTask("Problem couldn't be found.",
                createStringOfSize(12000), delegateExecution, TenantType.RETAIL);

        TaskV2DomainTask capturedTask = taskV2DomainTaskArgumentCaptor.getValue();

        Assertions.assertNotEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_2"));
        Assertions.assertNotEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_3"));
        Assertions.assertNotEquals("", capturedTask.getParameters().get(ERROR_STACK_TRACE + "_4"));
    }

    @Test
    public void retry_noExecutionFound() {
        when(runtimeService.createExecutionQuery()).thenReturn(executionQuery);
        when(executionQuery.processInstanceId(any())).thenReturn(executionQuery);
        when(executionQuery.activityId(any())).thenReturn(executionQuery);
        when(executionQuery.singleResult()).thenReturn(null);

        Exception thrown = Assertions.assertThrows(Exception.class, () -> {
            unitToTest.retry(createTaskEvent(
                    Map.of(CAMUNDA_PROC_INST_ID, CAMUNDA_PROC_INST_ID_VALUE,
                            CAMUNDA_ACT_ID, CAMUNDA_ACT_ID_VALUE
                    )));
        }, "Exception was expected");

        Assertions.assertEquals("No execution has found for process 71726 and activity coCreateWsCoReviewTaskStep", thrown.getMessage());



    }

    @Test
    public void retry_executionFound() {
        when(runtimeService.createExecutionQuery()).thenReturn(executionQuery);
        when(executionQuery.processInstanceId(any())).thenReturn(executionQuery);
        when(executionQuery.activityId(any())).thenReturn(executionQuery);
        when(executionQuery.singleResult()).thenReturn(execution);

        when(execution.getId()).thenReturn(EXECUTION_ID_VALUE);

        unitToTest.retry(createTaskEvent(
                Map.of(CAMUNDA_PROC_INST_ID, CAMUNDA_PROC_INST_ID_VALUE,
                        CAMUNDA_ACT_ID, CAMUNDA_ACT_ID_VALUE
                )));

        verify(runtimeService, times(1)).signal(EXECUTION_ID_VALUE);

    }

    private String createStringOfSize(int length) throws Exception {
        String someText = new String(Files.readAllBytes(Paths.get(this.getClass().getResource("/orderHierarchy.json").toURI())));
        StringBuilder targetText = new StringBuilder();

        while(targetText.length() < length) {
            targetText.append(someText);
        }

        return targetText.substring(0, length);
    }


    private void prepareDataForTask() throws Exception {
//        String processInstanceId = taskParams.get(CAMUNDA_PROC_INST_ID);
//        String activityId = taskParams.get(CAMUNDA_ACT_ID);


        when(delegateExecution.getVariable(WORKFLOW_NAME_SHORT)).thenReturn(WorkflowTypes.DELIVERY_ACCEPTANCE_FORM.getShortName());
        when(delegateExecution.getVariable(WORKFLOW_NAME)).thenReturn(WorkflowTypes.DELIVERY_ACCEPTANCE_FORM.getWorkflowName());

        when(infonovaLinkService.getCorrectTenant(TenantType.RETAIL)).thenReturn(TENANT);
        when(delegateExecution.getVariable(WF_ERROR_TEMPLATE)).thenReturn("TELUS_WORKFLOW_ERROR");
    }





    private TaskV2DomainTaskEvent createTaskEvent(Map<String,String> parameters) {
        TaskV2DomainTaskEvent taskEvent = new TaskV2DomainTaskEvent().event(new TaskV2DomainTaskEventBody()
                .current(
                        new com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTask()
                                .id(TASK_ID)
                                .billingAccount(BILLING_ACCOUNT_ID)
                                .parameters(parameters)));
        return taskEvent;
    }

}