package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.api.exception.InfonovaApiException;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.api.ProductOrderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.*;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceinventory.model.ServiceinventoryV1DomainService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductOrdersService {

    @Value("${tenant_name}")
    private String rtTenant;
    private final ProductOrderingV1Api productOrderingV1Api;
    private final ObjectMapper objectMapper;
    private final ServiceInventoryService serviceInventoryService;


    @Deprecated
    public void resolveEvent(String tenant, String eventId, String orderId, OrderResolution resolution) {
        switch (resolution) {
            case SUCCESS:
                productOrderingV1Api.postProductOrderingV1EventsProductOrderProcessingStartEventIdSuccess(tenant,
                        eventId, new OrderingV1DomainOrderEventResponse().id(orderId), null)
                        .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                        .block();
                break;
            case FAILURE:
                productOrderingV1Api.postProductOrderingV1EventsProductOrderProcessingStartEventIdFailure(tenant,
                                eventId, null, new CommonsDomainModelMessageAware()
                                        .addMessagesItem(new CommonsDomainModelMessage().message("fail")))
                        .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                        .block();
                break;
        }
    }

    @Deprecated
    public void cancelOrder(String tenant, String orderId) {
        productOrderingV1Api.patchProductOrderingV1ProductOrdersId(tenant, orderId,
                        new ProductorderingV1DomainProductOrder().state("Cancelled"))
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new).block();
    }

    @Deprecated
    public void makeOrderCancelableAndChangeable(String tenant, String orderId) {
        productOrderingV1Api.patchProductOrderingV1ProductOrdersId(tenant, orderId,
                        new ProductorderingV1DomainProductOrder().cancelable(true).changeable(true))
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new).block();
    }

    public ProductorderingV1DomainDraftOrder getDraftOrder(String tenant, String draftOrderId) {
        String query = String.format("draftOrder:%s", draftOrderId);
        ProductorderingV1DomainDraftOrderList orders = productOrderingV1Api.getProductOrderingV1DraftOrders(tenant, null,
                        BigDecimal.ONE, BigDecimal.ONE, query, false, null, null, null)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new).block();
        return orders.getList().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Draft order %s not found", draftOrderId)));
    }

    public ProductorderingV1DomainProductOrder getProductOrder(String tenant, String orderId) {
        return productOrderingV1Api.getProductOrderingV1ProductOrdersId(tenant, orderId)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }

    public ProductorderingV1DomainProductOrderList getProductOrders(String tenant, String query) {
        return productOrderingV1Api.getProductOrderingV1ProductOrders(tenant, null, null, query, null, null)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }

    public ProductorderingV1DomainProductOrder getProductOrderWithProductReferenceAndType(String tenant, String productId, String type) {
        String query = String.format("productReference:%s type:\"%s\"", productId, type);
        return getProductOrders(tenant, query)
                .getList().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Couldn't find order for product with id %s and type %s on tenant %s", productId, type, tenant)));
    }

    @SneakyThrows
    public ProductorderingV1DomainShoppingCart createCartFromDraft(String tenant, String draftId) {
        ProductorderingV1DomainShoppingCart cartBody = new ProductorderingV1DomainShoppingCart()
                .draftId(draftId);

        return productOrderingV1Api.postProductOrderingV1Carts(tenant, cartBody, UUID.randomUUID().toString())
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new).block();
    }


    public ProductorderingV1DomainShoppingCart postShoppingCart(String tenant, ProductorderingV1DomainShoppingCart cartBody) {
        return productOrderingV1Api.postProductOrderingV1Carts(tenant, cartBody, UUID.randomUUID().toString())
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new).block();
    }

    public ProductorderingV1DomainShoppingCart patchShoppingCart(String tenant, ProductorderingV1DomainShoppingCart cartBody) {
        return productOrderingV1Api.patchProductOrderingV1CartsCart(tenant, cartBody.getId(), cartBody)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new).block();
    }

    public ProductorderingV1DomainProductOrder submitShoppingCart(String tenant, ProductorderingV1DomainShoppingCart shoppingCart) {
        return productOrderingV1Api.postProductOrderingV1CartsCartSubmit(tenant,
                        shoppingCart.getId(), new ProductorderingV1DomainProductOrderDetails(), null)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new).block();
    }

    public ProductorderingV1DomainProductOrder submitShoppingCartWithStartDate(String tenant, ProductorderingV1DomainShoppingCart shoppingCart, OffsetDateTime startDateTime) {
        ProductorderingV1DomainProductOrderDetails productOrderDetails = new ProductorderingV1DomainProductOrderDetails();
        productOrderDetails.setRequestedCompletionDate(startDateTime);

        return productOrderingV1Api.postProductOrderingV1CartsCartSubmit(tenant,
                        shoppingCart.getId(), productOrderDetails, null)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new).block();
    }

    public ProductorderingV1DomainShoppingCart patchShoppingCart(String tenant, String shoppingCartId, ProductorderingV1DomainShoppingCart shoppingCart) {
        return productOrderingV1Api.patchProductOrderingV1CartsCart(tenant, shoppingCartId, shoppingCart)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new).block();
    }

    public ProductorderingV1DomainProductOrder getRetailOrderForTheWholesale(ProductorderingV1DomainProductOrder wsOrder) {
        final String rtOrderId = wsOrder.getRelatedObjects().stream()
                .filter((relatedObject) -> "Order".equals(relatedObject.getType()) && "ParentOrder".equals(relatedObject.getOrderType()))
                .map((relatedObject) -> relatedObject.getId())
                .findFirst().orElse("");

        if (StringUtils.isBlank(rtOrderId)) {
            throw new IllegalArgumentException(String.format("No retail order id found for order '%s'", wsOrder.getId()));
        }

        return productOrderingV1Api.getProductOrderingV1ProductOrdersId(rtTenant, rtOrderId)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }

    public ProductorderingV1SummaryDomainCustomerOrderSummary getOrderSummary(String cartId, String tenant) {
        return productOrderingV1Api.getProductOrderingV1CartsCartCustomerOrderSummary(tenant, cartId)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }


    public ProductorderingV1DomainProductOrder getProductOrderWithServiceIdentifier(String serviceIdentifier, String tenant) {
        ServiceinventoryV1DomainService service = serviceInventoryService.getServiceByServiceIdentifier(tenant, serviceIdentifier);
        String relatedProduct = service.getProductRelationships().get(0).getRelatedProduct();

        ProductorderingV1DomainProductOrder order = getProductOrderWithProductReferenceAndType(tenant, relatedProduct, "NEW");

        log.info("Found Product order with id {} on tenant {} that corresponds to serviceIdentifier {}",
                order!=null ? order.getId(): "null", tenant, serviceIdentifier);

        return order;
    }

    public enum OrderResolution {
        SUCCESS,
        FAILURE
    }
}
