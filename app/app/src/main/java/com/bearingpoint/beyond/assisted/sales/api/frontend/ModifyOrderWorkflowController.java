package com.bearingpoint.beyond.test-bpintegration.api.frontend;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.ModifyOrderStartParameters;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.WorkflowStartResponse;
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
import org.springframework.http.ResponseEntity;
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
public class ModifyOrderWorkflowController {

    private final WorkflowUtil workflowUtil;


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/v1/modifyOrder/startModifyOrder")
    @ApiOperation(value = "Handling creation of MO workflow."
            , notes = "Handling creation of MO workflow.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public ResponseEntity<WorkflowStartResponse> startModifyOrderWorkflow(@RequestHeader(name = "env", required = false) String env,
                                                                          @PathVariable("tenant") String tenant,
                                                                          @RequestBody ModifyOrderStartParameters startParameters) {
        log.debug("Received startModifyWorkflowWorkflow request: {}", startParameters);

        try {

            String id = workflowUtil.startModifyOrderWorkflow(tenant, startParameters);

            if (id == null) {
                return new ResponseEntity<>(WorkflowStartResponse.builder()
                        .workflowId("MO_" + startParameters.getServiceInstanceName())
                        .started(false)
                        .alreadyRunning(true)
                        .build(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(WorkflowStartResponse.builder()
                        .workflowId(id)
                        .started(true)
                        .alreadyRunning(false)
                        .build(), HttpStatus.OK);
            }
        } catch (Exception ex) {
            log.error("Error starting Modify Order workflow. Ex: " + ex.getMessage(), ex);
            return new ResponseEntity("Error starting Modify Order workflow. Ex: " + ex.getMessage(), HttpStatus.BAD_REQUEST );
        }

    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/v1/modifyOrder/workflowStatus/{serviceInstanceName}")
    @ApiOperation(value = "Get Modify Order Workflow Status."
            , notes = "Get Modify Order Workflow Status.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public WorkflowStatus getCoWorkflowStatus(@RequestHeader(name = "env", required = false) String env,
                                              @PathVariable("tenant") String tenant,
                                              @PathVariable String serviceInstanceName) {
        WorkflowStatus workflowStatus = workflowUtil.getWorkflowStatus("MO_" + serviceInstanceName, WorkflowTypes.MODIFY_ORDER);

        log.debug("modifyOrder status for serviceInstanceName: {}, workflowStatus: {}", serviceInstanceName, workflowStatus);

        return workflowStatus;
    }
}
