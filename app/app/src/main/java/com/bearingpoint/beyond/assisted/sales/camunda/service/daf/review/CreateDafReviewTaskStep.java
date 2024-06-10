package com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.review;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.DafStatus;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafReviewTaskHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_ORDER;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_ID;


@Slf4j
@Component
public class CreateDafReviewTaskStep extends AbstractBaseStep {

    public static final String TASK_TELUS_WS_DAF_REVIEW_ID = "TASK_TELUS_WS_DAF_REVIEW_ID";

    private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;
    private final DafReviewTaskHandler dafReviewTaskHandler;
    private final DafHandler dafHandler;

    public CreateDafReviewTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
                                   BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler,
                                   DafReviewTaskHandler dafReviewTaskHandler,
                                   DafHandler dafHandler) {
        super(objectMapper, workflowUtil);
        this.basedOnTheOtherTaskHandler = basedOnTheOtherTaskHandler;
        this.dafReviewTaskHandler = dafReviewTaskHandler;
        this.dafHandler = dafHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws IOException {

        String reviewTaskId = getVariable(execution, TASK_TELUS_WS_DAF_REVIEW_ID, String.class);

        TaskV2DomainTask task;

        if (reviewTaskId == null) {
            task = dafReviewTaskHandler.createDafReviewTask(
                    getMandatoryVariable(execution, WF_TENANT_WS, String.class),
                    getMandatoryVariable(execution, WF_WS_ORDER, Long.class),
                    getMandatoryVariable(execution, WORKFLOW_ID, String.class)
            );
        } else {
            task = basedOnTheOtherTaskHandler.createNewTaskFromOldOne(
                    getMandatoryVariable(execution, WF_TENANT_WS, String.class),
                    reviewTaskId,
                    TaskType.TELUS_WS_DAF_REVIEW
            );
            dafHandler.updateStatusOfItems(
                    getMandatoryVariable(execution, WORKFLOW_ID, String.class),
                    DafStatus.IN_REVIEW
            );
        }

        if (task != null) {
            execution.setVariable(TASK_TELUS_WS_DAF_REVIEW_ID, task.getId());
            setTaskInfo(task.getId(), task.getTaskDefinition(), getMandatoryVariable(execution, WF_TENANT_WS, String.class), execution);
        }
    }
}
