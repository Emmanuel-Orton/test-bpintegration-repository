package com.bearingpoint.beyond.test-bpintegration.api.frontend.domain;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvisioningOrderItemDto {

    private Long id;
    private Long retailOrderId;
    private Long retailBillingAccountId;
    private Long opiNumber;
    private Long wholesaleOrderId;
    private Long wholesaleBillingAccountId;
    private String wholesaleServiceOffer;
    private String wholesaleService;
    private String wholesaleServiceOrderId;
    private String dafWorkflowId;
    private String dafStatus;
    private Long serviceProvisioningId;
    private String tenant;
    private Instant deleteAfter;
    private Long taskId;
    private Long orderQuantity;
    private Long requestedQuantity;
    private String requestedActivationDate; // yyyy-mm-dd
    private String earliestActivationDate;

}
