package com.bearingpoint.beyond.test-bpintegration.api.frontend.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvisioningOrderItemsList {
    @JsonProperty("list")
    List<ProvisioningOrderItemDto> list;
}
