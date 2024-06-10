package com.bearingpoint.beyond.test-bpintegration.camunda.domain;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TaskInfo implements Serializable {

    private static final long serialVersionUID = 4837483781L;

    String taskId;
    String taskDefinition;
    String tenant;
}
