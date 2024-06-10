package com.bearingpoint.beyond.test-bpintegration.camunda.domain;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StepInfo {
    String stepId;
    String businessKey;
    String workflowType;
    String processInstanceId;
    String processDefinitionId;
}
