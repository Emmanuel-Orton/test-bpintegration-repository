package com.bearingpoint.beyond.test-bpintegration.camunda.service.base;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.CoStartParameters;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DafStartParameters;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.InflightOrderChangesDto;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.ModifyOrderStartParameters;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.StepInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowStatus;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowTypes;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowVariable;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep.WF_CANCELLED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep.WF_CURRENT_TASK;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep.WF_ERROR_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowUtil {

    @Value("${tenant_name}")
    private String tenant;

    @Value("${tenant_ws_name}")
    private String wsTenant;

    public static final String WF_TENANT = "TENANT";
    public static final String WF_TENANT_WS = "TENANT_WS";
    public static final String WF_DRAFT_ORDER = "DRAFT_ORDER";
    public static final String WF_TASK_RESOLUTION_ACTION = "WF_TASK_RESOLUTION_ACTION";
    public static final String WF_RT_ORDER = "RT_ORDER";
    public static final String WF_RT_CUSTOMER = "RT_CUSTOMER";
    public static final String WF_START_TASK_ID = "START_TASK_ID";
    public static final String WF_RT_CUSTOMER_LEGAL_NAME = "RT_CUSTOMER_LEGAL_NAME";
    public static final String WF_WS_ORDER = "WS_ORDER";
    public static final String WF_IS_RESTARTED = "IS_RESTARTED";
    public static final String WF_WS_PROVISIONING_ITEMS_REQUEST_DATA = "WS_PROVISIONING_ITEMS_REQUEST_DATA";
    public static final String WF_WS_CUSTOMER = "WS_CUSTOMER";
    public static final String DRAFT_ORDER_ID = "DRAFT_ORDER_ID";
    public static final String WS_ORDER_ID = "WS_ORDER_ID";
    public static final String WORKFLOW_NAME = "WORKFLOW_NAME";
    public static final String WORKFLOW_NAME_SHORT = "WORKFLOW_NAME_SHORT";
    public static final String WORKFLOW_ID = "WORKFLOW_ID";
    public static final String WF_WS_PROVISIONING_ID = "WS_PROVISIONING_ID";
    public static final String WF_CURRENT_TENANT = "CURRENT_TENANT";
    public static final String WF_ADDED_ORDER_ITEMS = "ADDED_ORDER_ITEMS";
    public static final String WF_REMOVED_ORDER_ITEMS = "REMOVED_ORDER_ITEMS";
    public static final String WF_MODIFIED_ORDER_ITEMS = "MODIFIED_ORDER_ITEMS";
    public static final String WF_DRAFT_ORDER_PLACED_DT = "DRAFT_ORDER_PLACED_DT";
    public static final String WF_SERVICE_INSTANCE_NAME = "SERVICE_INSTANCE_NAME";
    public static final String WF_SERVICE_NAME = "SERVICE_NAME";
    public static final String WF_CSM_USER = "CSM_USER";

    @Getter
    private final RuntimeService runtimeService;
    @Getter
    private final ManagementService managementService;
    @Getter
    private final ObjectMapper objectMapper;
    @Getter
    private final WorkflowErrorsAndRetries workflowErrorsAndRetries;

    public Map<String, Object> createContext(WorkflowTypes workflowType) {
        Map<String, Object> context = new HashMap<>();
        context.put(WF_TENANT, tenant);
        context.put(WF_TENANT_WS, wsTenant);
        context.put(WF_ERROR_TEMPLATE, "TELUS_WORKFLOW_ERROR");
        context.put(WF_TASK_RESOLUTION_ACTION, null);
        context.put(WORKFLOW_NAME_SHORT, workflowType.getShortName());
        context.put(WORKFLOW_NAME, workflowType.getWorkflowName());
        return context;
    }

    public String startSowWorkflow(String draftOrder) {
        Map<String, Object> context = createContext(WorkflowTypes.STATEMENT_OF_WORK);
        context.put(WF_DRAFT_ORDER, draftOrder);
        log.info("Starting workflow {} with id {}", WorkflowTypes.STATEMENT_OF_WORK.getWorkflowName(), draftOrder);
        ProcessInstance workflow = runtimeService.startProcessInstanceByKey(WorkflowTypes.STATEMENT_OF_WORK.getWorkflowName(), draftOrder, context);
        return workflow.getBusinessKey();
    }

    public String startWholesaleProvisioningWorkflow(String rtOrderId, String rtCustomer, String wsOrderId, String wsCustomer, String externalId, String tenant) {
        Map<String, Object> context = createContext(WorkflowTypes.WHOLESALE_PROVISIONING);
        context.put(WF_RT_ORDER, rtOrderId);
        context.put(WF_RT_CUSTOMER, rtCustomer);
        context.put(WF_WS_ORDER, wsOrderId);
        context.put(WF_WS_CUSTOMER, wsCustomer);
        context.put(WF_WS_PROVISIONING_ID, externalId);
        context.put(WF_CURRENT_TENANT, tenant);

        log.info("Starting workflow {} with wholesaleOderId {} (and retailOrderId {})", WorkflowTypes.WHOLESALE_PROVISIONING.getWorkflowName(), wsOrderId, rtOrderId);
        ProcessInstance workflow = runtimeService.startProcessInstanceByKey(WorkflowTypes.WHOLESALE_PROVISIONING.getWorkflowName(), wsOrderId, context);
        return workflow.getBusinessKey();
    }

    public String startInflightWholesaleProvisioningWorkflow(String rtOrderId, String rtCustomer, String wsOrderId,
                                                             String wsCustomer, String externalId, String tenant,
                                                             InflightOrderChangesDto inflightOrderChangesDto) throws JsonProcessingException {
        String addedItemsString = String.join(", ", inflightOrderChangesDto.getAddedItems());
        String removedItemsString = String.join(", ", inflightOrderChangesDto.getRemovedItems());
        String modifiedItemsString = String.join(", ", inflightOrderChangesDto.getModifiedItems());

        Map<String, Object> context = createContext(WorkflowTypes.INFLIGHT_WHOLESALE_PROVISIONING);
        context.put(WF_RT_ORDER, rtOrderId);
        context.put(WF_RT_CUSTOMER, rtCustomer);
        context.put(WF_WS_ORDER, wsOrderId);
        context.put(WF_WS_CUSTOMER, wsCustomer);
        context.put(WF_WS_PROVISIONING_ID, externalId);
        context.put(WF_CURRENT_TENANT, tenant);
        context.put(WF_ADDED_ORDER_ITEMS, addedItemsString);
        context.put(WF_REMOVED_ORDER_ITEMS, removedItemsString);
        context.put(WF_MODIFIED_ORDER_ITEMS, modifiedItemsString);

        String id = "INFLIGHT_" + wsOrderId;

        log.info("Starting workflow {} with id {} (for wholesaleOderId {}) [inflighOrderChanges={}]",
                WorkflowTypes.INFLIGHT_WHOLESALE_PROVISIONING.getWorkflowName(), id, wsOrderId,
                objectMapper.writeValueAsString(inflightOrderChangesDto)
        );
        ProcessInstance workflow = runtimeService.startProcessInstanceByKey(WorkflowTypes.INFLIGHT_WHOLESALE_PROVISIONING.getWorkflowName(), id, context);
        return workflow.getBusinessKey();
    }


    public String startDafProcessWorkflow(DafStartParameters startParameters) throws JsonProcessingException {
        Map<String, Object> context = createContext(WorkflowTypes.DELIVERY_ACCEPTANCE_FORM);
        context.put(WF_WS_ORDER, startParameters.getWholesaleOrderId());

        String idRoot = "DAF_" + startParameters.getWholesaleOrderId();
        String id = getFirstEmptyId(idRoot);

        if (id==null) {
            log.error("Couldn't find any empty id for Daf (Delivery Acceptance Form), with root {}", idRoot);
            return null;
        }

        String data = objectMapper.writeValueAsString(startParameters.getDafOrderItems());
        context.put(WF_WS_PROVISIONING_ITEMS_REQUEST_DATA, data);
        context.put(WORKFLOW_ID, id);

        log.info("Starting workflow {} (id={}) for wholesaleOderId {} (and requestData={})", WorkflowTypes.DELIVERY_ACCEPTANCE_FORM.getWorkflowName(), id, startParameters.getWholesaleOrderId(), data);
        ProcessInstance workflow = runtimeService.startProcessInstanceByKey(WorkflowTypes.DELIVERY_ACCEPTANCE_FORM.getWorkflowName(), id, context);
        return workflow.getBusinessKey();
    }

    public String startCoWorkflow(CoStartParameters startParameters) {
        String id = "CO_" + startParameters.getWholesaleOrderId();

        ProcessInstance checkWorkflow = getWorkflow(id);

        if (checkWorkflow != null) {
            log.error("ChangeOrder Workflow (id={}) is already running.", id);
            return null;
        }

        Map<String, Object> context = createContext(WorkflowTypes.CHANGE_ORDER);
        context.put(WF_WS_ORDER, startParameters.getWholesaleOrderId());
        context.put(WF_WS_CUSTOMER, startParameters.getBillingAccountId());
        context.put(WORKFLOW_ID, id);

        log.info("Starting workflow {} with id {}", WorkflowTypes.CHANGE_ORDER.getWorkflowName(), id);
        ProcessInstance workflow = runtimeService.startProcessInstanceByKey(WorkflowTypes.CHANGE_ORDER.getWorkflowName(), id, context);
        return workflow.getBusinessKey();
    }


    public String startModifyOrderWorkflow(String tenant, ModifyOrderStartParameters startParameters) throws JsonProcessingException {
        String idRoot = "MO_" + startParameters.getServiceInstanceName();

        if (!startParameters.getServiceInstanceName().contains("_")) {
            throw new IllegalArgumentException("Service instance name must be in format <retail_customer_id>_<unique uuid>.");
        }

        String retailCustomer = startParameters.getServiceInstanceName().substring(0, startParameters.getServiceInstanceName().indexOf("_"));

        String id = getFirstEmptyId(idRoot);

        if (id==null) {
            log.error("Couldn't find any empty id for Modify (Change Order Day 2), with root {}", idRoot);
            return null;
        }

        Map<String, Object> context = createContext(WorkflowTypes.MODIFY_ORDER);
        context.put(WF_RT_CUSTOMER, Long.valueOf(retailCustomer));
        context.put(WF_WS_CUSTOMER, startParameters.getWholesaleBillingAccountId());
        context.put(WF_SERVICE_INSTANCE_NAME, startParameters.getServiceInstanceName());
        context.put(WF_SERVICE_NAME, startParameters.getServiceName());
        context.put(WF_CSM_USER, startParameters.getCsmUser());
        context.put(WORKFLOW_ID, id);

        log.info("Starting workflow {} with id {}", WorkflowTypes.MODIFY_ORDER.getWorkflowName(), id);
        ProcessInstance workflow = runtimeService.startProcessInstanceByKey(WorkflowTypes.MODIFY_ORDER.getWorkflowName(), id, context);
        return workflow.getBusinessKey();
    }


    public ProcessInstance getWorkflow(String workflowId) {
        return runtimeService
                .createProcessInstanceQuery()
                .processInstanceBusinessKey(workflowId)
                .singleResult();
    }

    private String getFirstEmptyId(String idRoot) {

        ProcessInstance checkWorkflow = getWorkflow(idRoot);

        if (checkWorkflow != null) {
            for(int i=0; i<Integer.MAX_VALUE; i++) {
                checkWorkflow = getWorkflow(idRoot + "_" + i);

                if (checkWorkflow == null) {
                    return idRoot + "_" + i;
                }
            }

            return null;
        } else {
            return idRoot;
        }

    }


    /**
     * Return Id of current step in workflow (if workflow is running) or null
     * if such workflow is not running
     *
     * @param businessKey businessKey of workflow
     * @return name of step or null
     */
    @SneakyThrows
    public StepInfo getCurrentStepInfo(String businessKey) {
        ProcessInstance wfInstance = getWorkflow(businessKey);
        if (null != wfInstance) {
            String id;

            ActivityInstance rootActivityInstance = runtimeService.getActivityInstance(wfInstance.getId());

            if (rootActivityInstance.getChildActivityInstances() != null) {
                ActivityInstance childActivityInstance = rootActivityInstance.getChildActivityInstances()[0];
                id = childActivityInstance.getActivityId();
            } else {
                id = rootActivityInstance.getActivityId();
            }

            if (log.isDebugEnabled()) {
                log.debug("Workflow processInstance found, activityId {}, activity instance: {}", id, objectMapper.writeValueAsString(rootActivityInstance));
            }

            String wfInstanceName = wfInstance.getProcessDefinitionId();

            if (wfInstanceName != null && wfInstanceName.contains(":")) {
                wfInstanceName = wfInstanceName.substring(0, wfInstanceName.indexOf(":"));
            }

            return StepInfo.builder()
                    .businessKey(businessKey)
                    .stepId(id)
                    .workflowType(wfInstanceName)
                    .processInstanceId(wfInstance.getProcessInstanceId())
                    .processDefinitionId(wfInstance.getProcessDefinitionId())
                    .build();

        } else {
            log.debug("Workflow processInstance (businessKey={}) not found", businessKey);
            return null;
        }

    }


    public void handleTaskResolutionEvent(TaskV2DomainTaskEvent event, String parameterName, String messageName, String tenant) {
        handleTaskEventAndCreateMessage(event, parameterName, true, messageName, tenant);
    }

    public void handleTaskEventAndCreateMessage(TaskV2DomainTaskEvent event, String parameterName, boolean hasResolution, String messageName, String tenant) {
        handleTaskEventAndCreateMessageWithVariableSet(event, parameterName, hasResolution, messageName, tenant, null, null);
    }

    public void handleTaskEventAndCreateMessageWithVariableSet(TaskV2DomainTaskEvent event, String parameterName, boolean hasResolution, String messageName, String tenant, String variableName, String variableValue) {

        final Map<String, String> taskParameters = event.getEvent().getCurrent().getParameters();
        final String resolutionAction = event.getEvent().getCurrent().getResolutionAction();
        final String taskId = event.getEvent().getCurrent().getId();

        if (CollectionUtils.isEmpty(taskParameters)) {
            log.error("Task {} has no parameters.", taskId);
            return;
        }

        if (hasResolution && StringUtils.isBlank(resolutionAction)) {
            log.error("Task {} has no resolution action.", taskId);
            return;
        }

        final String orderId = taskParameters.get(parameterName);
        if (StringUtils.isBlank(orderId)) {
            log.error("Missing {} param for task {}", parameterName, taskId);
            return;
        }

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(orderId).singleResult();

        if (processInstance != null) {

            Object currentTaskRaw = runtimeService.getVariable(processInstance.getId(), WF_CURRENT_TASK);

            log.debug("currentTaskRaw: {}", currentTaskRaw);

            if (currentTaskRaw != null) {
                TaskInfo currentTask = getTaskInfo(currentTaskRaw);
                log.debug("currentTask: {}", currentTask);
                if (taskId != null && taskId.equals(currentTask.getTaskId())) {
                    log.debug("removing currentTask variable because its same as currently received one");
                    runtimeService.removeVariable(processInstance.getId(), WF_CURRENT_TASK);
                } else {
                    log.debug("NOT removing currentTask, not the same: currentTask={}, receivedTaskId={}/{}", currentTask, taskId, tenant);
                }
            }

            Object cancelled = runtimeService.getVariable(processInstance.getId(), WF_CANCELLED);

            if (cancelled != null) {
                log.warn("Workflow was cancelled, so task events are ignored.");
                return;
            }

            if (variableName != null) {
                log.info("Creating MessageCorrelation ({}) for {} {}, and resolution={} and setting variable {} with value {}", messageName, parameterName, orderId, resolutionAction, variableName, variableValue);
                runtimeService.createMessageCorrelation(messageName)
                        .processInstanceBusinessKey(orderId)
                        .setVariable(WF_TASK_RESOLUTION_ACTION, resolutionAction)
                        .setVariable(variableName, variableValue)
                        .correlateWithResult();
            } else {
                log.info("Creating MessageCorrelation ({}) for {} {}, and resolution={}", messageName, parameterName, orderId, resolutionAction);
                runtimeService.createMessageCorrelation(messageName)
                        .processInstanceBusinessKey(orderId)
                        .setVariable(WF_TASK_RESOLUTION_ACTION, resolutionAction)
                        .correlateWithResult();
            }

        } else {
            log.warn("Couldn't find running instance for {} {}, so MessageCorrelation (name={}) couldn't be created", parameterName, orderId, messageName);
        }
    }


    public void cancelWorkflow(String orderId, String messageName, String description) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(orderId).singleResult();

        if (processInstance != null) {
            log.info("Creating MessageCorrelation ({}) for Cancel of {}", messageName, description);
            runtimeService.createMessageCorrelation(messageName)
                    .processInstanceBusinessKey(orderId)
                    .correlateWithResult();
        } else {
            log.warn("Couldn't find running instance for {}, so we can't cancel.", description);
        }
    }


    @SneakyThrows
    public TaskInfo getTaskInfo(Object value) {
        if (value != null) {
            return objectMapper.readValue((String) value, TaskInfo.class);
        }
        return null;
    }


    public WorkflowStatus getWorkflowStatus(String businessKey, WorkflowTypes workflowTypes) {
        if (StringUtils.isBlank(businessKey)) {
            throw new IllegalArgumentException(String.format("BusinessKey must not be empty: '%s'.", businessKey));
        }

        StepInfo currentStep = getCurrentStepInfo(businessKey);

        if (currentStep != null) {
            return WorkflowStatus.builder()
                    .running(true)
                    .businessKey(businessKey)
                    .stepId(currentStep.getStepId())
                    .workflowType(currentStep.getWorkflowType() != null ? currentStep.getWorkflowType() : workflowTypes.getWorkflowName())
                    .processInstanceId(currentStep.getProcessInstanceId())
                    .processDefinitionId(currentStep.getProcessDefinitionId())
                    .build();
        } else {
            return WorkflowStatus.builder()
                    .running(false)
                    .canRestart(false)
                    .businessKey(businessKey)
                    .stepId("-")
                    .workflowType(workflowTypes == null ? "Unknown" : workflowTypes.getWorkflowName())
                    .processInstanceId("-")
                    .processDefinitionId("-")
                    .build();
        }
    }

    public WorkflowVariable getVariableValue(String businessKey, String variableName) {
        if (StringUtils.isBlank(businessKey)) {
            throw new IllegalArgumentException(String.format("BusinessKey must not be empty: '%s'.", businessKey));
        }

        ProcessInstance wfInstance = getWorkflow(businessKey);
        if (null != wfInstance) {
            String id;

            ActivityInstance rootActivityInstance = runtimeService.getActivityInstance(wfInstance.getId());

            if (rootActivityInstance.getChildActivityInstances() != null) {
                ActivityInstance childActivityInstance = rootActivityInstance.getChildActivityInstances()[0];
                id = childActivityInstance.getActivityId();
            } else {
                id = rootActivityInstance.getActivityId();
            }

            String wfInstanceName = wfInstance.getProcessDefinitionId();

            if (wfInstanceName != null && wfInstanceName.contains(":")) {
                wfInstanceName = wfInstanceName.substring(0, wfInstanceName.indexOf(":"));
            }

            try {
                Object variable = runtimeService.getVariable(wfInstance.getId(), variableName);

                if (variable == null) {
                    return WorkflowVariable.builder()
                            .businessKey(businessKey)
                            .stepId(id)
                            .workflowType(wfInstanceName)
                            .processInstanceId(wfInstance.getProcessInstanceId())
                            .processDefinitionId(wfInstance.getProcessDefinitionId())
                            .running(true)
                            .variableName(variableName)
                            .error(true)
                            .errorMessage(String.format("Variable %s could not be found.", variableName))
                            .build();
                }

                return WorkflowVariable.builder()
                        .businessKey(businessKey)
                        .stepId(id)
                        .workflowType(wfInstanceName)
                        .processInstanceId(wfInstance.getProcessInstanceId())
                        .processDefinitionId(wfInstance.getProcessDefinitionId())
                        .running(true)
                        .variableName(variableName)
                        .variableValue(variable.toString())
                        .error(false)
                        .build();

            } catch (Exception ex) {
                log.error("Variable could not be retrieved. Ex: " + ex.getMessage(), ex);
                return WorkflowVariable.builder()
                        .businessKey(businessKey)
                        .stepId(id)
                        .workflowType(wfInstanceName)
                        .processInstanceId(wfInstance.getProcessInstanceId())
                        .processDefinitionId(wfInstance.getProcessDefinitionId())
                        .running(true)
                        .variableName(variableName)
                        .error(true)
                        .errorMessage(String.format("Variable %s could not be retrieved (%s).", variableName, ex.getMessage()))
                        .build();
            }

        } else {
            log.debug("Workflow processInstance (businessKey={}) not found", businessKey);
            return WorkflowVariable.builder()
                    .businessKey(businessKey)
                    .running(false)
                    .variableName(variableName)
                    .error(true)
                    .errorMessage(String.format("Workflow processInstance (businessKey=%s) not found.", businessKey))
                    .build();
        }

    }


}
