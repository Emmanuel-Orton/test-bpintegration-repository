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
import com.bearingpoint.beyond.test-bpintegration.repository.ProvisioningOrderItemsRepository;
import com.bearingpoint.beyond.test-bpintegration.repository.domain.ProvisioningOrderItemEntity;
import com.bearingpoint.beyond.test-bpintegration.service.ServiceOrderingService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler;
import com.bearingpoint.beyond.test-bpintegration.service.tag.TagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.persistence.entity.TimerEntity;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_ID;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve.CreateDafApproveTaskStep.OVERDUE_DATE;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve.CreateDafApproveTaskStep.overdueDateSimpleDateFormat;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafReviewTaskHandler.DAF_SERVICE_ITEMS;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafReviewTaskHandler.PRODUCT_NAME;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafReviewTaskHandler.WS_BILLING_ACCOUNT_ID;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler.getDateAsString;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler.getDateFromStringAsInstant;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler.getDateFromStringAsOffsetDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class DafHandler {

    public static final String WF_APPROVAL_WARNING_TIMER_CANCELLED = "APPROVAL_WARNING_TIMER_CANCELLED";
    public static final String COMPLETED_STATE = "Completed";
    public static final String IN_PROGRESS_STATE = "InProgress";
    private final WorkflowUtil workflowUtil;
    private final ProvisioningOrderItemsRepository provisioningOrderItemsRepository;
    private final ServiceOrderingService serviceOrderingService;
    private final ObjectMapper objectMapper;
    private final TasksService tasksService;
    private final TagService tagService;

    @Value("${telus-test-bpintegration-api.overdue_date_start_time}")
    private String overdueTime;

    public String startWorkflow(DafStartParameters startParameters) throws Exception {

        if (startParameters.getWholesaleOrderId() == null) {
            throw new Exception("wholesaleOrderId not specified in parameters.");
        }

        if (CollectionUtils.isEmpty(startParameters.getDafOrderItems())) {
            throw new Exception("dafOrderItems not specified in parameters (or empty).");
        }

        List<ProvisioningOrderItemEntity> items = provisioningOrderItemsRepository.findByWholesaleOrderId(startParameters.getWholesaleOrderId());

        if (CollectionUtils.isEmpty(items)) {
            throw new Exception("Could not find any items for wholesaleOrderId " + startParameters.getWholesaleOrderId() + " in provisioning database. Only TBS bundles with TBS services are supported.");
        }

        List<Long> notFoundInOrder = new ArrayList<>();
        List<Long> alreadyProcessed = new ArrayList<>();

        String errorString = "";

        // Check 1: All specified items are part of WholesaleOrder
        for (DafOrderItem dafOrderItem : startParameters.getDafOrderItems()) {
            ProvisioningOrderItemEntity item = getItem(dafOrderItem.getId(), items);

            if (item == null) {
                notFoundInOrder.add(dafOrderItem.getId());
            } else {
                if (!DafStatus.READY.name().equals(item.getDafStatus())) {
                    alreadyProcessed.add(dafOrderItem.getId());
                }
            }
        }

        if (notFoundInOrder.size() > 0) {
            errorString += String.format("Following selected order item(s) are not in wholesaleOrder with id %d: %s",
                    startParameters.getWholesaleOrderId(), notFoundInOrder.stream().map(Object::toString)
                            .collect(Collectors.joining(", ")));
        }

        if (alreadyProcessed.size() > 0) {
            if (errorString.length() != 0) {
                errorString += "\n";
            }

            errorString += String.format("Following selected order item(s) are already in DAF process: %s",
                    alreadyProcessed.stream().map(Object::toString)
                            .collect(Collectors.joining(", ")));
        }

        if (errorString.length() != 0) {
            throw new Exception(errorString);
        }

        return workflowUtil.startDafProcessWorkflow(startParameters);
    }

    private ProvisioningOrderItemEntity getItem(Long selectedOrderItemId, List<ProvisioningOrderItemEntity> items) {
        Optional<ProvisioningOrderItemEntity> first = items.stream()
                .filter(pre -> pre.getId().equals(selectedOrderItemId))
                .findFirst();

        return first.orElse(null);
    }

    public void prepareServiceOrderItems(String workflowId, List<DafOrderItem> dafOrderItems) {

        List<Long> idsList = dafOrderItems.stream().map(DafOrderItem::getId).collect(Collectors.toList());

        Iterable<ProvisioningOrderItemEntity> entityList = provisioningOrderItemsRepository.findAllById(idsList);

        for (ProvisioningOrderItemEntity entity : entityList) {

            Optional<DafOrderItem> first = dafOrderItems.stream().filter(pre -> pre.getId() == entity.getId()).findFirst();

            if (first.isPresent()) {
                DafOrderItem dafOrderItem = first.get();
                entity.setDafWorkflowId(workflowId);
                entity.setRequestedQuantity(dafOrderItem.getQuantity() == null ? null : dafOrderItem.getQuantity().longValue());
                entity.setRequestedActivationDate(getDateFromStringAsInstant(dafOrderItem.getRequestedDate()));
                entity.setDafStatus(DafStatus.IN_REVIEW.name());
            } else {
                log.error("Item not found. Should not happen. dafOrderItems={}, entityList={}", getDebugData(dafOrderItems), getDebugData(entityList));
            }
        }
        provisioningOrderItemsRepository.saveAll(entityList);
    }

    private String getDebugData(Object data) {
        if (data == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return data.toString();
        }
    }


    public void cleanServiceOrderItems(String workflowId) {
        Iterable<ProvisioningOrderItemEntity> entityList = provisioningOrderItemsRepository.findByDafWorkflowId(workflowId);

        for (ProvisioningOrderItemEntity entity : entityList) {
            entity.setDafWorkflowId(null);
            entity.setDafStatus(DafStatus.READY.name());
        }

        provisioningOrderItemsRepository.saveAll(entityList);
    }

    public void continueServiceProvisioning(String workflowId) throws JsonProcessingException {

        List<ProvisioningOrderItemEntity> entityList = provisioningOrderItemsRepository.findByDafWorkflowId(workflowId);

        ProvisioningOrderItemEntity entityFirst = entityList.get(0);
        String tenant = entityFirst.getTenant();

        ServiceorderingV1DomainServiceOrder serviceOrderByExternalId = serviceOrderingService
                .getServiceOrderByExternalId(entityFirst.getServiceProvisioningId().toString(), tenant);

        if (COMPLETED_STATE.equals(serviceOrderByExternalId.getState())) {
            log.debug("Service order {} is already in completed state, no action required.",
                    serviceOrderByExternalId.getExternalId());
            return;
        }

        HashMap<String, ArrayList<ProvisioningOrderItemEntity>> orderItemsSortedByDates = getOrderItemsSortedByDates(entityList);

        for (HashMap.Entry<String, ArrayList<ProvisioningOrderItemEntity>> entry : orderItemsSortedByDates.entrySet()) {
            continueServiceProvisioningForItems(entry.getKey(), entry.getValue(), serviceOrderByExternalId);
        }

        provisioningOrderItemsRepository.saveAll(entityList);
    }

    private void continueServiceProvisioningForItems(String date, List<ProvisioningOrderItemEntity> entityList, ServiceorderingV1DomainServiceOrder serviceOrderByExternalId) throws JsonProcessingException {

        OffsetDateTime targetDateTime = null;

        ProvisioningOrderItemEntity entityFirst = entityList.get(0);
        String tenant = entityFirst.getTenant();

        if (!date.equals("NULL")) {
            String earliestActivationDate = getDateAsString(entityFirst.getEarliestActivationDate());

            if (earliestActivationDate.equals(date)) {
                Instant earliestActivationDateObject = entityFirst.getEarliestActivationDate();

                OffsetDateTime offsetDateTime = earliestActivationDateObject.atOffset(ZoneOffset.UTC);
                targetDateTime = offsetDateTime.plus(1, ChronoUnit.HOURS);
            } else {
                targetDateTime = getDateFromStringAsOffsetDateTime(date);
            }
        }

        ServiceorderingV1DomainServiceOrder serviceOrder = new ServiceorderingV1DomainServiceOrder()
                .externalId(serviceOrderByExternalId.getExternalId())
                .priority(serviceOrderByExternalId.getPriority())
                .cancelable(false)
                .changeable(true)
                .state(IN_PROGRESS_STATE);

        if (targetDateTime != null) {
            serviceOrder.setOrderDate(targetDateTime);
            serviceOrder.setExpectedCompletionDate(targetDateTime);
            serviceOrder.setCompletionDate(targetDateTime);
        }

        for (ProvisioningOrderItemEntity entity : entityList) {
            String id = entity.getWholesaleServiceOrderId();

            ServiceorderingV1DomainServiceOrderItem item = serviceOrderByExternalId.getOrderItems()
                    .stream()
                    .filter(pre -> id.equals(pre.getId()))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.error("ServiceOrderItem with id {} is missing", id);
                        throw new IllegalArgumentException("ServiceOrderItem with id " + id + " is missing from service order with externalId " + entityFirst.getServiceProvisioningId());
                    });

            item.setState(COMPLETED_STATE);
            serviceOrder.addOrderItemsItem(item);
            entity.setDafStatus(DafStatus.COMPLETE.name());
        }

        if (log.isDebugEnabled()) {
            log.debug("ServiceOrder to change: {}", objectMapper.writeValueAsString(serviceOrder));
        }

        serviceOrderingService.sendOrderStateChangeNotification(tenant, serviceOrder, null);

    }

    private HashMap<String, ArrayList<ProvisioningOrderItemEntity>> getOrderItemsSortedByDates(List<ProvisioningOrderItemEntity> entityList) {
        HashMap<String, ArrayList<ProvisioningOrderItemEntity>> itemsMap = new HashMap<>();

        String todaysDate = getDateAsString(Instant.now());

        for (ProvisioningOrderItemEntity entity : entityList) {
            String activationDate = "NULL";

            if (entity.getRequestedActivationDate() != null) {
                String requestedActionDate = getDateAsString(entity.getRequestedActivationDate());
                if (!Objects.equals(todaysDate, requestedActionDate)) {
                    activationDate = requestedActionDate;
                }
            }

            if (itemsMap.containsKey(activationDate)) {
                itemsMap.get(activationDate).add(entity);
            } else {
                itemsMap.put(activationDate, new ArrayList<>(List.of(entity)));
            }
        }

        return itemsMap;

    }

    @SneakyThrows
    public void updateServiceOrderAndStatusOfItems(String workflowId) {
        List<ProvisioningOrderItemEntity> entityList = provisioningOrderItemsRepository.findByDafWorkflowId(workflowId);

        ProvisioningOrderItemEntity entityFirst = entityList.get(0);

        String tenant = entityFirst.getTenant();

        ServiceorderingV1DomainServiceOrder serviceOrderByExternalId = serviceOrderingService
                .getServiceOrderByExternalId(entityFirst.getServiceProvisioningId().toString(), tenant);

        if (COMPLETED_STATE.equals(serviceOrderByExternalId.getState())) {
            log.debug("Service order {} is already in completed state, no action required.",
                    serviceOrderByExternalId.getExternalId());
            return;
        }

        ServiceorderingV1DomainServiceOrder serviceOrder = new ServiceorderingV1DomainServiceOrder()
                .externalId(entityFirst.getServiceProvisioningId().toString())
                .priority(serviceOrderByExternalId.getPriority())
                .cancelable(false)
                .changeable(true)
                .state(IN_PROGRESS_STATE);

        for (ProvisioningOrderItemEntity entity : entityList) {
            String id = entity.getWholesaleServiceOrderId();

            ServiceorderingV1DomainServiceOrderItem item = serviceOrderByExternalId.getOrderItems()
                    .stream()
                    .filter(pre -> id.equals(pre.getId()))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.error("ServiceOrderItem with id {} is missing", id);
                        throw new IllegalArgumentException("ServiceOrderItem with id " + id + " is missing from service order with externalId " + entityFirst.getServiceProvisioningId());
                    });

            item.setState(COMPLETED_STATE);
            serviceOrder.addOrderItemsItem(item);
            entity.setDafStatus(DafStatus.COMPLETE.name());
        }

        if (serviceOrder.getOrderItems().size() == serviceOrderByExternalId.getOrderItems().size()) {
            serviceOrder.setState(COMPLETED_STATE);
            serviceOrder.changeable(false);
        }

        if (log.isDebugEnabled()) {
            log.debug("ServiceOrder to change: {}", objectMapper.writeValueAsString(serviceOrder));
        }

        serviceOrderingService.sendOrderStateChangeNotification(tenant, serviceOrder, null);
        provisioningOrderItemsRepository.saveAll(entityList);
    }


    public void updateStatusOfItems(String workflowId, DafStatus dafStatus) {
        Iterable<ProvisioningOrderItemEntity> entityList = provisioningOrderItemsRepository.findByDafWorkflowId(workflowId);

        for (ProvisioningOrderItemEntity entity : entityList) {
            entity.setDafStatus(dafStatus.name());
        }
        provisioningOrderItemsRepository.saveAll(entityList);
    }

    public void databaseCleanup(Long wholesaleOrderId) {
        // 1. cleanup all old items
        log.debug("Database Cleanup. Determining if we have items to delete...");
        List<ProvisioningOrderItemEntity> deletableItems = provisioningOrderItemsRepository.findDeletableItems(Instant.now());

        if (!deletableItems.isEmpty()) {
            log.info("Deleting {} old items.", deletableItems);
            provisioningOrderItemsRepository.deleteAll(deletableItems);
        } else {
            log.debug("No items to delete.");
        }


        // 2. check if current order is fully completed, if yes mark deletion of items in 6 months

        List<ProvisioningOrderItemEntity> byWholesaleOrderId = provisioningOrderItemsRepository.findByWholesaleOrderId(wholesaleOrderId);
        boolean complete = true;

        for (ProvisioningOrderItemEntity entity : byWholesaleOrderId) {
            if (!DafStatus.COMPLETE.name().equals(entity.getDafStatus())) {
                complete = false;
                break;
            }
        }

        if (complete) {
            log.debug("Database Cleanup. All provisioning items were processed, making them to be deleted in 6 months...");
            Instant time = Instant.now();
            time = time.plus(6 * 30, ChronoUnit.DAYS); // we add 6 times 30 days, which is about 6 months
            provisioningOrderItemsRepository.updateServiceEntryDeleteAfter(time, wholesaleOrderId);
        }
    }

    public void startDafApprovalWarningSubprocess(String workflowId) {
        ProcessInstance workflow = workflowUtil.getWorkflow(workflowId);

        if (workflow != null) {
            log.debug("Starting Daf Approval Warning Subprocess in workflow {}.", workflowId);
            workflowUtil.getRuntimeService().createMessageCorrelation(DafCamundaMessages.DAF_APPROVAL_WARNING_TIMER_START.getMessage())
                    .processInstanceBusinessKey(workflowId)
                    .setVariable(WF_APPROVAL_WARNING_TIMER_CANCELLED, "false")
                    .correlateWithResult();
        } else {
            log.warn("Workflow {} is not running. Can't start the subprocess.", workflowId);
        }
    }


    // This hack is needed because Camunda job.getJobConfiguration is not working correctly, any other way
    // to retrieve this string has failed so far, this might be camunda issue.
    private String getJobConfiguration(TimerEntity timerEntity) {
        String timerEntityString = timerEntity.toString();
        String jc2 = timerEntityString.substring(timerEntityString.indexOf("[") + 1, timerEntityString.indexOf("]"));

        Map<String, String> headerMap = Arrays.stream(jc2.split(", "))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));

        return headerMap.get("jobHandlerConfiguration");
    }

    public boolean hasApprovalDueDateChanged(TaskV2DomainTaskEvent taskEvent) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTask task = taskEvent.getEvent().getCurrent();
        Map<String, String> parameters = task.getParameters();

        if (parameters.containsKey(OVERDUE_DATE)) {
            String overdueDate = parameters.get(OVERDUE_DATE);
            String workflowId = parameters.get(WORKFLOW_ID);

            ProcessInstance workflow = workflowUtil.getWorkflow(workflowId);

            if (workflow == null) {
                log.error("Workflow instance {} not found.", workflowId);
                return false;
            } else {
                String overdueDateWf = (String) workflowUtil.getRuntimeService().getVariable(workflow.getId(), OVERDUE_DATE);
                log.debug("Overdue Date: Task={}, Workflow={}", overdueDate, overdueDateWf);
                return !(overdueDate.equals(overdueDateWf));
            }
        } else {
            log.error("Task {} has no OVERDUE_DATE parameter.", task.getId());
            return false;
        }
    }

    public boolean updateDafApprovalWarningTimer(DelegateExecution execution, GregorianCalendar dateTime) {
        updateTime(dateTime);
        return updateDafApprovalWarningTimer(execution.getProcessInstanceId(), dateTime.getTime());
    }

    @SneakyThrows
    public boolean updateDafApprovalWarningTimer(com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTask task) {
        Map<String, String> parameters = task.getParameters();
        String workflowId = parameters.get(WORKFLOW_ID);
        ProcessInstance workflow = workflowUtil.getWorkflow(workflowId);

        if (workflow == null) {
            log.error("Workflow is not running.");
            return false;
        }

        String overdueDateString = parameters.get(OVERDUE_DATE);

        Date parsedDate = overdueDateSimpleDateFormat.parse(overdueDateString);

        GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        gc.setTime(parsedDate);

        updateTime(gc);

        if (updateDafApprovalWarningTimer(workflow.getProcessInstanceId(), gc.getTime())) {
            log.info("Update of Timer successful, updating Workflow variable {} to date: {}", OVERDUE_DATE, overdueDateString);
            workflowUtil.getRuntimeService().setVariable(workflow.getId(), OVERDUE_DATE, overdueDateString);
            return true;
        } else {
            log.error("Update of Timer unsuccessful (workflowId={}). Shouldn't happen.", workflowId);
            return false;
        }
    }

    private void updateTime(GregorianCalendar baseDate) {
        int[] time = getTime();

        baseDate.set(Calendar.HOUR, time[0]);
        baseDate.set(Calendar.HOUR_OF_DAY, time[0]);
        baseDate.set(Calendar.MINUTE, time[1]);
        baseDate.set(Calendar.SECOND, 0);
        baseDate.set(Calendar.MILLISECOND, 0);
    }

    public boolean updateDafApprovalWarningTimer(String processInstanceId, Date dateTime) {

        ManagementService managementService = workflowUtil.getManagementService();

        List<Job> listOfJobs = managementService.createJobQuery()
                .processInstanceId(processInstanceId)
                .list();

        if (listOfJobs.isEmpty()) {
            log.warn("No Jobs found (expected at least one with dafWarningTimer).");
            return false;
        }

        TimerEntity targetJob = null;

        for (Job job : listOfJobs) {
            if (job instanceof TimerEntity) {
                TimerEntity timerEntity = (TimerEntity) job;
                log.debug("timerEntity: {}", timerEntity);

                String jobConfiguration = getJobConfiguration(timerEntity);
                log.debug("JobConfiguration: " + jobConfiguration);

                if ("dafWarningTimer".equals(jobConfiguration)) {
                    targetJob = timerEntity;
                }
            }
        }

        if (targetJob == null) {
            log.warn("Target Job (dafWarningTimer) not found.");
            return false;
        }

        managementService.setJobDuedate(targetJob.getId(), dateTime);

        return true;
    }

    public TaskV2DomainTask createDafApproveTask(String tenant, String reviewTaskId, String overdueDate, String orderId) {

        TaskV2DomainTask oldTask = tasksService.getTask(tenant, reviewTaskId);

        Map<String, String> parameters = oldTask.getParameters();
        parameters.put(OVERDUE_DATE, overdueDate);

        final String wsBillingAccount = parameters.get(WS_BILLING_ACCOUNT_ID);
        final String productName = parameters.get(PRODUCT_NAME);

        final TaskV2DomainTask task = new TaskV2DomainTask()
                .taskDefinition(TaskType.TELUS_WS_DAF_APPROVE.name())
                .billingAccount(oldTask.getBillingAccount())
                .relatedServiceOrders(oldTask.getRelatedServiceOrders())
                .tags(tagService.getCommonTagsForDaf(tenant, wsBillingAccount, productName, orderId))
                .parameters(oldTask.getParameters());

        return tasksService.createTask(tenant, task);
    }

    int[] overdueTimeInt = null;

    public int[] getTime() {
        try {
            if (overdueTimeInt == null) {
                overdueTimeInt = new int[2];
                if (overdueTime.contains(":")) {
                    String[] split = overdueTime.split(":");
                    overdueTimeInt[0] = Integer.parseInt(split[0]);
                    overdueTimeInt[1] = Integer.parseInt(split[1]);
                } else {
                    int minutes = Integer.parseInt(overdueTime);

                    if (minutes == 0) {
                        overdueTimeInt = new int[]{0, 0};
                    } else {
                        int hours = minutes / 60;
                        minutes = minutes % 60;
                        overdueTimeInt = new int[]{hours, minutes};
                    }
                }
            }

            log.debug("Overdue time for overdue date: {}:{}", overdueTimeInt[0], overdueTimeInt[1]);

            return overdueTimeInt;
        } catch (Exception ex) {
            log.error("Problem parsing or reading parameter (overdueTime={}, exception={})", overdueTime, ex.getMessage());
            overdueTimeInt = new int[]{0, 0};
            return overdueTimeInt;
        }
    }

    public List<DafServiceItemDto> getDafServiceOrderItemsList(String dafWorkflowId) {

        List<ProvisioningOrderItemEntity> inList = provisioningOrderItemsRepository.findByDafWorkflowId(dafWorkflowId)
                .stream()
                .filter((item) -> !item.getDafStatus().equals(DafStatus.COMPLETE.name()))
                .collect(Collectors.toList());

        return inList.stream()
                .map((entity) -> DafServiceItemDto.builder()
                        .id(entity.getId())
                        .targetDate(ProvisioningHandler.getDateAsString(entity.getRequestedActivationDate()))
                        .earliestActivationDate(ProvisioningHandler.getDateAsString(entity.getEarliestActivationDate()))
                        .isPartialDelivery(entity.getRequestedQuantity() != null)
                        .targetQuantity(entity.getRequestedQuantity() != null
                                ? entity.getRequestedQuantity()
                                : entity.getOrderQuantity())
                        .serviceName(entity.getWholesaleService())
                        .build())
                .collect(Collectors.toList());
    }

    public void updateDatesFromTask(com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTask task) throws JsonProcessingException {

        if (CollectionUtils.isEmpty(task.getParameters()) || !task.getParameters().containsKey(DAF_SERVICE_ITEMS)) {
            return;
        }

        String workflowId = task.getParameters().get(WORKFLOW_ID);
        String data = task.getParameters().get(DAF_SERVICE_ITEMS);

        List<ProvisioningOrderItemEntity> items = provisioningOrderItemsRepository.findByDafWorkflowId(workflowId);

        List<DafServiceItemDto> listServiceItems = objectMapper.readerForListOf(DafServiceItemDto.class).readValue(data);

        List<ProvisioningOrderItemEntity> listOut = items.stream()
                .peek((entity) -> listServiceItems.stream()
                        .filter(pre -> pre.getId() == entity.getId())
                        .findFirst()
                        .ifPresent((dafServiceItem) -> {
                            if (!Objects.equals(ProvisioningHandler.getDateAsString(entity.getRequestedActivationDate()),
                                    dafServiceItem.getTargetDate())) {
                                entity.setRequestedActivationDate(getDateFromStringAsInstant(dafServiceItem.getTargetDate()));
                            }
                        })
                )
                .collect(Collectors.toList());

        log.debug("List before saving: " + objectMapper.writeValueAsString(listOut));

        provisioningOrderItemsRepository.saveAll(listOut);
    }

    public void deleteDafApprovalWarningTimer(DelegateExecution execution) {
        if (updateDafApprovalWarningTimer(execution.getProcessInstanceId(), null)) {
            log.info("DafApprovalWarning subprocess will be cancelled win next job execution run.");
        }
    }
}
