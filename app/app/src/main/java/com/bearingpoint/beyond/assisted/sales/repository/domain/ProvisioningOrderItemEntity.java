package com.bearingpoint.beyond.test-bpintegration.repository.domain;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "PROVISIONING_ORDER_ITEMS", schema = "assisted_sales")
public class ProvisioningOrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long retailOrderId;
    private Long retailBillingAccountId;
    private Long opiNumber;
    private Long wholesaleOrderId;
    private Long wholesaleBillingAccountId;
    private Long taskId;
    private String wholesaleServiceOffer;
    private String wholesaleService;
    private String wholesaleServiceOrderId;
    private String dafWorkflowId;
    private String dafStatus;
    private Long serviceProvisioningId;
    private String tenant;
    private Instant created;
    private Instant updated;
    private Instant deleteAfter;
    private Long orderQuantity;
    private Long requestedQuantity;
    private Instant requestedActivationDate;
    private Instant earliestActivationDate;


    @PreUpdate
    void preUpdate() {
        if (this.updated == null) {
            this.updated = Instant.now();
        }
    }

    @PrePersist
    void created() {
        if (this.created == null) {
            this.created = Instant.now();
        }
    }

}
