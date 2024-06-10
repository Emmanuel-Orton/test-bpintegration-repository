package com.bearingpoint.beyond.test-bpintegration.api.frontend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DafWorkflowStartResponse {
    String workflowId;
}
