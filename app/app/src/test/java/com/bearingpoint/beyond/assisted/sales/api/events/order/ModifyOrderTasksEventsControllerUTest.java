package com.bearingpoint.beyond.test-bpintegration.api.events.order;

import com.bearingpoint.beyond.test-bpintegration.api.common.MockMvcMockitoTest;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEvent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.MoCamundaMessages.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ModifyOrderTasksEventsControllerUTest extends MockMvcMockitoTest {

    @InjectMocks
    private ModifyOrderTasksEventsController modifyOrderTasksEventsController;

    @Override
    protected Object getController() {
        return modifyOrderTasksEventsController;
    }

    @Test
    public void wsMoPartnerApproveTaskResolvedTest() throws Exception {
        final TaskV2DomainTaskEvent taskV2DomainTaskEvent = prepareTaskEvent();

        mockMvc.perform(post(TENANT_URL + "/event/v1/telusWsMoPartnerApproveTaskResolved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskV2DomainTaskEvent))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(workflowUtil).handleTaskEventAndCreateMessage(taskV2DomainTaskEvent, WorkflowUtil.WORKFLOW_ID, true,
                MO_APPROVE_TASK_RESOLVED.getMessage(),TENANT);
    }


    @Test
    public void rtMoCreatedTaskResolved() throws Exception {
        final TaskV2DomainTaskEvent taskV2DomainTaskEvent = prepareTaskEvent();

        mockMvc.perform(post(TENANT_URL + "/event/v1/telusRtMoCreatedTaskResolved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskV2DomainTaskEvent))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(workflowUtil).handleTaskEventAndCreateMessage(taskV2DomainTaskEvent, WorkflowUtil.WORKFLOW_ID, true,
                MO_RETAIL_MO_CREATED_RESOLVED.getMessage(),TENANT);
    }


    @Test
    public void wsMoUpdateTaskResolved() throws Exception {
        final TaskV2DomainTaskEvent taskV2DomainTaskEvent = prepareTaskEvent();

        mockMvc.perform(post(TENANT_URL + "/event/v1/telusWsMoUpdateTaskResolved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskV2DomainTaskEvent))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(workflowUtil).handleTaskEventAndCreateMessage(taskV2DomainTaskEvent, WorkflowUtil.WORKFLOW_ID, true,
                MO_UPDATE_TASK_RESOLVED.getMessage(),TENANT);
    }


    @Test
    public void rtModifyOrderCreatedTaskResolved() throws Exception {
        final TaskV2DomainTaskEvent taskV2DomainTaskEvent = prepareTaskEvent();

        mockMvc.perform(post(TENANT_URL + "/event/v1/telusRtModifyOrderCreatedTaskResolved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskV2DomainTaskEvent))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(workflowUtil).handleTaskEventAndCreateMessage(taskV2DomainTaskEvent, WorkflowUtil.WORKFLOW_ID, true,
                MO_MODIFY_ORDER_CREATED_RESOLVED.getMessage(),TENANT);
    }


    @Test
    public void moReviewTaskResolvedTest() throws Exception {
        final TaskV2DomainTaskEvent taskV2DomainTaskEvent = prepareTaskEvent();

        mockMvc.perform(post(TENANT_URL + "/event/v1/telusWsMoReviewTaskResolved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskV2DomainTaskEvent))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(workflowUtil).handleTaskEventAndCreateMessage(taskV2DomainTaskEvent, WorkflowUtil.WORKFLOW_ID, true,
                MO_REVIEW_TASK_RESOLVED.getMessage(),TENANT);
    }


    @Test
    @Disabled
    public void wsStartModifyOrderWorkflow() throws Exception {
        final TaskV2DomainTaskEvent taskV2DomainTaskEvent = prepareTaskEvent();

        // TODO Andy extend test when integrated

        mockMvc.perform(post(TENANT_URL + "/event/v1/telusWsStartModifyOrderWorkflow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskV2DomainTaskEvent))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(workflowUtil).handleTaskEventAndCreateMessage(taskV2DomainTaskEvent, WorkflowUtil.WORKFLOW_ID, true,
                MO_REVIEW_TASK_RESOLVED.getMessage(),TENANT);
    }

}
