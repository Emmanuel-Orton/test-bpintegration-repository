package com.bearingpoint.beyond.test-bpintegration.api.frontend;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.CoStartParameters;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.CoWorkflowStartResponse;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowStatus;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowTypes;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/{tenant}")
@Api(tags = "TelusAssistedSalesV1Api")
@RequiredArgsConstructor
public class CoWorkflowController {

    private final WorkflowUtil workflowUtil;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/v1/changeOrder/startChangeOrder")
    @ApiOperation(value = "Handling creation of CO workflow."
            , notes = "Handling creation of CO workflow.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public CoWorkflowStartResponse startCoWorkflow(@RequestHeader(name = "env", required = false) String env,
                                                   @PathVariable("tenant") String tenant,
                                                   @RequestBody CoStartParameters startParameters) {

        log.debug("Received startCoWorkflow request: {}", startParameters);


        String id = workflowUtil.startCoWorkflow(startParameters);

        if (id == null) {
            return CoWorkflowStartResponse.builder()
                    .workflowId("CO_" + startParameters.getWholesaleOrderId())
                    .started(false)
                    .alreadyRunning(true)
                    .build();
        } else {
            return CoWorkflowStartResponse.builder()
                    .workflowId(id)
                    .started(true)
                    .alreadyRunning(false)
                    .build();
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/v1/changeOrder/workflowStatus/{wholesaleOrderId}")
    @ApiOperation(value = "Get CO Workflow Status."
            , notes = "Get CO Workflow Status.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public WorkflowStatus getCoWorkflowStatus(@RequestHeader(name = "env", required = false) String env,
                                              @PathVariable("tenant") String tenant,
                                              @PathVariable String wholesaleOrderId) {
        WorkflowStatus workflowStatus = workflowUtil.getWorkflowStatus("CO_" + wholesaleOrderId, WorkflowTypes.CHANGE_ORDER);

        log.debug("changeOrder getWorkflowStatus for wholesaleOrderId: {}, workflowStatus: {}", wholesaleOrderId, workflowStatus);

        return workflowStatus;
    }
}
