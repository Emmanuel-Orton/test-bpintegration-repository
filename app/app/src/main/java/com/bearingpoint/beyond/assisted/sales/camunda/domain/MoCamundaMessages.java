package com.bearingpoint.beyond.test-bpintegration.camunda.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MoCamundaMessages {

    MO_INITIATE_TASK_RESOLVED("MO_initiate_task_resolved"),
    MO_REVIEW_TASK_RESOLVED("MO_review_task_resolved"),
    MO_RETAIL_MO_CREATED_RESOLVED("MO_RT_mo_created_task_resolved"),
    MO_APPROVE_TASK_RESOLVED("MO_approved_task_resolved"),
    MO_UPDATE_TASK_RESOLVED("MO_update_task_resolved"),
    MO_MODIFY_ORDER_CREATED_RESOLVED("MO_modify_order_created_task_resolved"),
    MO_FINALIZE_ORDER_RESOLVED("MO_finalize_order_task_resolved")
    ;

    @Getter
    private final String message;

}
