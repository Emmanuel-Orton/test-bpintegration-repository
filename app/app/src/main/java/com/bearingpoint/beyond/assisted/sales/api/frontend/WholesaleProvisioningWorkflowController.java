package com.bearingpoint.beyond.test-bpintegration.api.frontend;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.ProvisioningOrderItemDto;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.ProvisioningOrderItemsList;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.ServiceorderingV1DomainServiceOrder;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
public class WholesaleProvisioningWorkflowController {

    private final ProvisioningHandler provisioningHandler;
    private final ObjectMapper objectMapper;


    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/serviceProvisioning/v1/telusWholesaleProvisioningStarted")
    @ApiOperation(value = "Handling starting of Wholesale provisioning workflow."
            , notes = "Handling starting of Wholesale provisioning workflow."
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "CREATED"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public ServiceorderingV1DomainServiceOrder startWsProvisioningWorkflow(@RequestHeader(name = "env", required = false) String env,
                                            @PathVariable("tenant") String tenant,
                                            @RequestBody ServiceorderingV1DomainServiceOrder serviceOrder) {
        if (log.isDebugEnabled()) {
            log.debug("startWsProvisioningWorkflow: event received: {}", objectMapper.writeValueAsString(serviceOrder));
        }

        provisioningHandler.startWorkflowIfNeeded(serviceOrder);


        if (log.isDebugEnabled()) {
            log.debug("Returning response that order with externalId {} was received. ", serviceOrder.getExternalId());
        }

        return serviceOrder;
    }


    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(value = "/serviceProvisioning/v1/telusWholesaleProvisioningStarted")
    @ApiOperation(value = "Handling inflight of Wholesale provisioning workflow."
            , notes = "Handling inflight of Wholesale provisioning workflow."
    )
    @ApiResponses({
            @ApiResponse(code = 202, message = "ACCEPTED"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    @SneakyThrows
    public ServiceorderingV1DomainServiceOrder startInflightWsProvisioningWorkflow(@RequestHeader(name = "env", required = false) String env,
                                                                           @PathVariable("tenant") String tenant,
                                                                           @RequestBody ServiceorderingV1DomainServiceOrder serviceOrder) {
        if (log.isDebugEnabled()) {
            log.debug("startInflightWsProvisioningWorkflow: event received: {}", objectMapper.writeValueAsString(serviceOrder));
        }

        provisioningHandler.startInflightOrderWorkflow(serviceOrder, tenant);

        log.debug("Return response for inflight order. state to set service order: {} to in InProgress.", serviceOrder.getExternalId());

        return serviceOrder;
    }



    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/v1/provisioning/{wholesaleOrderId}/items/unassigned")
    @ApiOperation(value = "Get Unassigned provisioning Items."
            , notes = "Get Unassigned provisioning Items.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public List<ProvisioningOrderItemDto> getProvisioningItemsUnassigned(@RequestHeader(name = "env", required = false) String env,
                                                               @PathVariable("tenant") String tenant,
                                                               @PathVariable Long wholesaleOrderId) {
        log.debug("GET getProvisioningItems for wholesaleOrderId: {}", wholesaleOrderId);

        return provisioningHandler.getWholesaleOrderItems(wholesaleOrderId, true);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/v1/provisioning/{wholesaleOrderId}/items/closed")
    @ApiOperation(value = "Check if order's items are closed."
            , notes = "Check is order's items close are closed.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public Boolean checkProvisioningItemsComplete(@RequestHeader(name = "env", required = false) String env,
            @PathVariable("tenant") String tenant,
            @PathVariable Long wholesaleOrderId) {
        log.debug("GET checkProvisioningItemsComplete for wholesaleOrderId: {}", wholesaleOrderId);

        return provisioningHandler.checkIsAllOrderItemsComplete(wholesaleOrderId);
    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/v1/provisioning/{wholesaleOrderId}/items/")
    @ApiOperation(value = "Get all provisioning Items (full)."
            , notes = "Get all provisioning Items (full).")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public ProvisioningOrderItemsList getProvisioningItems(@RequestHeader(name = "env", required = false) String env,
                                                               @PathVariable("tenant") String tenant,
                                                               @PathVariable Long wholesaleOrderId) {
        log.debug("GET getProvisioningItems for wholesaleOrderId: {}", wholesaleOrderId);

        return new ProvisioningOrderItemsList(provisioningHandler.getWholesaleOrderItems(wholesaleOrderId));
    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/v1/provisioning/retailOrder/{retailOrderId}/items/")
    @ApiOperation(value = "Get all provisioning Items by retailOrder Id."
            , notes = "Get all provisioning Items by retailOrder Id.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public ProvisioningOrderItemsList getProvisioningItemsByRetailId(@RequestHeader(name = "env", required = false) String env,
                                                                     @PathVariable("tenant") String tenant,
                                                                     @PathVariable Long retailOrderId) {
        log.debug("GET getProvisioningItems for retailOrderId: {}", retailOrderId);

        return new ProvisioningOrderItemsList(provisioningHandler.getWholesaleOrderItemsByRetailId(retailOrderId));
    }

}
