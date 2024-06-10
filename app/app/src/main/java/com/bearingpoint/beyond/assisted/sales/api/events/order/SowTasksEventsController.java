package com.bearingpoint.beyond.test-bpintegration.api.events.order;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.SowCamundaMessages.*;

@Slf4j
@RestController
@RequestMapping(value = "/{tenant}")
@Api(tags = "TelusAssistedSalesV1Api")
@RequiredArgsConstructor
public class SowTasksEventsController {

    private final ObjectMapper objectMapper;
    private final WorkflowUtil workflowUtil;


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/sowCreationTaskResolved")
    @ApiOperation(value = "Handling resolution of SOW Creation task."
            , notes = "Handling resolution of SOW Creation task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public void wsSowCreationTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                        @PathVariable("tenant") String tenant,
                                        @RequestBody TaskV2DomainTaskEvent event) throws JsonProcessingException {
        if (log.isDebugEnabled()) {
            log.debug("Ws Sow Created Task Resolved event received {}", objectMapper.writeValueAsString(event));
        }
        workflowUtil.handleTaskResolutionEvent(event, WorkflowUtil.DRAFT_ORDER_ID,
                SOW_CREATE_TASK_RESOLVED.getMessage(), tenant );
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsSowReviewTaskResolved")
    @ApiOperation(value = "Handling resolution for WS SOW Review Task."
            , notes = "Handling resolution for SOW Review Task created.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void wsSowReviewCloseAction(@RequestHeader(name = "env", required = false) String env,
                                      @PathVariable("tenant") String tenant, @RequestBody TaskV2DomainTaskEvent taskEvent) {
        if (log.isDebugEnabled()) {
            log.debug("Ws Som Review Task Resolved event received {}", objectMapper.writeValueAsString(taskEvent));
        }
        workflowUtil.handleTaskResolutionEvent(taskEvent, WorkflowUtil.DRAFT_ORDER_ID,
                SOW_REVIEW_TASK_RESOLVED.getMessage(), tenant);
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsSowApproveTaskResolved")
    @ApiOperation(value = "Handling resolution for WS SOW Approve Task."
            , notes = "Handling resolution for WS SOW Approve Task.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void wsSowApproveCloseAction(@RequestHeader(name = "env", required = false) String env,
                                         @PathVariable("tenant") String tenant, @RequestBody TaskV2DomainTaskEvent taskEvent) {
        if (log.isDebugEnabled()) {
            log.debug("Ws Som Review Task Resolved event received {}", objectMapper.writeValueAsString(taskEvent));
        }
        workflowUtil.handleTaskResolutionEvent(taskEvent, WorkflowUtil.DRAFT_ORDER_ID,
                SOW_APPROVE_TASK_RESOLVED.getMessage(), tenant);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusRetailSowApproveTaskResolved")
    @ApiOperation(value = "Handling resolution for Create Retail SOW Approve Task."
            , notes = "Handling resolution for Create Retail SOW Approve Task.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void createRetailSowApproveCloseAction(@RequestHeader(name = "env", required = false) String env,
                                        @PathVariable("tenant") String tenant, @RequestBody TaskV2DomainTaskEvent taskEvent) {
        if (log.isDebugEnabled()) {
            log.debug("Create Retail SOW Task Resolved event received {}", objectMapper.writeValueAsString(taskEvent));
        }
        workflowUtil.handleTaskResolutionEvent(taskEvent, WorkflowUtil.DRAFT_ORDER_ID,
                SOW_RT_APPROVE_TASK_RESOLVED.getMessage(), tenant);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsSowUpdateTaskResolved")
    @ApiOperation(value = "Handling resolution for WS Sow Update Task."
            , notes = "Handling resolution for WS Sow Update Task")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void sowWsUpdateTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                                  @PathVariable("tenant") String tenant, @RequestBody TaskV2DomainTaskEvent taskEvent) {
        if (log.isDebugEnabled()) {
            log.debug("Sow WS Update Task Resolved event received {}", objectMapper.writeValueAsString(taskEvent));
        }
        workflowUtil.handleTaskResolutionEvent(taskEvent, WorkflowUtil.DRAFT_ORDER_ID,
                SOW_UPDATE_TASK_RESOLVED.getMessage(), tenant);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/orderVerificationTaskResolved")
    @ApiOperation(value = "Handling resolution of Order Verification task."
            , notes = "Handling resolution of Order Verification task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void orderVerificationTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                              @PathVariable("tenant") String tenant,
                                              @RequestBody TaskV2DomainTaskEvent taskEvent) {
        if (log.isDebugEnabled()) {
            log.debug("Order Verification Task Resolved event received {}", objectMapper.writeValueAsString(taskEvent));
        }
        workflowUtil.handleTaskEventAndCreateMessage(taskEvent, WorkflowUtil.DRAFT_ORDER_ID, true,
                SOW_VERIFICATION_TASK_RESOLVED.getMessage(), tenant );
    }

}
