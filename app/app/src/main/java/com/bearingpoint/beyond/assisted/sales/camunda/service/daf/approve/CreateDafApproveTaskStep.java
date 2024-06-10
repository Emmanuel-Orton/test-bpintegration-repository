package com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.DafStatus;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_WS_ORDER;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_ID;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.review.CreateDafReviewTaskStep.TASK_TELUS_WS_DAF_REVIEW_ID;


@Slf4j
@Component
public class CreateDafApproveTaskStep extends AbstractBaseStep {

    public static final String OVERDUE_DATE = "OVERDUE_DATE";
    public static final String WF_DAF_APPROVAL_TASK_ID = "DAF_APPROVAL_TASK_ID";
    public static final SimpleDateFormat overdueDateSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final DafHandler dafHandler;


    public CreateDafApproveTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
                                    DafHandler dafHandler) {
        super(objectMapper, workflowUtil);
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
    public void executeStep(DelegateExecution execution) throws JsonProcessingException {

        String reviewTaskId = getVariable(execution, TASK_TELUS_WS_DAF_REVIEW_ID, String.class);
        String overdueDateOld = getVariable(execution, OVERDUE_DATE, String.class);
        String workflowId = getMandatoryVariable(execution, WORKFLOW_ID, String.class);

        GregorianCalendar overdueDate = new GregorianCalendar(TimeZone.getTimeZone("UTC")); //   OffsetDateTime.now();
        overdueDate.add(Calendar.DAY_OF_YEAR, 7);

        String overDueDateString = overdueDateSimpleDateFormat.format(overdueDate.getTime());

        TaskV2DomainTask task = dafHandler.createDafApproveTask(
                getMandatoryVariable(execution, WF_TENANT_WS, String.class),
                reviewTaskId,
                overDueDateString,
                String.valueOf(getMandatoryVariable(execution, WF_WS_ORDER, Long.class))
        );

        execution.setVariable(OVERDUE_DATE, overDueDateString);
        execution.setVariable(WF_DAF_APPROVAL_TASK_ID, task.getId());

        log.debug("overdueDateOld: {}", overdueDateOld == null ? "null" : overdueDateOld);

        if (overdueDateOld == null) {
            log.debug("startDafApprovalWarningSubprocess if not started before");
            dafHandler.startDafApprovalWarningSubprocess(workflowId);
        } else {
            if (!dafHandler.updateDafApprovalWarningTimer(execution, overdueDate)) {
                log.debug("startDafApprovalWarningSubprocess because update of DafApprovalWarning failed (Job/Timer not found).");
                dafHandler.startDafApprovalWarningSubprocess(workflowId);
            }
        }

        dafHandler.updateStatusOfItems(
                getMandatoryVariable(execution, WORKFLOW_ID, String.class),
                DafStatus.IN_APPROVAL
        );


        setTaskInfo(task.getId(), task.getTaskDefinition(), getMandatoryVariable(execution, WF_TENANT_WS, String.class), execution);
    }
}
