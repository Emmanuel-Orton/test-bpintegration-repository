package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.api.exception.InfonovaApiException;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.api.ServiceOrderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrderItem;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrderingNotification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrderingNotification.EventTypeEnum.ORDERINFLIGHTSTATECHANGENOTIFICATION;
import static com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrderingNotification.EventTypeEnum.ORDERSTATECHANGENOTIFICATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceOrderingService {

    private final ServiceOrderingV1Api serviceOrderingV1Api;
    private final ObjectMapper objectMapper;

    public ServiceorderingV1DomainServiceOrder getServiceOrderByExternalId(String externalId, String tenant) {
        return serviceOrderingV1Api.getServiceorderingV1Id(tenant, externalId)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }

    public void setServiceOrderInProgress(String tenant, String serviceOrderId, String requestId) throws JsonProcessingException {
        ServiceorderingV1DomainServiceOrderingNotification completeServiceOrder = new ServiceorderingV1DomainServiceOrderingNotification()
                .eventType(ORDERSTATECHANGENOTIFICATION)
                .id(UUID.randomUUID().toString())
                .dateTime(OffsetDateTime.now())
                .serviceOrder(
                        new ServiceorderingV1DomainServiceOrder()
                                .externalId(serviceOrderId)
                                .priority(4)
                                .cancelable(false)
                                .state("InProgress")
                );
        log.info("Set service order in progress:{} with requestId:{}", objectMapper.writeValueAsString(completeServiceOrder), requestId);
        serviceOrderingV1Api.postServiceorderingV1Notification(tenant, completeServiceOrder, requestId)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }

    public void sendOrderStateChangeNotification(String tenant, ServiceorderingV1DomainServiceOrder serviceOrder, String requestId) throws JsonProcessingException {
        ServiceorderingV1DomainServiceOrderingNotification completeServiceOrder = new ServiceorderingV1DomainServiceOrderingNotification()
                .eventType(ORDERSTATECHANGENOTIFICATION)
                .id(UUID.randomUUID().toString())
                .dateTime(OffsetDateTime.now())
                .serviceOrder(serviceOrder);

        if (!log.isDebugEnabled())
            log.info("Send ORDERSTATECHANGENOTIFICATION for service order (externalId={}) with state={} and requestId:{}", serviceOrder.getExternalId(), serviceOrder.getState(), requestId);
        else
            log.info("Send ORDERSTATECHANGENOTIFICATION for service order {} with state={} and requestId:{}", objectMapper.writeValueAsString(completeServiceOrder), serviceOrder.getState(), requestId);

        serviceOrderingV1Api.postServiceorderingV1Notification(tenant, completeServiceOrder, requestId)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }


    public void sendOrderInflightStateChangeNotification(String tenant, ServiceorderingV1DomainServiceOrder serviceOrder, String requestId, ServiceorderingV1DomainServiceOrderingNotification.InFlightOrderChangeStateEnum state) throws JsonProcessingException {
        ServiceorderingV1DomainServiceOrderingNotification completeServiceOrder = new ServiceorderingV1DomainServiceOrderingNotification()
                .eventType(ORDERINFLIGHTSTATECHANGENOTIFICATION)
                .id(UUID.randomUUID().toString())
                .dateTime(OffsetDateTime.now())
                .inFlightOrderChangeState(state)
                .serviceOrder(serviceOrder);

        if (!log.isDebugEnabled())
            log.info("Send ORDERINFLIGHTSTATECHANGENOTIFICATION for service order (externalId={}) with state={} and requestId:{}", completeServiceOrder.getServiceOrder().getExternalId(), state, requestId);
        else
            log.info("Send ORDERINFLIGHTSTATECHANGENOTIFICATION for service order {} with state={} and requestId:{}", objectMapper.writeValueAsString(completeServiceOrder), state, requestId);

        serviceOrderingV1Api.postServiceorderingV1Notification(tenant, completeServiceOrder, requestId)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }


    private void test() {

//        new ServiceorderingV1DomainServiceOrder()
//                .externalId(serviceOrderId)
//                .priority(4)
//                .cancelable(false)
//                .state("InProgress")
//                .addOrderItemsItem(new ServiceorderingV1DomainServiceOrderItem().orderItemAction())


    }



//    public void completeOrderItems(ServiceorderingV1DomainServiceOrder serviceOrderInput, ServiceKey serviceKey, ServiceorderingV1DomainServiceOrderItem.OrderItemActionEnum action, boolean ignoreItemsWithJustNoChangeCharacteristics) {
//        List<ServiceorderingV1DomainServiceOrderItem> orderItems = getOrderItems(serviceOrderInput, serviceKey, action);
//
//        if (ignoreItemsWithJustNoChangeCharacteristics) {
//
//            List<ServiceorderingV1DomainServiceOrderItem> orderItemsChanged = new ArrayList<>();
//
//            for (ServiceorderingV1DomainServiceOrderItem orderItem : orderItems) {
//                if (getCountOfCharacteristicsChanged(orderItem) > 0) {
//                    orderItemsChanged.add(orderItem);
//                }
//            }
//
//            if (orderItemsChanged.size() != orderItems.size()) {
//                orderItems = orderItemsChanged;
//            }
//        }
//
//        if (orderItems.size() > 0) {
//            setOrderItemsState(serviceOrderInput, orderItems, OrderItemState.COMPLETED);
//        }
//    }


}
