package com.bearingpoint.beyond.test-bpintegration.service.handler.daf;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DafOrderItem;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DafServiceItemDto;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DafStartParameters;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.DafCamundaMessages;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.DafStatus;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrderItem;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEvent;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEventBody;
import com.bearingpoint.beyond.test-bpintegration.repository.ProvisioningOrderItemsRepository;
import com.bearingpoint.beyond.test-bpintegration.repository.domain.ProvisioningOrderItemEntity;
import com.bearingpoint.beyond.test-bpintegration.service.ServiceOrderingService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler;
import com.bearingpoint.beyond.test-bpintegration.service.tag.TagService;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_ID;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve.CreateDafApproveTaskStep.OVERDUE_DATE;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve.CreateDafApproveTaskStep.overdueDateSimpleDateFormat;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler.WF_APPROVAL_WARNING_TIMER_CANCELLED;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler.getDateAsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DafHandlerUTest {

    private static final String TENANT = "TELUS_SIT";
    private static final String TASK_ID = "5885849";
    private static final String BILLING_ACCOUNT_ID = "12334554";
    private static final String WORKFLOW_ID_VALUE = "437uyureyu748738";
    private static final Long WS_ORDER_ID_VALUE = 438378L;
    private static final Long SERVICE_PROVISIONING_ID = 323277L;
    private static final String WORKFLOW_ID_ID = "2718278";
    private static final long DAF_ID = 2718278L;
    private static final String TARGET_DATE = "2023-02-02";
    private static final String EARLIEST_ACTIVATION_DATE = "2023-02-02";

    private static final Boolean IS_PARTIAL_DELIVERY = true;
    private static final Long TARGET_QUANTITY = 15L;
    private static final Long ORDER_QUANTITY = 20L;
    private static final String SERVICE_NAME = "SERVICE_NAME";

    @Mock
    private TasksService tasksService;
    @Mock
    WorkflowUtil workflowUtil;
    @Mock
    RuntimeService runtimeService;
    @Mock
    private ExecutionEntity executionEntity;
    @Mock
    private MessageCorrelationBuilder messageCorrelationBuilder;
    @Mock
    private ProvisioningOrderItemsRepository provisioningOrderItemsRepository;
    @Mock
    private ServiceOrderingService serviceOrderingService;
    //@Mock
    private ObjectMapper objectMapper;
    @Mock
    private ManagementService managementService;
    @Mock
    private JobQuery jobQuery;
    @Mock
    private DelegateExecution delegateExecution;
    @Mock
    private TimerEntity job;
    @Mock
    private TagService tagService;


    @InjectMocks
    private DafHandler unitToTest;


    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configOverride(BigDecimal.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
        JsonNullableModule jnm = new JsonNullableModule();
        objectMapper.registerModule(jnm);
        ReflectionTestUtils.setField(unitToTest, "objectMapper", objectMapper);
    }


    @Test
    void startWorkflow_failWhenWholesaleOrderIdNotSet() throws Exception {
        DafStartParameters dafStartParameters = DafStartParameters.builder().build();
        Exception thrown = Assertions.assertThrows(Exception.class, () -> {
            unitToTest.startWorkflow(dafStartParameters);
        }, "Exception was expected");

        Assertions.assertEquals("wholesaleOrderId not specified in parameters.", thrown.getMessage());
    }

    @Test
    void startWorkflow_failWhenOrderItemsNotSet() throws Exception {
        DafStartParameters dafStartParameters = DafStartParameters.builder()
                .wholesaleOrderId(WS_ORDER_ID_VALUE).build();
        Exception thrown = Assertions.assertThrows(Exception.class, () -> {
            unitToTest.startWorkflow(dafStartParameters);
        }, "Exception was expected");

        Assertions.assertEquals("dafOrderItems not specified in parameters (or empty).", thrown.getMessage());
    }

    @Test
    void startWorkflow_failWhenNoOrderItemsForWsOrderInDatabase() throws Exception {
        DafStartParameters dafStartParameters = DafStartParameters.builder()
                .wholesaleOrderId(WS_ORDER_ID_VALUE)
                .dafOrderItems(List.of(DafOrderItem.builder().id(1L).build())).build();

        // List<DafOrderItem> requestList = List.of(DafOrderItem.builder().id(1L).build(), DafOrderItem.builder().id(2L).build());

        when(provisioningOrderItemsRepository.findByWholesaleOrderId(WS_ORDER_ID_VALUE))
                .thenReturn(new ArrayList<>());

        Exception thrown = Assertions.assertThrows(Exception.class, () -> {
            unitToTest.startWorkflow(dafStartParameters);
        }, "Exception was expected");

        Assertions.assertEquals("Could not find any items for wholesaleOrderId 438378 in provisioning database. Only TBS bundles with TBS services are supported.", thrown.getMessage());
    }

    @Test
    void startWorkflow_failWhenOrderItemsNotInOrder() {
        DafStartParameters dafStartParameters = DafStartParameters.builder()
                .wholesaleOrderId(WS_ORDER_ID_VALUE)
                .dafOrderItems(List.of(DafOrderItem.builder().id(100).build())).build();

        when(provisioningOrderItemsRepository.findByWholesaleOrderId(WS_ORDER_ID_VALUE))
                .thenReturn(List.of(ProvisioningOrderItemEntity.builder()
                        .dafStatus(DafStatus.READY.name())
                        .id(101L)
                        .build()));

        Exception thrown = Assertions.assertThrows(Exception.class, () -> unitToTest.startWorkflow(dafStartParameters),
                "Exception was expected");

        Assertions.assertEquals("Following selected order item(s) are not in wholesaleOrder with id 438378: 100", thrown.getMessage());
    }

    @Test
    void startWorkflow_failWhenOrderItemsAlreadyProcessed() {
        DafStartParameters dafStartParameters = DafStartParameters.builder()
                .wholesaleOrderId(WS_ORDER_ID_VALUE)
                .dafOrderItems(List.of(DafOrderItem.builder().id(100L).build())).build();

        when(provisioningOrderItemsRepository.findByWholesaleOrderId(WS_ORDER_ID_VALUE))
                .thenReturn(List.of(ProvisioningOrderItemEntity.builder()
                        .dafStatus(DafStatus.COMPLETE.name())
                        .id(100L)
                        .build()));

        Exception thrown = Assertions.assertThrows(Exception.class, () -> unitToTest.startWorkflow(dafStartParameters),
                "Exception was expected");

        Assertions.assertEquals("Following selected order item(s) are already in DAF process: 100", thrown.getMessage());
    }

    @Test
    void startWorkflow_startOK() throws Exception {
        DafStartParameters dafStartParameters = DafStartParameters.builder()
                .wholesaleOrderId(WS_ORDER_ID_VALUE)
                .dafOrderItems(List.of(DafOrderItem.builder().id(100L).quantity(1).requestedDate("2023-01-10").build())).build();

        System.out.println("Parameters: " + objectMapper.writeValueAsString(dafStartParameters));

        when(provisioningOrderItemsRepository.findByWholesaleOrderId(WS_ORDER_ID_VALUE))
                .thenReturn(List.of(ProvisioningOrderItemEntity.builder()
                        .dafStatus(DafStatus.READY.name())
                        .id(100L)
                        .build()));

        unitToTest.startWorkflow(dafStartParameters);
    }


    @Test
    @Disabled
    void prepareServiceOrderItems() {

        // TODO fix test prepareServiceOrderItems()
//        when(provisioningOrderItemsRepository.findAllById(any()))
//                .thenReturn(List.of(ProvisioningOrderItemEntity.builder().build()));
//
//        unitToTest.prepareServiceOrderItems(WORKFLOW_ID_VALUE, "1, 2, 3");
//
//        verify(provisioningOrderItemsRepository, times(1)).saveAll(any());
    }

    @Test
    @Disabled
    void prepareServiceOrderItems_requestedDateToday() {
        // TODO fix test prepareServiceOrderItems()

        when(provisioningOrderItemsRepository.findByWholesaleOrderId(WS_ORDER_ID_VALUE))
                .thenReturn(List.of(ProvisioningOrderItemEntity.builder()
                        .dafStatus(DafStatus.COMPLETE.name())
                        .id(100L)
                        .build()));


        when(provisioningOrderItemsRepository.findAllById(any()))
                .thenReturn(List.of(ProvisioningOrderItemEntity.builder()
                        .dafStatus(DafStatus.READY.name())
                        .id(100L)
                        .build()));

        List<DafOrderItem> dafOrderItems = List.of(DafOrderItem.builder()
                        .id(100)
                        .requestedDate(getDateAsString(Instant.now()))
                .build());

        unitToTest.prepareServiceOrderItems(WORKFLOW_ID_VALUE, dafOrderItems);

        //Captor<List<ProvisioningOrderItemEntity>> captor = ArgumentCaptor.forClass(String.class);

        verify(provisioningOrderItemsRepository, times(1)).saveAll(any());


    }

    @Test
    void cleanServiceOrderItems() {
        when(provisioningOrderItemsRepository.findByDafWorkflowId(WORKFLOW_ID_VALUE))
                .thenReturn(List.of(ProvisioningOrderItemEntity.builder().build()));

        unitToTest.cleanServiceOrderItems(WORKFLOW_ID_VALUE);

        verify(provisioningOrderItemsRepository, times(1)).saveAll(any());
    }


    @Test
    void continueServiceProvisioning_itemNotFoundInServiceOrder() {
        when(provisioningOrderItemsRepository.findByDafWorkflowId(WORKFLOW_ID_VALUE))
                .thenReturn(List.of(ProvisioningOrderItemEntity.builder()
                        .serviceProvisioningId(SERVICE_PROVISIONING_ID)
                        .tenant(TENANT)
                        .wholesaleServiceOrderId("11")
                        .build()));

        when(serviceOrderingService
                .getServiceOrderByExternalId("" + SERVICE_PROVISIONING_ID, TENANT))
                .thenReturn(new ServiceorderingV1DomainServiceOrder()
                        .state("InProgress")
                        .addOrderItemsItem(new ServiceorderingV1DomainServiceOrderItem()
                                .id("22"))
                );

        Exception thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            unitToTest.continueServiceProvisioning(WORKFLOW_ID_VALUE);
        }, "IllegalArgumentException was expected");

        Assertions.assertEquals("ServiceOrderItem with id 11 is missing from service order with externalId 323277", thrown.getMessage());
    }


    @Test
    void continueServiceProvisioning_allItemsFound() throws JsonProcessingException {

        when(provisioningOrderItemsRepository.findByDafWorkflowId(WORKFLOW_ID_VALUE))
                .thenReturn(List.of(ProvisioningOrderItemEntity.builder()
                        .serviceProvisioningId(SERVICE_PROVISIONING_ID)
                        .tenant(TENANT)
                        .wholesaleServiceOrderId("22")
                        .build()));

        when(serviceOrderingService
                .getServiceOrderByExternalId("" + SERVICE_PROVISIONING_ID, TENANT))
                .thenReturn(new ServiceorderingV1DomainServiceOrder()
                        .state("InProgress")
                        .addOrderItemsItem(new ServiceorderingV1DomainServiceOrderItem()
                                .id("22"))
                );

        unitToTest.continueServiceProvisioning(WORKFLOW_ID_VALUE);

        verify(serviceOrderingService, times(1)).sendOrderStateChangeNotification(eq(TENANT), any(), eq(null));
        verify(provisioningOrderItemsRepository, times(1)).saveAll(any());
    }

    @Test
    void updateStatusOfItems() {
        when(provisioningOrderItemsRepository.findByDafWorkflowId(WORKFLOW_ID))
                .thenReturn(List.of(new ProvisioningOrderItemEntity()));
        unitToTest.updateStatusOfItems(WORKFLOW_ID, DafStatus.IN_REVIEW);
    }

    @Test
    void databaseCleanup_noDeletableItems() {
        when(provisioningOrderItemsRepository.findDeletableItems(any())).thenReturn(new ArrayList<>());
        unitToTest.databaseCleanup(WS_ORDER_ID_VALUE);
    }

    @Test
    void databaseCleanup_deletableItemsFound() {
        when(provisioningOrderItemsRepository.findDeletableItems(any())).thenReturn(List.of(new ProvisioningOrderItemEntity()));
        unitToTest.databaseCleanup(WS_ORDER_ID_VALUE);
    }

    @Test
    void databaseCleanup_completeFound() {
        when(provisioningOrderItemsRepository.findDeletableItems(any())).thenReturn(new ArrayList<>());

        when(provisioningOrderItemsRepository.findByWholesaleOrderId(WS_ORDER_ID_VALUE))
                .thenReturn(List.of(ProvisioningOrderItemEntity.builder().dafStatus(DafStatus.COMPLETE.name()).build()));

        unitToTest.databaseCleanup(WS_ORDER_ID_VALUE);

        verify(provisioningOrderItemsRepository, times(1)).updateServiceEntryDeleteAfter(any(), any());
    }

    @Test
    void databaseCleanup_notAllComplete() {
        when(provisioningOrderItemsRepository.findDeletableItems(any())).thenReturn(new ArrayList<>());

        when(provisioningOrderItemsRepository.findByWholesaleOrderId(WS_ORDER_ID_VALUE))
                .thenReturn(List.of(ProvisioningOrderItemEntity.builder().dafStatus(DafStatus.IN_UPDATE.name()).build()));

        unitToTest.databaseCleanup(WS_ORDER_ID_VALUE);
    }

    @Test
    void startDafApprovalWarningSubprocess_workflowNotFound() {
        unitToTest.startDafApprovalWarningSubprocess(WORKFLOW_ID_VALUE);
    }

    @Test
    void startDafApprovalWarningSubprocess_ok() {

        when(workflowUtil.getWorkflow(WORKFLOW_ID_VALUE)).thenReturn(
                executionEntity);
        when(workflowUtil.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.createMessageCorrelation(DafCamundaMessages.DAF_APPROVAL_WARNING_TIMER_START.getMessage()))
                .thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey(WORKFLOW_ID_VALUE)).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariable(WF_APPROVAL_WARNING_TIMER_CANCELLED, "false")).thenReturn(messageCorrelationBuilder);

        unitToTest.startDafApprovalWarningSubprocess(WORKFLOW_ID_VALUE);
    }


    @Test
    void hasApprovalDueDateChanged_dateChanged() {
        GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        String nowFormatted = overdueDateSimpleDateFormat.format(now.getTime());

        TaskV2DomainTaskEvent taskEvent = new TaskV2DomainTaskEvent().event(new TaskV2DomainTaskEventBody()
                .current(
                        new com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTask()
                                .parameters(Map.of(OVERDUE_DATE, nowFormatted,
                                        WORKFLOW_ID, WORKFLOW_ID_VALUE))));

        when(workflowUtil.getWorkflow(WORKFLOW_ID_VALUE)).thenReturn(
                executionEntity);
        when(executionEntity.getId()).thenReturn(WORKFLOW_ID_VALUE);
        when(workflowUtil.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.getVariable(WORKFLOW_ID_VALUE, OVERDUE_DATE))
                .thenReturn("xzzd");

        Assertions.assertTrue(unitToTest.hasApprovalDueDateChanged(taskEvent));
    }

    @Test
    void hasApprovalDueDateChanged_dateSame() {
        GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        String nowFormatted = overdueDateSimpleDateFormat.format(now.getTime());

        TaskV2DomainTaskEvent taskEvent = new TaskV2DomainTaskEvent().event(new TaskV2DomainTaskEventBody()
                .current(
                        new com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTask()
                                .parameters(Map.of(OVERDUE_DATE, nowFormatted,
                                        WORKFLOW_ID, WORKFLOW_ID_VALUE))));

        when(workflowUtil.getWorkflow(WORKFLOW_ID_VALUE)).thenReturn(
                executionEntity);
        when(executionEntity.getId()).thenReturn(WORKFLOW_ID_ID);
        when(workflowUtil.getRuntimeService()).thenReturn(runtimeService);
        when(runtimeService.getVariable(WORKFLOW_ID_ID, OVERDUE_DATE))
                .thenReturn(nowFormatted);

        Assertions.assertFalse(unitToTest.hasApprovalDueDateChanged(taskEvent));
    }

    @Test
    void hasApprovalDueDateChanged_workflowNotFound() {
        GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        String nowFormatted = overdueDateSimpleDateFormat.format(now.getTime());

        TaskV2DomainTaskEvent taskEvent = createTaskEvent(Map.of(
                OVERDUE_DATE, nowFormatted,
                WORKFLOW_ID, WORKFLOW_ID_VALUE));

        when(workflowUtil.getWorkflow(WORKFLOW_ID_VALUE)).thenReturn(
                null);

        Assertions.assertFalse(unitToTest.hasApprovalDueDateChanged(taskEvent));
    }

    @Test
    void hasApprovalDueDateChanged_overdueDateNotFound() {
        TaskV2DomainTaskEvent taskEvent = createTaskEvent(Map.of(
                WORKFLOW_ID, WORKFLOW_ID_VALUE));

        Assertions.assertFalse(unitToTest.hasApprovalDueDateChanged(taskEvent));
    }

    @Test
    void updateDafApprovalWarningTimerWithTask_workflowNotFound() {
        when(workflowUtil.getWorkflow(WORKFLOW_ID_VALUE)).thenReturn(
                null);

        Assertions.assertFalse(unitToTest.updateDafApprovalWarningTimer(createTaskFromEvent(null,
                Map.of(WORKFLOW_ID, WORKFLOW_ID_VALUE))));
    }

    @Test
    void updateDafApprovalWarningTimerWithTask_workflowFound_notUpdated() {
        when(workflowUtil.getWorkflow(WORKFLOW_ID_VALUE)).thenReturn(
                executionEntity);

        GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        String nowFormatted = overdueDateSimpleDateFormat.format(now.getTime());

        ReflectionTestUtils.setField(unitToTest, "overdueTime", "08:30");

        expectJobQueryList(null);

        Assertions.assertFalse(unitToTest.updateDafApprovalWarningTimer(createTaskFromEvent(null, Map.of(
                WORKFLOW_ID, WORKFLOW_ID_VALUE,
                OVERDUE_DATE, nowFormatted //"2022-12-16"
        ))));
    }

    @Test
    void updateDafApprovalWarningTimerWithTask_workflowFound_updated() {
        when(workflowUtil.getWorkflow(WORKFLOW_ID_VALUE)).thenReturn(
                executionEntity);

        GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        String nowFormatted = overdueDateSimpleDateFormat.format(now.getTime());

        ReflectionTestUtils.setField(unitToTest, "overdueTime", "08:30");

        expectJobQueryList(job);

        when(job.toString()).thenReturn("TimerEntity [jobHandlerConfiguration=dafWarningTimer, test=123]");
        when(workflowUtil.getRuntimeService()).thenReturn(runtimeService);
        when(executionEntity.getId()).thenReturn(WORKFLOW_ID_ID);

        Assertions.assertTrue(unitToTest.updateDafApprovalWarningTimer(createTaskFromEvent(null, Map.of(
                WORKFLOW_ID, WORKFLOW_ID_VALUE,
                OVERDUE_DATE, nowFormatted
        ))));

        verify(runtimeService, times(1))
                .setVariable(WORKFLOW_ID_ID, OVERDUE_DATE, nowFormatted);
    }

    @Test
    void updateDafApprovalWarningTimerWithDelegate() {
        when(delegateExecution.getProcessInstanceId()).thenReturn(WORKFLOW_ID_VALUE);
        expectJobQueryList(null);

        unitToTest.updateDafApprovalWarningTimer(delegateExecution, new GregorianCalendar());
    }


    @Test
    void createDafApproveTask() {
        GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        String nowFormatted = overdueDateSimpleDateFormat.format(now.getTime());

        when(tasksService.getTask(TENANT, TASK_ID)).thenReturn(createTask(null, new HashMap<>()));

        TaskV2DomainTask task = createTask(TaskType.TELUS_WS_DAF_APPROVE, Map.of(OVERDUE_DATE, nowFormatted));

        when(tasksService.createTask(TENANT, task)).thenReturn(task);

        TaskV2DomainTask dafApproveTask = unitToTest.createDafApproveTask(TENANT, TASK_ID, nowFormatted,
                WS_ORDER_ID_VALUE.toString());

        Assertions.assertEquals(BILLING_ACCOUNT_ID, dafApproveTask.getBillingAccount());
        Assertions.assertNotNull(dafApproveTask.getParameters());
        Assertions.assertEquals(nowFormatted, dafApproveTask.getParameters().get(OVERDUE_DATE));
    }

    @Test
    void getTime_withInteger() {
        ReflectionTestUtils.setField(unitToTest, "overdueTime", "500");

        int[] time = unitToTest.getTime();

        Assertions.assertNotNull(time);
        Assertions.assertEquals(8, time[0]);
        Assertions.assertEquals(20, time[1]);
    }

    @Test
    void getTime_withString() {
        ReflectionTestUtils.setField(unitToTest, "overdueTime", "8:20");

        int[] time = unitToTest.getTime();

        Assertions.assertNotNull(time);
        Assertions.assertEquals(8, time[0]);
        Assertions.assertEquals(20, time[1]);
    }

    private TaskV2DomainTask createTask(TaskType taskType, Map<String, String> parameters) {
        TaskV2DomainTask task = new TaskV2DomainTask()
                .billingAccount(BILLING_ACCOUNT_ID);
        task.setTags(Collections.emptyList());

        if (taskType != null) {
            task.setTaskDefinition(taskType.name());
        }

        if (parameters != null) {
            task.setParameters(parameters);
        }

        return task;
    }


    private com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTask createTaskFromEvent(TaskType taskType, Map<String, String> parameters) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTask task = new com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTask()
                .id(TASK_ID)
                .billingAccount(BILLING_ACCOUNT_ID);

        if (taskType != null) {
            task.setTaskDefinition(taskType.name());
        }

        if (parameters != null) {
            task.setParameters(parameters);
        }

        return task;
    }


    private TaskV2DomainTaskEvent createTaskEvent(Map<String, String> parameters) {
        return new TaskV2DomainTaskEvent().event(new TaskV2DomainTaskEventBody()
                .current(
                        new com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTask()
                                .id(TASK_ID)
                                .billingAccount(BILLING_ACCOUNT_ID)
                                .parameters(parameters)));
    }

    private void expectJobQueryList(Job job) {
        when(workflowUtil.getManagementService()).thenReturn(managementService);
        when(managementService.createJobQuery()).thenReturn(jobQuery);
        when(jobQuery.processInstanceId(any())).thenReturn(jobQuery);
        if (job == null) {
            when(jobQuery.list()).thenReturn(new ArrayList<>());
        } else {
            when(jobQuery.list()).thenReturn(List.of(job));
        }
    }

    @Test
    public void getDafServiceOrderItemsList() {
        when(provisioningOrderItemsRepository.findByDafWorkflowId(WORKFLOW_ID_ID)).thenReturn(List.of(ProvisioningOrderItemEntity.builder()
                .id(DAF_ID)
                .requestedActivationDate(ProvisioningHandler.getDateFromStringAsInstant(TARGET_DATE))
                .earliestActivationDate(ProvisioningHandler.getDateFromStringAsInstant(EARLIEST_ACTIVATION_DATE))
                .requestedQuantity(TARGET_QUANTITY)
                .orderQuantity(ORDER_QUANTITY)
                .dafStatus(DafStatus.READY.name())
                .wholesaleService(SERVICE_NAME)
                .build()));

        List<DafServiceItemDto> dafServiceItemDtoList = new ArrayList<>();
        DafServiceItemDto dafServiceItem = DafServiceItemDto.builder()
                .id(DAF_ID)
                .targetDate(TARGET_DATE)
                .earliestActivationDate(EARLIEST_ACTIVATION_DATE)
                .isPartialDelivery(IS_PARTIAL_DELIVERY)
                .targetQuantity(TARGET_QUANTITY)
                .serviceName(SERVICE_NAME)
                .build();
        dafServiceItemDtoList.add(dafServiceItem);

        List<DafServiceItemDto> returnedList = unitToTest.getDafServiceOrderItemsList(WORKFLOW_ID_ID);

        Assertions.assertEquals(returnedList.get(0).getId(), dafServiceItemDtoList.get(0).getId());
        Assertions.assertEquals(returnedList.get(0).getTargetDate(), dafServiceItemDtoList.get(0).getTargetDate());
        Assertions.assertEquals(returnedList.get(0).getEarliestActivationDate(), dafServiceItemDtoList.get(0).getEarliestActivationDate());
        Assertions.assertEquals(returnedList.get(0).getIsPartialDelivery(), dafServiceItemDtoList.get(0).getIsPartialDelivery());
        Assertions.assertEquals(returnedList.get(0).getTargetDate(), dafServiceItemDtoList.get(0).getTargetDate());
        Assertions.assertEquals(returnedList.get(0).getServiceName(), dafServiceItemDtoList.get(0).getServiceName());

    }

}