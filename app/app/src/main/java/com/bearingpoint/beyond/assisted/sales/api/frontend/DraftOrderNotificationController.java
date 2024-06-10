package com.bearingpoint.beyond.test-bpintegration.api.frontend;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DraftOrderNotification;
import com.bearingpoint.beyond.test-bpintegration.service.DraftOrderNotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping(value = "/{tenant}")
@Api(tags = "TelusAssistedSalesV1Api")
@RequiredArgsConstructor
public class DraftOrderNotificationController {

    private final DraftOrderNotificationService draftOrderNotificationService;

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(value = "/v1/draftOrdering/{draftOrder}")
    @ApiOperation(value = "Sending customer approval notification for draft order."
            , notes = "Sending customer approval notification for draft order.")
    @ApiResponses({
            @ApiResponse(code = 202, message = "ACCEPTED"),
            @ApiResponse(code = 400, message = "BAD REQUEST")
    })
    public void sendNotificationAndUpdateOrder(@RequestHeader(name = "env", required = false) String env,
                              @PathVariable("tenant") String tenant,
                              @PathVariable("draftOrder") String draftOrder,
                              @RequestBody DraftOrderNotification dratOrderNotification) throws IOException {
        log.debug("Sending customer approval notification for {}", draftOrder);
        draftOrderNotificationService.sendNotificationAndUpdateOrder(tenant, draftOrder, dratOrderNotification);
    }


}