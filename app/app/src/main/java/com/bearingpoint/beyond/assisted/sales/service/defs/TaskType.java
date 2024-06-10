package com.bearingpoint.beyond.test-bpintegration.service.defs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskType {

    TELUS_RT_ORDER_VERIFICATION(TenantType.RETAIL, "modify_order"),
    TELUS_WS_SOW_CREATE(TenantType.WHOLESALE, "approved"),
    TELUS_WS_SOW_UPDATE(TenantType.WHOLESALE, "modify_order"),
    TELUS_WS_SOW_REVIEW(TenantType.WHOLESALE, "approved"),
    TELUS_WS_SOW_APPROVE(TenantType.WHOLESALE, "approved"),
    TELUS_RT_SOW_CREATE(TenantType.RETAIL, "approved"),
    TELUS_WS_DAF_REVIEW(TenantType.WHOLESALE, "approved"),
    TELUS_WS_DAF_UPDATE(TenantType.WHOLESALE, "review"),
    TELUS_WS_DAF_APPROVE(TenantType.WHOLESALE, "approved"),
    TELUS_WS_CO_REVIEW(TenantType.WHOLESALE, "approved"),
    TELUS_WS_CO_PARTNER_APPROVE(TenantType.WHOLESALE, "completed"),
    TELUS_RT_CO_CREATION(TenantType.RETAIL, "completed"),
    TELUS_WS_CO_UPDATE(TenantType.WHOLESALE, "review"),
    TELUS_RT_ORDER_MODIFICATION(TenantType.RETAIL, "completed"),
    TELUS_WS_CO_DAY2_INITIATE(TenantType.WHOLESALE, "completed"),

    TELUS_WS_CO_DAY2_REVIEW(TenantType.WHOLESALE, "approved"),
    TELUS_WS_CO_DAY2_UPDATE(TenantType.WHOLESALE, "review"),
    TELUS_RT_CO_DAY2_CREATE(TenantType.RETAIL, "completed"),
    TELUS_WS_CO_DAY2_FINALIZE(TenantType.WHOLESALE, "completed"),
    ;

    private final TenantType sourceTenant;
    private final String goodPathResolution;

}
