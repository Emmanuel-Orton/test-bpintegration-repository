package com.bearingpoint.beyond.test-bpintegration.api.events.order;

import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEvent;
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

import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.CoCamundaMessages.*;

@Slf4j
@RestController
@RequestMapping(value = "/{tenant}")
@Api(tags = "TelusAssistedSalesV1Api")
@RequiredArgsConstructor
public class CoTasksEventsController {

    private final ObjectMapper objectMapper;
    private final WorkflowUtil workflowUtil;


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsCoReviewTaskResolved")
    @ApiOperation(value = "Handling resolution of Co Review task."
            , notes = "Handling resolution of Co Review task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void coReviewTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                           @PathVariable("tenant") String tenant,
                                           @RequestBody TaskV2DomainTaskEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Ws Co Review Resolved event received {}", objectMapper.writeValueAsString(event));
        }
        workflowUtil.handleTaskEventAndCreateMessage(event, WorkflowUtil.WORKFLOW_ID, true,
                CO_REVIEW_TASK_RESOLVED.getMessage(), tenant );
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsCoPartnerApproveTaskResolved")
    @ApiOperation(value = "Handling resolution of Co Partner Approve task."
            , notes = "Handling resolution of Co Partner Approve task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void coPartnerApproveTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                              @PathVariable("tenant") String tenant,
                                              @RequestBody TaskV2DomainTaskEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Ws Co Partner Approve Task Resolved event received {}", objectMapper.writeValueAsString(event));
        }
        workflowUtil.handleTaskEventAndCreateMessage(event, WorkflowUtil.WORKFLOW_ID, true,
                CO_APPROVE_TASK_RESOLVED.getMessage(), tenant );
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusRtCoCreateTaskResolved")
    @ApiOperation(value = "Handling resolution of Retail Co Create task."
            , notes = "Handling resolution of Retail Co Create task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void rtCoCreateTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                     @PathVariable("tenant") String tenant,
                                     @RequestBody TaskV2DomainTaskEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Rt Co Create Resolved event received {}", objectMapper.writeValueAsString(event));
        }
        workflowUtil.handleTaskEventAndCreateMessage(event, WorkflowUtil.WORKFLOW_ID, true,
                CO_RETAIL_CO_CREATED_RESOLVED.getMessage(), tenant );
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsCoUpdateTaskResolved")
    @ApiOperation(value = "Handling resolution of Co Update Approve task."
            , notes = "Handling resolution of Co Update Approve task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void coUpdateTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                        @PathVariable("tenant") String tenant,
                                        @RequestBody TaskV2DomainTaskEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Ws Co Update Task Resolved event received {}", objectMapper.writeValueAsString(event));
        }
        workflowUtil.handleTaskEventAndCreateMessage(event, WorkflowUtil.WORKFLOW_ID, true,
                CO_UPDATE_TASK_RESOLVED.getMessage(), tenant);
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusRtCoOrderModificationTaskResolved")
    @ApiOperation(value = "Handling resolution of Rt Co Order Modification task."
            , notes = "Handling resolution of Rt Co Order Modification task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void rtCoOrderModificationTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                              @PathVariable("tenant") String tenant,
                                              @RequestBody TaskV2DomainTaskEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Rt Co Order Modification Task Resolved event received {}", objectMapper.writeValueAsString(event));
        }
        workflowUtil.handleTaskEventAndCreateMessage(event, WorkflowUtil.WORKFLOW_ID, true,
                CO_ORDER_MODIFICATION_CREATED_RESOLVED.getMessage(), tenant);
    }
}
