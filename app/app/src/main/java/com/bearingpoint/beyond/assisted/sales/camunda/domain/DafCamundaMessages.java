package com.bearingpoint.beyond.test-bpintegration.camunda.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DafCamundaMessages {

    DAF_REVIEW_TASK_RESOLVED("DAF_review_task_resolved"),
    DAF_APPROVE_TASK_RESOLVED("DAF_approve_task_resolved"),
    DAF_UPDATE_TASK_RESOLVED("DAF_update_task_resolved"),
    DAF_APPROVAL_WARNING_TIMER_START("DAF_approval_timer_start");

    @Getter
    private final String message;
}
