package com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_ORDER;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve.CreateDafApproveTaskStep.WF_DAF_APPROVAL_TASK_ID;

@Slf4j
@Component
public class SendApprovalWarningStep extends AbstractBaseStep {

    private final DafHandler dafHandler;

    private final TasksService tasksService;

    public SendApprovalWarningStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil, DafHandler dafHandler, TasksService tasksService) {
        super(objectMapper, workflowUtil);
        this.dafHandler = dafHandler;
        this.tasksService = tasksService;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {

        String approvalTaskId = getVariable(execution, WF_DAF_APPROVAL_TASK_ID, String.class);

        if (approvalTaskId!=null) {
            TaskV2DomainTask task;
            String tenant = getMandatoryVariable(execution, WF_TENANT_WS, String.class);

            try {
                log.info("DAF_APPROVAL_TASK_ID parameter found {}.", approvalTaskId);
                task = tasksService.getTask(tenant, approvalTaskId);
            } catch (Exception ex) {
                log.error("Task with id {}, could not be found.", approvalTaskId);
                return;
            }

            if ("Resolved".equals(task.getState())) {
                log.warn("Task is already resolved. Approval Warning notification won't be sent.");
                return;
            }

            try {
                task.getParameters().put("SEND_OVERDUE_NOTIFICATION", "true");
                tasksService.updateTask(tenant, task);
                log.info("DAF_APPROVAL_TASK was updated with SEND_OVERDUE_NOTIFICATION parameter.");
            } catch (Exception ex) {
                log.error("Task with id {}, could not be updated.", approvalTaskId);
                return;
            }

            log.info("Notification scheduled to be sent on {}", new Date());

        } else {
            log.warn("DAF_APPROVAL_TASK_ID not found. Task was probably closed,");
        }
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }

}
