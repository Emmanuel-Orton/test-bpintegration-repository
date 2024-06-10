package com.bearingpoint.beyond.test-bpintegration.camunda.service.sow.cleanup;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.CleanupHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class CleanSowWorkflowStep extends AbstractBaseStep {

    private final CleanupHandler cleanupHandler;

    public CleanSowWorkflowStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil, CleanupHandler cleanupHandler) {
        super(objectMapper, workflowUtil);
        this.cleanupHandler = cleanupHandler;
    }

    @Override
    public boolean hasRetry() {
        return false;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {
        execution.setVariable(WF_CANCELLED, true);

        try {
            if (execution.hasVariable(WF_CURRENT_TASK)) {
                Object currentTaskRaw = execution.getVariable(WF_CURRENT_TASK);
                log.debug("currentTask: {}", currentTaskRaw);

                if (currentTaskRaw!=null) {
                    TaskInfo currentTask = workflowUtil.getTaskInfo(currentTaskRaw);
                    cleanupHandler.cleanupTask(currentTask);
                }
            } else {
                log.debug("CleanSowWorkflow: cleanup not needed, no Task opened.");
            }
        } catch (Exception ex) {
            log.error("Error in cleanup step. Ignoring error: " + ex.getMessage());
        }

    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.RETAIL;
    }

}
