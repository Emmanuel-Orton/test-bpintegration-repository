package com.bearingpoint.beyond.test-bpintegration.api.events.order;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEvent;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TaskType;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.DafCamundaMessages.*;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WORKFLOW_ID;
import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WS_ORDER_ID;
import static com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler.WF_APPROVAL_WARNING_TIMER_CANCELLED;

@Slf4j
@RestController
@RequestMapping(value = "/{tenant}")
@Api(tags = "TelusAssistedSalesV1Api")
@RequiredArgsConstructor
public class DafTasksEventsController {

    private final ObjectMapper objectMapper;
    private final WorkflowUtil workflowUtil;
    private final DafHandler dafHandler;


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsDafReviewTaskResolved")
    @ApiOperation(value = "Handling resolution for WS DAF Review Task."
            , notes = "Handling resolution for DAF Review Task created.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void wsDafReviewCloseAction(@RequestHeader(name = "env", required = false) String env,
                                       @PathVariable("tenant") String tenant,
                                       @RequestBody TaskV2DomainTaskEvent taskEvent) {
        if (log.isDebugEnabled()) {
            log.debug("Ws DAF Review Task Resolved event received {}", objectMapper.writeValueAsString(taskEvent));
        }
        workflowUtil.handleTaskResolutionEvent(taskEvent, WORKFLOW_ID, DAF_REVIEW_TASK_RESOLVED.getMessage(), tenant);

        if (taskEvent.getEvent().getCurrent().getResolutionAction().equals("approved")) {
            dafHandler.updateDatesFromTask(taskEvent.getEvent().getCurrent());
        }


    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsDafApproveTaskResolved")
    @ApiOperation(value = "Handling resolution for WS DAF Approve Task."
            , notes = "Handling resolution for WS DAF Approve Task.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void wsDafApproveCloseAction(@RequestHeader(name = "env", required = false) String env,
                                        @PathVariable("tenant") String tenant,
                                        @RequestBody TaskV2DomainTaskEvent taskEvent) {
        if (log.isDebugEnabled()) {
            log.debug("Ws DAF Approve Task Resolved event received {}", objectMapper.writeValueAsString(taskEvent));
        }

        workflowUtil.handleTaskEventAndCreateMessageWithVariableSet(taskEvent, WORKFLOW_ID, true,
                DAF_APPROVE_TASK_RESOLVED.getMessage(), tenant,
                WF_APPROVAL_WARNING_TIMER_CANCELLED, "true");
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsDafApproveTaskUpdated")
    @ApiOperation(value = "Handling update for WS DAF Approve Task."
            , notes = "Handling update for WS DAF Approve Task.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void wsDafApproveUpdateAction(@RequestHeader(name = "env", required = false) String env,
                                         @PathVariable("tenant") String tenant,
                                         @RequestBody TaskV2DomainTaskEvent taskEvent) {
        if (log.isDebugEnabled()) {
            log.debug("Ws DAF Approve Task Updated event received {}", objectMapper.writeValueAsString(taskEvent));
        }

        if (dafHandler.hasApprovalDueDateChanged(taskEvent)) {
            log.info("Ws DAF Approve Task Updated event received, OVERDUE_DATE has changed, updating timer.");
            dafHandler.updateDafApprovalWarningTimer(taskEvent.getEvent().getCurrent());
        } else {
            log.info("Ws DAF Approve Task Updated event received, but OVERDUE_DATE has not changed.");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsDafUpdateTaskResolved")
    @ApiOperation(value = "Handling resolution for WS DAF Update Task."
            , notes = "Handling resolution for WS DAF Update Task")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void dafWsUpdateTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                        @PathVariable("tenant") String tenant,
                                        @RequestBody TaskV2DomainTaskEvent taskEvent) {
        if (log.isDebugEnabled()) {
            log.debug("DAF WS Update Task Resolved event received {}", objectMapper.writeValueAsString(taskEvent));
        }
        workflowUtil.handleTaskResolutionEvent(taskEvent, WORKFLOW_ID, DAF_UPDATE_TASK_RESOLVED.getMessage(), tenant);
    }
}
