package com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.update;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.AbstractBaseStep;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.DafStatus;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.BasedOnTheOtherTaskHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_ID;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve.CreateDafApproveTaskStep.*;


@Slf4j
@Component
public class CreateDafUpdateTaskStep extends AbstractBaseStep {

    private final BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler;
    private final DafHandler dafHandler;

    public CreateDafUpdateTaskStep(ObjectMapper objectMapper, WorkflowUtil workflowUtil,
                                   BasedOnTheOtherTaskHandler basedOnTheOtherTaskHandler,
                                   DafHandler dafHandler) {
        super(objectMapper, workflowUtil);
        this.basedOnTheOtherTaskHandler = basedOnTheOtherTaskHandler;
        this.dafHandler = dafHandler;
    }

    @Override
    public boolean hasRetry() {
        return true;
    }

    @Override
    public void executeStep(DelegateExecution execution) throws IOException {

        TaskInfo previousTask = workflowUtil.getTaskInfo(getMandatoryVariable(execution, WF_PREVIOUS_TASK, String.class));

        TaskV2DomainTask task = basedOnTheOtherTaskHandler.createNewTaskFromOldOneWithClosingNote(
                previousTask.getTaskId(),
                previousTask.getTenant(),
                previousTask,
                TaskType.TELUS_WS_DAF_UPDATE
        );

        if (task != null) {
            setTaskInfo(task.getId(), task.getTaskDefinition(), getMandatoryVariable(execution, WF_TENANT_WS, String.class), execution);
        }

        dafHandler.updateStatusOfItems(
                getMandatoryVariable(execution, WORKFLOW_ID, String.class),
                DafStatus.IN_UPDATE
        );

        String overdueDateOld = getVariable(execution, OVERDUE_DATE, String.class);

        if (overdueDateOld != null) {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeZone(TimeZone.getTimeZone("UTC"));
            gc.add(Calendar.YEAR, 1);

            if (dafHandler.updateDafApprovalWarningTimer(execution, gc)) {
                execution.setVariable(OVERDUE_DATE, overdueDateSimpleDateFormat.format(gc.getTime()));
            } else {
                execution.removeVariable(OVERDUE_DATE);
            }
        }
    }

    @Override
    public TenantType getStepTenantType() {
        return TenantType.WHOLESALE;
    }
}
