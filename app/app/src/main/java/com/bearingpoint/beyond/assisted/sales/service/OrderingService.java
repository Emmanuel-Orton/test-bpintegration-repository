package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.api.exception.InfonovaApiException;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.ordering.api.OrderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.ordering.model.OrderingV1DomainOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.ordering.model.OrderingV1DomainOrderList;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.api.ProductOrderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderingService {

    private final OrderingV1Api orderingV1Api;

    public OrderingV1DomainOrderList getOrderHierarchy(String tenant, String orderId) {
        return orderingV1Api.getOrderingV1OrdersOrderHierarchy(tenant, orderId)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }

    public OrderingV1DomainOrder getOrder(String tenant, String orderId) {
        return orderingV1Api.getOrderingV1OrdersOrder(tenant, orderId)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }

}
