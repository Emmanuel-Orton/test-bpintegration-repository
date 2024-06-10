package com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.update;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.TaskInfo;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.AbstractWorkflowStepTest;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.DafStatus;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.*;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve.CreateDafApproveTaskStep.OVERDUE_DATE;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.approve.CreateDafApproveTaskStep.overdueDateSimpleDateFormat;
import static org.mockito.Mockito.doNothing;
import org.junit.jupiter.api.Disabled;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CreateDafUpdateTaskStepUTest extends AbstractWorkflowStepTest {

    private static final String CURRENT_TENANT = "SB_TELUS_SIT";
    private static final String TASK_ID = "71670932";
    private static final String TASK_TENANT = "SB_TELUS_SIT";
//    private static final String WS_ORDER = "7167099";
//    private static final String WS_PROVISIONING_ID = "1965282";
//    private static final String WS_CUSTOMER = "874374734";
//    private static final String ADDED_ORDER_ITEMS = "1, 2, 3";

    @Mock
    DafHandler dafHandler;


    @Mock
    ProvisioningHandler provisioningHandler;

    @InjectMocks
    CreateDafUpdateTaskStep unitToTest;

    @BeforeEach
    public void setUp() {
        setObjectMapper(unitToTest);
    }

    @Test
    public void hasRetry() {
        expectHasRetry(unitToTest, true);
    }


    @Test
    public void getStepTenantType() {
        expectStepTenantType(unitToTest, TenantType.WHOLESALE);
    }


    @Test
    @Disabled
    public void executeStep() throws Exception {
        //expectGetMandatoryVariable(WF_WS_ORDER, WS_ORDER);

        // TODO Andy implement this

        expectGetTaskInfo(WF_PREVIOUS_TASK, TASK_ID, TASK_TENANT);

//        doNothing().when(provisioningHandler)
//                .raiseOrderServiceProvisioningTask(Long.parseLong(WS_ORDER));
//
//
//        TaskInfo previousTask = workflowUtil.getTaskInfo(getMandatoryVariable(execution, WF_PREVIOUS_TASK, String.class));
//
//        TaskV2DomainTask task = basedOnTheOtherTaskHandler.createNewTaskFromOldOneWithClosingNote(
//                previousTask.getTaskId(),
//                previousTask.getTenant(),
//                previousTask,
//                TaskType.TELUS_WS_DAF_UPDATE
//        );
//
//        if (task != null) {
//            setTaskInfo(task.getId(), task.getTaskDefinition(), getMandatoryVariable(execution, WF_TENANT_WS, String.class), execution);
//        }
//
//        dafHandler.updateStatusOfItems(
//                getMandatoryVariable(execution, WORKFLOW_ID, String.class),
//                DafStatus.IN_UPDATE
//        );
//
//        String overdueDateOld = getVariable(execution, OVERDUE_DATE, String.class);
//
//        if (overdueDateOld != null) {
//            GregorianCalendar gc = new GregorianCalendar();
//            gc.setTimeZone(TimeZone.getTimeZone("UTC"));
//            gc.add(Calendar.YEAR, 1);
//
//            if (dafHandler.updateDafApprovalWarningTimer(execution, gc)) {
//                execution.setVariable(OVERDUE_DATE, overdueDateSimpleDateFormat.format(gc.getTime()));
//            } else {
//                execution.removeVariable(OVERDUE_DATE);
//            }
//        }
//


        unitToTest.executeStep(execution);
    }


}

