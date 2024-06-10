package com.bearingpoint.beyond.test-bpintegration.camunda.service.co;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.co.ChangeOrderHandler;
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
public class CreateWsCoReviewTaskStep extends AbstractBaseStep {
	public static final String TASK_TELUS_WS_CO_REVIEW_ID = "TASK_TELUS_WS_CO_REVIEW_ID";

	private final ChangeOrderHandler changeOrderHandler;
	private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;

    public CreateWsCoReviewTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
                                    ChangeOrderHandler changeOrderHandler,
                                    BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler) {
        super(objectMapper, workflowUtil);
        this.changeOrderHandler = changeOrderHandler;
        this.basedOnTheOtherTaskHandler = basedOnTheOtherTaskHandler;
    }

	@Override
	public boolean hasRetry() {
		return true;
	}

    @Override
    public void executeStep(DelegateExecution execution) throws IOException {
        String reviewTaskId = getVariable(execution, TASK_TELUS_WS_CO_REVIEW_ID, String.class);

		TaskV2DomainTask task = null;

		if (reviewTaskId == null) {
			task = changeOrderHandler.createReviewTask(
					getMandatoryVariable(execution, WF_TENANT_WS, String.class),
					getMandatoryVariable(execution, WF_WS_ORDER, Long.class),
					getMandatoryVariable(execution, WORKFLOW_ID, String.class)
			);

			execution.setVariable(TASK_TELUS_WS_CO_REVIEW_ID, task.getId());
		} else {
			task = basedOnTheOtherTaskHandler.createNewTaskFromOldOne(
					getMandatoryVariable(execution, WF_TENANT_WS, String.class),
					reviewTaskId,
					TaskType.TELUS_WS_CO_REVIEW
			);
		}

		if (task != null) {
			setTaskInfo(task.getId(), task.getTaskDefinition(), getMandatoryVariable(execution, WF_TENANT_WS, String.class),
					execution, true);
		}
	}

	@Override
	public TenantType getStepTenantType() {
		return TenantType.WHOLESALE;
	}
}
