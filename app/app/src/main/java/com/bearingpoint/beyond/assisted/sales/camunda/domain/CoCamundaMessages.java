package com.bearingpoint.beyond.test-bpintegration.camunda.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CoCamundaMessages {

    CO_REVIEW_TASK_RESOLVED("CO_review_task_resolved"),
    CO_RETAIL_CO_CREATED_RESOLVED("CO_RT_co_created_task_resolved"),
    CO_APPROVE_TASK_RESOLVED("CO_approved_task_resolved"),
    CO_UPDATE_TASK_RESOLVED("CO_update_task_resolved"),
    CO_ORDER_MODIFICATION_CREATED_RESOLVED("CO_order_modification_created_task_resolved"),
    ;

    @Getter
    private final String message;

}
