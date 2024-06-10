package com.bearingpoint.beyond.test-bpintegration.camunda.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SowCamundaMessages {

    SOW_CREATE_TASK_RESOLVED("SOW_create_task_resolved"),
    SOW_REVIEW_TASK_RESOLVED("SOW_review_task_resolved"),
    SOW_APPROVE_TASK_RESOLVED("SOW_approve_task_resolved"),
    SOW_UPDATE_TASK_RESOLVED("SOW_update_task_resolved"),
    SOW_RT_APPROVE_TASK_RESOLVED("SOW_RT_approve_task_resolved"),
    SOW_VERIFICATION_TASK_RESOLVED("SOW_verification_task_resolved"),
    ;

    @Getter
    private final String message;
}
