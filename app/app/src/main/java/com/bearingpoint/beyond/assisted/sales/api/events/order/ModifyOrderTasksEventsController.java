package com.bearingpoint.beyond.test-bpintegration.api.events.order;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.ModifyOrderStartParameters;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTask;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.camunda.domain.MoCamundaMessages.*;

@Slf4j
@RestController
@RequestMapping(value = "/{tenant}")
@Api(tags = "TelusAssistedSalesV1Api")
@RequiredArgsConstructor
public class ModifyOrderTasksEventsController {

    private final ObjectMapper objectMapper;
    private final WorkflowUtil workflowUtil;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsMoInitiateTaskResolved")
    @ApiOperation(value = "Handling resolution of ModifyOrder Initiate task."
            , notes = "Handling resolution of ModifyOrder (Change Order Day 2) Initiate task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public void wsMoInitiateTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                               @PathVariable("tenant") String tenant,
                                               @RequestBody TaskV2DomainTaskEvent event) throws JsonProcessingException {
        if (log.isDebugEnabled()) {
            log.debug("Ws Mo Initiate Resolved event received {}", objectMapper.writeValueAsString(event));
        }

        workflowUtil.handleTaskEventAndCreateMessage(event, WorkflowUtil.WORKFLOW_ID, true,
                MO_INITIATE_TASK_RESOLVED.getMessage(), tenant );
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsMoReviewTaskResolved")
    @ApiOperation(value = "Handling resolution of Mo Review task."
            , notes = "Handling resolution of Mo Review task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void wsMoReviewTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                           @PathVariable("tenant") String tenant,
                                           @RequestBody TaskV2DomainTaskEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Ws Mo Review Resolved event received {}", objectMapper.writeValueAsString(event));
        }
        workflowUtil.handleTaskEventAndCreateMessage(event, WorkflowUtil.WORKFLOW_ID, true,
                MO_REVIEW_TASK_RESOLVED.getMessage(), tenant );
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsMoPartnerApproveTaskResolved")
    @ApiOperation(value = "Handling resolution of Mo Partner Approve task."
            , notes = "Handling resolution of Mo Partner Approve task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void  wsMoPartnerApproveTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                              @PathVariable("tenant") String tenant,
                                              @RequestBody TaskV2DomainTaskEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Ws Mo Partner Approve Task Resolved event received {}", objectMapper.writeValueAsString(event));
        }
        workflowUtil.handleTaskEventAndCreateMessage(event, WorkflowUtil.WORKFLOW_ID, true,
                MO_APPROVE_TASK_RESOLVED.getMessage(), tenant );
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusRtMoCreatedTaskResolved")
    @ApiOperation(value = "Handling resolution of Retail Mo Create task."
            , notes = "Handling resolution of Retail Mo Create task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void rtMoCreatedTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                     @PathVariable("tenant") String tenant,
                                     @RequestBody TaskV2DomainTaskEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Rt Mo Create Resolved event received {}", objectMapper.writeValueAsString(event));
        }
        workflowUtil.handleTaskEventAndCreateMessage(event, WorkflowUtil.WORKFLOW_ID, true,
                MO_RETAIL_MO_CREATED_RESOLVED.getMessage(), tenant );
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsMoUpdateTaskResolved")
    @ApiOperation(value = "Handling resolution of Mo Update Approve task."
            , notes = "Handling resolution of Mo Update Approve task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void wsMoUpdateTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                        @PathVariable("tenant") String tenant,
                                        @RequestBody TaskV2DomainTaskEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Ws Co Update Task Resolved event received {}", objectMapper.writeValueAsString(event));
        }
        workflowUtil.handleTaskEventAndCreateMessage(event, WorkflowUtil.WORKFLOW_ID, true,
                MO_UPDATE_TASK_RESOLVED.getMessage(), tenant);
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusRtModifyOrderCreatedTaskResolved")
    @ApiOperation(value = "Handling resolution of Rt Co Order Modification task."
            , notes = "Handling resolution of Rt Co Order Modification task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void rtModifyOrderCreatedTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                              @PathVariable("tenant") String tenant,
                                              @RequestBody TaskV2DomainTaskEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Rt Modify Order Created Task Resolved event received {}", objectMapper.writeValueAsString(event));
        }

        workflowUtil.handleTaskEventAndCreateMessage(event, WorkflowUtil.WORKFLOW_ID, true,
                MO_MODIFY_ORDER_CREATED_RESOLVED.getMessage(), tenant);
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/event/v1/telusWsMoFinalizeOrderWorkflow")
    @ApiOperation(value = "Handling resolution of Finalize Order task."
            , notes = "Handling resolution of Finalize Order (Change Order Day 2) task."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public void wsMoFinalTaskResolved(@RequestHeader(name = "env", required = false) String env,
                                       @PathVariable("tenant") String tenant,
                                       @RequestBody TaskV2DomainTaskEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Ws Mo Finalize Resolved event received {}", objectMapper.writeValueAsString(event));
        }
        workflowUtil.handleTaskEventAndCreateMessage(event, WorkflowUtil.WORKFLOW_ID, true,
                MO_FINALIZE_ORDER_RESOLVED.getMessage(), tenant );

    }
}
