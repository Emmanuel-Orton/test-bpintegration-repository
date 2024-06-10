package com.bearingpoint.beyond.test-bpintegration.camunda.service.co;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.co.CreateRetailCoCreateTaskStep.TASK_TELUS_RT_CO_CREATION_ID;


@Slf4j
@Component
public class CreateOrderModificationTaskStep extends AbstractBaseStep {
    private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;

    public CreateOrderModificationTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
                                           BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler) {
        super(objectMapper, workflowUtil);
        this.basedOnTheOtherTaskHandler = basedOnTheOtherTaskHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {
        String tenant = getMandatoryVariable(execution, WF_TENANT, String.class);
        String rtCoCreateTaskId = getVariable(execution, TASK_TELUS_RT_CO_CREATION_ID, String.class);

        TaskV2DomainTask task = basedOnTheOtherTaskHandler.createNewTaskFromOldOne(
                tenant,
                rtCoCreateTaskId,
                TaskType.TELUS_RT_ORDER_MODIFICATION
        );

        if (task != null) {
            setTaskInfo(task.getId(), task.getTaskDefinition(), tenant, execution, true);
        }
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.RETAIL;
    }
}
