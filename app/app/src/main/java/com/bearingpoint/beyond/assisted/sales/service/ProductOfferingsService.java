package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.api.exception.InfonovaApiException;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productcatalog.api.ProductCatalogV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productcatalog.model.CommonsDomainModelConfigurationAttribute;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productcatalog.model.ProductofferingV1DomainProductOffering;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.OffsetDateTime;
import java.util.Comparator;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductOfferingsService {

    private static final String RELATED_WS_OFFER = "RELATED_WS_OFFER";

    private final ProductCatalogV1Api productCatalogV1Api;

    public String getWholesaleOfferName(String retailTenant, String retailOfferName) {
        ProductofferingV1DomainProductOffering retailOffering = productCatalogV1Api
                .getProductCatalogV1ProductOfferingsName(retailTenant, retailOfferName)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new).block();

        CommonsDomainModelConfigurationAttribute wsOrderName = retailOffering.getConfigurationAttributes().stream()
                .filter(configAttribute -> StringUtils.equals(configAttribute.getName(), RELATED_WS_OFFER)
                        && configAttribute.getValidFor() != null
                        && configAttribute.getValidFor().getStart() != null
                        && configAttribute.getValidFor().getStart().isBefore(OffsetDateTime.now()))
                .max(Comparator.comparing(configAttribute -> configAttribute.getValidFor().getStart()))
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("%s does not have configuration attribute with value %s or it is missing the validFor.start attribute", retailOfferName, RELATED_WS_OFFER)));
        return wsOrderName.getValue();
    }

    public String getProductOfferingInternalName(String tenant, String productOffering) {
        return productCatalogV1Api.getProductCatalogV1ProductOfferingsName(tenant, productOffering)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new).block()
                .getInternalName();
    }
}
