package com.bearingpoint.beyond.test-bpintegration.api.events.order;

import com.bearingpoint.beyond.test-bpintegration.api.common.MockMvcMockitoTest;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEvent;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.CoCamundaMessages.CO_APPROVE_TASK_RESOLVED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.CoCamundaMessages.CO_ORDER_MODIFICATION_CREATED_RESOLVED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.CoCamundaMessages.CO_RETAIL_CO_CREATED_RESOLVED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.CoCamundaMessages.CO_REVIEW_TASK_RESOLVED;
import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.CoCamundaMessages.CO_UPDATE_TASK_RESOLVED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CoTasksEventsControllerUTest extends MockMvcMockitoTest {

    @InjectMocks
    private CoTasksEventsController coTasksEventsController;

    @Override
    protected Object getController() {
        return coTasksEventsController;
    }

    @Test
    public void cpPartnerApproveTaskResolvedTest() throws Exception {
        final TaskV2DomainTaskEvent taskV2DomainTaskEvent = prepareTaskEvent();

        mockMvc.perform(post(TENANT_URL + "/event/v1/telusWsCoPartnerApproveTaskResolved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskV2DomainTaskEvent))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(workflowUtil).handleTaskEventAndCreateMessage(taskV2DomainTaskEvent, WorkflowUtil.WORKFLOW_ID, true,
                CO_APPROVE_TASK_RESOLVED.getMessage(),TENANT);

    }


    @Test
    public void rtCoCreateTaskResolvedTest() throws Exception {
        final TaskV2DomainTaskEvent taskV2DomainTaskEvent = prepareTaskEvent();

        mockMvc.perform(post(TENANT_URL + "/event/v1/telusRtCoCreateTaskResolved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskV2DomainTaskEvent))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(workflowUtil).handleTaskEventAndCreateMessage(taskV2DomainTaskEvent, WorkflowUtil.WORKFLOW_ID, true,
                CO_RETAIL_CO_CREATED_RESOLVED.getMessage(),TENANT);

    }


    @Test
    public void coUpdateTaskResolvedTest() throws Exception {
        final TaskV2DomainTaskEvent taskV2DomainTaskEvent = prepareTaskEvent();

        mockMvc.perform(post(TENANT_URL + "/event/v1/telusWsCoUpdateTaskResolved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskV2DomainTaskEvent))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(workflowUtil).handleTaskEventAndCreateMessage(taskV2DomainTaskEvent, WorkflowUtil.WORKFLOW_ID, true,
                CO_UPDATE_TASK_RESOLVED.getMessage(),TENANT);

    }


    @Test
    public void rtCoOrderModificationTaskResolvedTest() throws Exception {
        final TaskV2DomainTaskEvent taskV2DomainTaskEvent = prepareTaskEvent();

        mockMvc.perform(post(TENANT_URL + "/event/v1/telusRtCoOrderModificationTaskResolved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskV2DomainTaskEvent))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(workflowUtil).handleTaskEventAndCreateMessage(taskV2DomainTaskEvent, WorkflowUtil.WORKFLOW_ID, true,
                CO_ORDER_MODIFICATION_CREATED_RESOLVED.getMessage(),TENANT);

    }


    @Test
    public void coReviewTaskResolvedTest() throws Exception {
        final TaskV2DomainTaskEvent taskV2DomainTaskEvent = prepareTaskEvent();

        mockMvc.perform(post(TENANT_URL + "/event/v1/telusWsCoReviewTaskResolved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskV2DomainTaskEvent))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(workflowUtil).handleTaskEventAndCreateMessage(taskV2DomainTaskEvent, WorkflowUtil.WORKFLOW_ID, true,
                CO_REVIEW_TASK_RESOLVED.getMessage(),TENANT);

    }

}
