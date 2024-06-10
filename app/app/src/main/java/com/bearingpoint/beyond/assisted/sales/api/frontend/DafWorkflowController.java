package com.bearingpoint.beyond.test-bpintegration.api.frontend;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DafStartParameters;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DafWorkflowStartResponse;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.ProvisioningOrderItemDto;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowStatus;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowTypes;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.service.handler.daf.DafHandler;
import com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning.ProvisioningHandler;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/{tenant}")
@Api(tags = "TelusAssistedSalesV1Api")
@RequiredArgsConstructor
public class DafWorkflowController {

    private final ObjectMapper objectMapper;
    private final DafHandler dafHandler;
    private final ProvisioningHandler provisioningHandler;
    private final WorkflowUtil workflowUtil;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/v1/daf/DAFProcessStart")
    @ApiOperation(value = "Handling starting of Daf Process workflow."
            , notes = "Handling starting of Daf Process workflow."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public ResponseEntity<DafWorkflowStartResponse> startDafProcessWorkflow(@RequestHeader(name = "env", required = false) String env,
                                                                            @PathVariable("tenant") String tenant,
                                                                            @RequestBody DafStartParameters startParameters) {
        if (log.isDebugEnabled()) {
            log.debug("startDafProcessWorkflow: {}", objectMapper.writeValueAsString(startParameters));
        }

        try {
            String id = dafHandler.startWorkflow(startParameters);
            return new ResponseEntity<>(DafWorkflowStartResponse.builder().workflowId(id).build(), HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new ResponseEntity(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/v1/daf/DAFProcessStatus/{workflowId}")
    @ApiOperation(value = "Get all items of specified Daf process."
            , notes = "Get all items of specified Daf process."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public List<ProvisioningOrderItemDto> getDafProcessStatus(@RequestHeader(name = "env", required = false) String env,
                                                              @PathVariable("tenant") String tenant,
                                                              @PathVariable String workflowId) {
        log.debug("getDafProcessStatus for workflow (id={})", workflowId);

        return provisioningHandler.getWholesaleOrderItems(workflowId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/v1/daf/workflowStatus/{wholesaleOrderId}")
    @ApiOperation(value = "Get DAF Workflow Status."
            , notes = "Get DAF Workflow Status.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public WorkflowStatus getDafWorkflowStatus(@RequestHeader(name = "env", required = false) String env,
                                               @PathVariable("tenant") String tenant,
                                               @PathVariable String wholesaleOrderId) {
        WorkflowStatus workflowStatus = workflowUtil.getWorkflowStatus("DAF_" + wholesaleOrderId, WorkflowTypes.DELIVERY_ACCEPTANCE_FORM);

        log.debug("DAF getWorkflowStatus for id: {}, workflowStatus: {}", "DAF_" + wholesaleOrderId, workflowStatus);

        return workflowStatus;
    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/v1/daf/unDafedItemsAvailable/{wholesaleOrderId}")
    @ApiOperation(value = "Return true if there are any items not in Daf process."
            , notes = "Return true if there are any items not in Daf process.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public boolean getUnDafedItemsAvailable(@RequestHeader(name = "env", required = false) String env,
                                                                         @PathVariable("tenant") String tenant,
                                                                         @PathVariable Long wholesaleOrderId) {
        List<ProvisioningOrderItemDto> wholesaleOrderItems = provisioningHandler.getWholesaleOrderItems(wholesaleOrderId, true);
        log.debug("Check if we have any unDafed items for wholesaleOrder {}: {}", wholesaleOrderId, !wholesaleOrderItems.isEmpty());
        return !wholesaleOrderItems.isEmpty();
    }


}
