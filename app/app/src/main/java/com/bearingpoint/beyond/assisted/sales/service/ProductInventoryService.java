package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.api.exception.InfonovaApiException;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productinventory.api.ProductInventoryV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productinventory.model.ProductinventoryV1DomainProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductInventoryService {

    private final ProductInventoryV1Api productInventoryV1Api;

    public String getWholesaleAgreementBillingAccount(String tenant, String productName) {
        String query = String.format("productOffering:%s state:Active completenessState:Complete", productName);
        ProductinventoryV1DomainProduct product = productInventoryV1Api.getProductInventoryV1Products(tenant, null,
                        null, BigDecimal.ONE, BigDecimal.valueOf(100),query, null, null, null, null)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new).block()
                .getList().stream().findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Wholesale agreement billing account not found for productName %s", productName)));
        return product.getBillingAccount();

    }


}
