package com.bearingpoint.beyond.test-bpintegration.camunda.service.mo;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.CustomerDataService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.mo.ModifyOrderHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_CSM_USER;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_RT_CUSTOMER;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_RT_CUSTOMER_LEGAL_NAME;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_SERVICE_INSTANCE_NAME;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_SERVICE_NAME;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_START_TASK_ID;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_CUSTOMER;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_ID;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_ORDER;


@Slf4j
@Component
public class CreateInitiateChangeOrderTaskStep extends AbstractBaseStep {

    private final ModifyOrderHandler modifyOrderHandler;
    private final CustomerDataService customerDataService;

    @Value("${tenant_name}")
    private String rtTenant;


    public CreateInitiateChangeOrderTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
                                             CustomerDataService customerDataService,
                                             ModifyOrderHandler modifyOrderHandler) {
        super(objectMapper, workflowUtil);
        this.customerDataService = customerDataService;
        this.modifyOrderHandler = modifyOrderHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {

        String tenant = getMandatoryVariable(execution, WF_TENANT_WS, String.class);

        TaskV2DomainTask task = modifyOrderHandler.createInitiateMoTask(
                getMandatoryVariable(execution, WF_WS_CUSTOMER, Long.class),
                tenant,
                getCustomerLegalName(execution),
                getMandatoryVariable(execution, WF_SERVICE_INSTANCE_NAME, String.class),
                getMandatoryVariable(execution, WF_SERVICE_NAME, String.class),
                getMandatoryVariable(execution, WF_CSM_USER, String.class),
                getMandatoryVariable(execution, WORKFLOW_ID, String.class)
        );

        if (task != null) {
            execution.setVariable(WF_START_TASK_ID, Long.parseLong(task.getId()));
            TaskInfo currentTask = setTaskInfo(task.getId(), task.getTaskDefinition(), tenant, execution, true);
            log.info("Created task {}", currentTask);
        }
    }

    private String getCustomerLegalName(DelegateExecution execution) {
        final String legalName = customerDataService.getCustomerLegalName(rtTenant,
                getMandatoryVariable(execution, WF_RT_CUSTOMER, Long.class).toString());
        execution.setVariable(WF_RT_CUSTOMER_LEGAL_NAME, legalName);
        return legalName;
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }
}
