package com.bearingpoint.beyond.test-bpintegration.camunda.service.co;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.co.ChangeOrderHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;


@Slf4j
@Component
public class CreateRetailCoCreateTaskStep extends AbstractBaseStep {

    public static final String TASK_TELUS_RT_CO_CREATION_ID = "TASK_TELUS_RT_CO_CREATION_ID";

    private final ChangeOrderHandler changeOrderHandler;

    public CreateRetailCoCreateTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil, ChangeOrderHandler changeOrderHandler) {
        super(objectMapper, workflowUtil);
        this.changeOrderHandler = changeOrderHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {
        TaskV2DomainTask task = changeOrderHandler.createRetailCOCreateTask(
            getMandatoryVariable(execution, WF_TENANT, String.class),
            getMandatoryVariable(execution, WF_TENANT_WS, String.class),
            getMandatoryVariable(execution, WF_WS_ORDER, Long.class),
            getMandatoryVariable(execution, WORKFLOW_ID, String.class)
        );

        if (task != null) {
            execution.setVariable(TASK_TELUS_RT_CO_CREATION_ID, task.getId());
            setTaskInfo(task.getId(), task.getTaskDefinition(), getMandatoryVariable(execution, WF_TENANT, String.class),
                    execution, true);
        }
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.RETAIL;
    }

}
