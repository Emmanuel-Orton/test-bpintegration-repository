package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
public class InfonovaLinkService {

    @Value("${infonovaMarketplaceUrl}")
    private String marketPlaceUrl; // RETAIL
    @Value("${infonovaPartnerhubUrl}")
    private String partnerHubUrl; // WS

    @Value("${tenant_ws_name}")
    private String wholesaleTenant;
    @Value("${tenant_name}")
    private String retailTenant;

    // links format: system url for tenant, api-prefix (r6-ui or r6-csm) and then tenant, other parameters are link specific
    public final String TASK_URL = "%s/%s/%s/index#!/casemanagement/tasks/%s?type=%s&category=%s";
    public final String ORDER_URL = "%s/%s/%s/index#!/customermanagement/customer/%s/offer/orders/%s";
    public final String DRAFT_ORDER_URL = "%s/%s/%s/index#!/customermanagement/customer/%s/add/wizard/orderdetails?prospectOrderId=%s&backTo=drafts";
    public final String CUSTOMER_ACCOUNT_LINK = "%s/%s/%s/index#!/customermanagement/customer/%s/account/profile";

    public String getOrderLinkFromTask(TaskV2DomainTask task, TenantType tenantType,
                                       String orderIdParam, String billingAccountParam) {

        if (CollectionUtils.isEmpty(task.getParameters()) ||
                !task.getParameters().containsKey(orderIdParam)) {
            log.error("No {} found. Link couldn't be created.", orderIdParam);
            return "No link.";
        }

        if (CollectionUtils.isEmpty(task.getParameters()) ||
                !task.getParameters().containsKey(billingAccountParam)) {
            log.error("No {} found. Link couldn't be created.", billingAccountParam);
            return "No link.";
        }

        return getOrderLink(tenantType,
                task.getParameters().get(orderIdParam),
                task.getParameters().get(billingAccountParam));
    }

    public String getRetailCustomerAccountLink(String billingAccount) {
        final TenantType rtTenant = TenantType.RETAIL;
        return String.format(CUSTOMER_ACCOUNT_LINK, getCorrectTenantUrl(rtTenant), getTenantApiPrefix(rtTenant),
                getCorrectTenant(rtTenant), billingAccount);
    }


    public String getOrderLink(TenantType tenantType, String orderId, String billingAccountId) {
        return String.format(ORDER_URL,
                getCorrectTenantUrl(tenantType),
                getTenantApiPrefix(tenantType),
                getCorrectTenant(tenantType),
                billingAccountId,
                orderId);
    }

    public String getDraftOrderLink(TenantType tenantType, String draftOrderId, String billingAccountId) {
        return String.format(DRAFT_ORDER_URL,
                getCorrectTenantUrl(tenantType),
                getTenantApiPrefix(tenantType),
                getCorrectTenant(tenantType),
                billingAccountId,
                draftOrderId);
    }

    private String getTaskLink(TenantType tenantType, String taskId, String taskDefinition, String taskCategory) {
        return String.format(TASK_URL,
                getCorrectTenantUrl(tenantType),
                getTenantApiPrefix(tenantType),
                getCorrectTenant(tenantType),
                taskId,
                taskDefinition,
                taskCategory);
    }

    public Object getTenantApiPrefix(TenantType tenantType) {
        return (tenantType == TenantType.WHOLESALE_CSM) ? "r6-csm" : "r6-ui";
    }

    public String getCorrectTenantUrl(TenantType tenantType) {
        switch (tenantType) {
            case RETAIL:
                return marketPlaceUrl;
            case WHOLESALE:
            case WHOLESALE_CSM:
                return partnerHubUrl;
        }
        return null;
    }

    public String getCorrectTenant(TenantType tenantType) {
        return tenantType == TenantType.RETAIL ?
                retailTenant : wholesaleTenant;
    }
}
