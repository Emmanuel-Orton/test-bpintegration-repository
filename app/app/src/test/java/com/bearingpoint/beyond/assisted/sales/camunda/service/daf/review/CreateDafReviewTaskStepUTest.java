package com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.review;

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
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_TENANT_WS;
import static org.mockito.Mockito.doNothing;
import org.junit.jupiter.api.Disabled;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CreateDafReviewTaskStepUTest extends AbstractWorkflowStepTest {

    private static final String WS_ORDER = "7167099";
    @Mock
    DafHandler dafHandler;


    @Mock
    ProvisioningHandler provisioningHandler;

    @InjectMocks
    CreateDafReviewTaskStep unitToTest;

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
    public void executeStep_noReviewTask() throws Exception {
        expectGetMandatoryVariable(WF_WS_ORDER, WS_ORDER);

        doNothing().when(provisioningHandler)
                .raiseOrderServiceProvisioningTask(Long.parseLong(WS_ORDER));


//        String reviewTaskId = getVariable(execution, TASK_TELUS_WS_DAF_REVIEW_ID, String.class);
//
//        TaskV2DomainTask task;
//
//        if (reviewTaskId == null) {
//            task = dafReviewTaskHandler.createDafReviewTask(
//                    getMandatoryVariable(execution, WF_TENANT_WS, String.class),
//                    getMandatoryVariable(execution, WF_WS_ORDER, Long.class),
//                    getMandatoryVariable(execution, WORKFLOW_ID, String.class)
//            );
//        } else {
//            task = basedOnTheOtherTaskHandler.createNewTaskFromOldOne(
//                    getMandatoryVariable(execution, WF_TENANT_WS, String.class),
//                    reviewTaskId,
//                    TaskType.TELUS_WS_DAF_REVIEW
//            );
//            dafHandler.updateStatusOfItems(
//                    getMandatoryVariable(execution, WORKFLOW_ID, String.class),
//                    DafStatus.IN_REVIEW
//            );
//        }
//
//        if (task != null) {
//            execution.setVariable(TASK_TELUS_WS_DAF_REVIEW_ID, task.getId());
//            setTaskInfo(task.getId(), task.getTaskDefinition(), getMandatoryVariable(execution, WF_TENANT_WS, String.class), execution);
//        }



        // TODO implement
        unitToTest.executeStep(execution);
    }

    @Test
    @Disabled
    public void executeStep_reviewTaskFound() throws Exception {
        // TODO implement
        unitToTest.executeStep(execution);
    }

}

