package com.bearingpoint.beyond.test-bpintegration.api.frontend;

import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowStatus;
import com.bearingpoint.beyond.test-bpintegration.camunda.domain.WorkflowVariable;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowErrorsAndRetries;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.TaskV2DomainTaskEvent;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.handler.sow.SowHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/{tenant}")
@Api(tags = "TelusAssistedSalesV1Api")
@RequiredArgsConstructor
public class WorkflowController {

    private final SowHandler sowHandler;
    private final WorkflowUtil workflowUtil;
    private final WorkflowErrorsAndRetries workflowErrorsAndRetries;
    private final TasksService tasksService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/v1/workflow/workflowStatus/{businessKey}")
    @ApiOperation(value = "Get Workflow Status."
            , notes = "Get Workflow Status.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public WorkflowStatus getBaseWorkflowStatus(@RequestHeader(name = "env", required = false) String env,
                                            @PathVariable("tenant") String tenant,
                                            @PathVariable String businessKey) {
        WorkflowStatus workflowStatus = sowHandler.getWorkflowStatus(businessKey);

        log.debug("GET Workflow Status for businessKey: {}, workflowStatus: {}", businessKey, workflowStatus);

        return workflowStatus;
    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/v1/workflow/workflowVariable/{businessKey}/variable/{variableName}")
    @ApiOperation(value = "Get Workflow Status."
            , notes = "Get Workflow Status.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public WorkflowVariable getVariableName(@RequestHeader(name = "env", required = false) String env,
                                            @PathVariable("tenant") String tenant,
                                            @PathVariable String businessKey,
                                            @PathVariable String variableName) {
        WorkflowVariable workflowStatus = workflowUtil.getVariableValue(businessKey, variableName);

        log.debug("Get Workflow Variable {} for businessKey: {}, result: {}", variableName, businessKey, workflowStatus);

        return workflowStatus;
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(value = "/event/v1/workflowErrorTaskResolved")
    @ApiOperation(value = "Handle the resolve initial payment task event"
            , notes = "Handle the resolve initial payment task event")
    @ApiResponses({
            @ApiResponse(code = 202, message = "ACCEPTED"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public void retryWorkflow(@RequestHeader(name = "env", required = false) String env,
                              @PathVariable("tenant") String tenant,
                              @RequestBody TaskV2DomainTaskEvent taskResolvedEvent) throws JsonProcessingException {
        log.debug("Retry Workflow: {}", taskResolvedEvent.getEvent().getCurrent().getId());
        workflowErrorsAndRetries.retry(taskResolvedEvent);
    }


//    @ResponseStatus(HttpStatus.OK)
//    @GetMapping("/zumzum")
//    @ApiOperation(value = "For testing logging"
//            , notes = "For testing logging")
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "OK"),
//            @ApiResponse(code = 400, message = "BAD REQUEST")
//    })
//    public void testQuery(@RequestHeader(name = "env", required = false) String env) {
//
//        TaskV2DomainTask task = new TaskV2DomainTask()
//                .billingAccount("1192241444")
//                .taskDefinition("TELUS_WS_DAF_APPROVEDDD");
//
//        tasksService.createTask("SB_TELUS_ANDYROZMAN", task);
//    }

}
