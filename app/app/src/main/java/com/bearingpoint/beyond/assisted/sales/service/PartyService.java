package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.api.exception.InfonovaApiException;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.parties.api.PartiesV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.parties.model.PartyV1DomainOrganizationOrganization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyService {

    private final PartiesV1Api partiesV1Api;

    public PartyV1DomainOrganizationOrganization getCustomersOrganization(String tenant, String customerId) {
        final String query = String.format("relatedEntities.entity:%s relatedEntities.entityType:CustomerAccount relatedEntities.role:CUSTOMER_ACCOUNT_OWNER", customerId);
        return partiesV1Api.getPartiesV1Organizations(tenant, null, null, BigDecimal.ONE, BigDecimal.valueOf(100),
                        query, null, null, null, null)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block()
                .getList().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Organization of customer with id %s not found", customerId)));
    }
}
