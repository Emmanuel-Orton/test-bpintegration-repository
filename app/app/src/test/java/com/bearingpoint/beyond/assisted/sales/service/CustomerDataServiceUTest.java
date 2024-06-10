package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.parties.model.PartyV1DomainOrganizationOrganization;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CustomerDataServiceUTest {

    private static final String WS_OFFER_NAME = "WS_OFFER";
    private static final String RT_OFFER_NAME = "RT_OFFER";
    private static final String WS_TENANT = "tenantWs";
    public static final String TENANT = "tenant";

    public static final String WS_BILLING_ACCOUNT_ID = "32732782";
    private static final String TRADING_NAME = "tradingName";


    @Mock
    private ProductOfferingsService productOfferingsService;
    @Mock
    private ProductInventoryService productInventoryService;
    @Mock
    private PartyService partyService;

    @InjectMocks
    private CustomerDataService customerDataService;


    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(customerDataService, "wsTenant", WS_TENANT);
    }


    @Test
    public void getWholesaleBillingAccount() {
        when(productOfferingsService.getWholesaleOfferName(TENANT, RT_OFFER_NAME)).thenReturn(WS_OFFER_NAME);
        when(productInventoryService.getWholesaleAgreementBillingAccount(WS_TENANT, WS_OFFER_NAME)).thenReturn(WS_BILLING_ACCOUNT_ID);

        String wholesaleBillingAccount = customerDataService.getWholesaleBillingAccount(TENANT, RT_OFFER_NAME);

        assertThat(wholesaleBillingAccount, is(WS_BILLING_ACCOUNT_ID));
    }


    @Test
    public void getCustomerTradingName() {
        PartyV1DomainOrganizationOrganization organization = new PartyV1DomainOrganizationOrganization().tradingName(TRADING_NAME);
        when(partyService.getCustomersOrganization(TENANT, WS_BILLING_ACCOUNT_ID)).thenReturn(organization);

        String customerTradingName = customerDataService.getCustomerTradingName(TENANT, WS_BILLING_ACCOUNT_ID);

        assertThat(customerTradingName, is(TRADING_NAME));
    }
}