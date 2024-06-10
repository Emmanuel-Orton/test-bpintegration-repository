package com.bearingpoint.beyond.test-bpintegration.api.frontend;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowStatus;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.service.handler.sow.SowHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/{tenant}")
@Api(tags = "TelusAssistedSalesV1Api")
@RequiredArgsConstructor
public class SowWorkflowController {

    private final SowHandler sowHandler;
    private final WorkflowUtil workflowUtil;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/v1/startSowWorkflow/{draftOrder}")
    @ApiOperation(value = "Handling creation of SOW workflow."
            , notes = "Handling creation of SOW workflow.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public void startSowWorkflow(@RequestHeader(name = "env", required = false) String env,
                                 @PathVariable("tenant") String tenant,
                                 @PathVariable String draftOrder) {
        if (log.isDebugEnabled()) {
            log.debug("Received draft order for startSowWorkflow {}", draftOrder);
        }
        sowHandler.startWorkflow(draftOrder);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/v1/workflowStatus/{draftOrderId}")
    @ApiOperation(value = "Get SOW Workflow Status."
            , notes = "Get SOW Workflow Status.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public WorkflowStatus getSowWorkflowStatus(@RequestHeader(name = "env", required = false) String env,
                                               @PathVariable("tenant") String tenant,
                                               @PathVariable String draftOrderId) {
        WorkflowStatus workflowStatus = sowHandler.getWorkflowStatus(draftOrderId);

        log.debug("GET Workflow Status for draftOrderId: {}, workflowStatus: {}", draftOrderId, workflowStatus);

        return workflowStatus;
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/v1/cancelSowWorkflow/{draftOrder}")
    @ApiOperation(value = "Handling cancellation of SOW workflow."
            , notes = "Handling cancellation of SOW workflow.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public void cancelSowWorkflow(@RequestHeader(name = "env", required = false) String env,
                                  @PathVariable("tenant") String tenant,
                                  @PathVariable String draftOrder) {
        log.debug("Received cancel signal for SowWorkflow for DraftOrderId {}", draftOrder);

        workflowUtil.cancelWorkflow(draftOrder, "SOW_Cancel_Request_Received",
                "SowWorkflow (draftOrderId=" + draftOrder + ")");
    }
}
