package com.bearingpoint.beyond.test-bpintegration.camunda.service.co;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;

@Slf4j
@Component
public class CreateCoUpdateTaskStep extends AbstractBaseStep {

    private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;

    public CreateCoUpdateTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
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

        TaskInfo previousTask = workflowUtil.getTaskInfo(getMandatoryVariable(execution, WF_PREVIOUS_TASK, String.class));
        String tenantWs = getMandatoryVariable(execution, WF_TENANT_WS, String.class);

        TaskV2DomainTask task = basedOnTheOtherTaskHandler.createNewTaskFromOldOneWithClosingNote(
                getMandatoryVariable(execution, CreateWsCoReviewTaskStep.TASK_TELUS_WS_CO_REVIEW_ID, String.class),
                tenantWs,
                previousTask,
                TaskType.TELUS_WS_CO_UPDATE
        );
        if (task != null) {
            setTaskInfo(task.getId(), task.getTaskDefinition(), tenantWs, execution, false);
        }
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }
}