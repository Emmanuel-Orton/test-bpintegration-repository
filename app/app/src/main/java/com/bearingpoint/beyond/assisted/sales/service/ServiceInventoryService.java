package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.api.exception.InfonovaApiException;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productinventory.api.ProductInventoryV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productinventory.model.ProductinventoryV1DomainProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceinventory.api.ServiceInventoryV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceinventory.model.ServiceinventoryV1DomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceInventoryService {

    private final ServiceInventoryV1Api serviceInventoryV1Api;

    public ServiceinventoryV1DomainService getServiceByServiceIdentifier(String tenant, String serviceIdentifier) {
        String query = String.format("serviceIdentifier:%s", serviceIdentifier);
        return serviceInventoryV1Api.getServiceInventoryV1Services(tenant, query)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new).block()
                .getList().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Service with serviceIdentifier %s not found on tenant %s", serviceIdentifier, tenant)));
    }


}
