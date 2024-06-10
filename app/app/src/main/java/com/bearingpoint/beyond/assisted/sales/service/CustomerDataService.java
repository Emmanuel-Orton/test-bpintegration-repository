package com.bearingpoint.beyond.test-bpintegration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerDataService {

    @Value("${tenant_ws_name}")
    private String wsTenant;

    private final ProductOfferingsService productOfferingsService;
    private final ProductInventoryService productInventoryService;
    private final PartyService partyService;

    public String getWholesaleBillingAccount(String tenant, String retailOrderName) {
        String offerNameWs = productOfferingsService.getWholesaleOfferName(tenant, retailOrderName);
        return productInventoryService.getWholesaleAgreementBillingAccount(wsTenant, offerNameWs);
    }

    public String getCustomerTradingName(String tenant, String customerId) {
        return partyService.getCustomersOrganization(tenant, customerId).getTradingName();
    }

    public String getCustomerLegalName(String tenant, String customerId) {
        return partyService.getCustomersOrganization(tenant, customerId).getLegalName();
    }
}
