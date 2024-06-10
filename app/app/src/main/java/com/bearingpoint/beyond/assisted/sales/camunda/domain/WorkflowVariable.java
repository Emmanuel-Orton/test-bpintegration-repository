package com.bearingpoint.beyond.test-bpintegration.camunda.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowVariable {
    private boolean running;
    private String stepId;
    private String businessKey;
    private String workflowType;
    private String processInstanceId;
    private String processDefinitionId;
    private String variableName;
    private String variableValue;
    private boolean error;
    private String errorMessage;
}
