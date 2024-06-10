package com.bearingpoint.beyond.test-bpintegration.camunda.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkflowTypes {
    STATEMENT_OF_WORK ("StatementOfWork", "SOW"),
    WHOLESALE_PROVISIONING("WholesaleProvisioning", "WsProvisioning"),
    INFLIGHT_WHOLESALE_PROVISIONING("InflightOrderWholesaleProvisioning", "InflightWsProvisioning"),
    DELIVERY_ACCEPTANCE_FORM("DeliveryAcceptanceForm", "DAF"),
    CHANGE_ORDER("ChangeOrder", "CO"),
    MODIFY_ORDER("ModifyOrder", "MO")
    ;

    private final String workflowName;
    private final String shortName;

}
