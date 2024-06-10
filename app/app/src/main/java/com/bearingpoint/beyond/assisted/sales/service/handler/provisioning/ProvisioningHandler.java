package com.bearingpoint.beyond.test-bpintegration.service.handler.provisioning;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.InflightOrderChangesDto;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.ProvisioningOrderItemDto;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil;
import com.bearingpoint.beyond.test-bpintegration.camunda.service.daf.DafStatus;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.ordering.model.OrderingV1DomainOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.ordering.model.OrderingV1DomainOrderList;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productinventory.api.ProductInventoryV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productinventory.model.ProductinventoryV1DomainProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainServiceCharacteristic;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceCharacteristic;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrderingNotification;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.ApiModelV1CommonsDomainRelatedParty;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.ServiceorderingV1DomainRelatedOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.ServiceorderingV1DomainServiceOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.ServiceorderingV1DomainServiceOrderItem;
import com.bearingpoint.beyond.test-bpintegration.repository.ProvisioningOrderItemsRepository;
import com.bearingpoint.beyond.test-bpintegration.repository.domain.ProvisioningOrderItemEntity;
import com.bearingpoint.beyond.test-bpintegration.service.InfonovaLinkService;
import com.bearingpoint.beyond.test-bpintegration.service.OrderingService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import com.bearingpoint.beyond.test-bpintegration.service.ServiceOrderingService;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bearingpoint.beyond.test-bpintegration.camunda.service.base.WorkflowUtil.WF_DRAFT_ORDER_PLACED_DT;
import static java.util.Map.entry;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProvisioningHandler {

    private final WorkflowUtil workflowUtil;
    private final ProvisioningOrderItemsRepository provisioningOrderItemsRepository;
    private final OrderingService orderingService;
    private final InfonovaLinkService infonovaLinkService;
    private final ObjectMapper objectMapper;
    private final ServiceOrderingService serviceOrderingService;
    private final TasksService tasksService;
    private final ProductInventoryV1Api productInventoryV1Api;
    private final ProductOrdersService productOrdersService;

    public static final String TELUS_WS_ORDER_SERVICE_PROVISIONING_TASK = "TELUS_WS_ORDER_SERVICE_PROVISIONING_TASK";
    public static final String RETAIL_BILLING_ACCOUNT_ID = "RETAIL_BILLING_ACCOUNT_ID";
    public static final String RETAIL_ORDER_ID = "RETAIL_ORDER_ID";
    public static final String PROVISIONING_ORDER_ID = "PROVISIONING_ORDER_ID";
    public static final String WS_ORDER_ID = "WS_ORDER_ID";
    public static final String WS_ORDER_LINK = "WS_ORDER_LINK";
    public static final String WHOLESALE_SERVICE_OFFER = "WHOLESALE_SERVICE_OFFER";
    public static final String WS_BILLING_ACCOUNT_ID = "WS_BILLING_ACCOUNT_ID";
    public static final String WS_SERVICE_ORDER_ID = "WS_SERVICE_ORDER_ID";
    public static final String PRODUCT_NAME = "PRODUCT_NAME";
    public static final String CLOSE = "close";
    public static final String ASSISTED_SALES_APP_ID = "telus_assisted_sales_app";
    public static final String TRAVEL_AND_EXPENSES_SERVICE = "TravelAndExpenses";

    public static final DateTimeFormatter dateSimpleDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter offsetFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public static final DateTimeFormatter offsetDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;


    @Value("${tenant_name}")
    private String retailTenant;
    @Value("${tenant_ws_name}")
    private String wholesaleTenant;

    public void startWorkflowIfNeeded(ServiceorderingV1DomainServiceOrder serviceOrder) {
        ServiceorderingV1DomainRelatedOrder retailRelatedOrder = getRelatedOrder(serviceOrder, 2);
        ServiceorderingV1DomainRelatedOrder wsRelatedOrder = getRelatedOrder(serviceOrder, 1);

        Optional<ApiModelV1CommonsDomainRelatedParty> tenant = serviceOrder.getRelatedParties().stream()
                .filter(pre -> "TENANT".equals(pre.getRole()))
                .findFirst();

        if (retailRelatedOrder==null || wsRelatedOrder==null) {
            log.warn("Related orders objects are not found. retail={}, ws={}",
                    retailRelatedOrder==null ? "null" : retailRelatedOrder,
                    wsRelatedOrder==null ? "null" : wsRelatedOrder
            );
            return;
        }

        ProcessInstance wfInstance = workflowUtil.getWorkflow(wsRelatedOrder.getId());

        if (wfInstance!=null) {
            log.warn("Workflow for wholesaleOrder (id={}) is already running. Exiting.", retailRelatedOrder.getId());
            return;
        }

        workflowUtil.startWholesaleProvisioningWorkflow(retailRelatedOrder.getId(),
                retailRelatedOrder.getOwner(),
                wsRelatedOrder.getId(),
                wsRelatedOrder.getOwner(),
                serviceOrder.getExternalId(),
                tenant.get().getName()
        );
    }

    private ServiceorderingV1DomainRelatedOrder getRelatedOrder(ServiceorderingV1DomainServiceOrder serviceOrder, int distance) {

        Optional<ServiceorderingV1DomainRelatedOrder> relatedOrderOpt = serviceOrder.getRelatedObjects().stream()
                .filter(pre -> "ParentOrder".equals(pre.getOrderType()) && pre.getDistance() == distance)
                .findFirst();

        if (relatedOrderOpt.isEmpty()) {
            log.error("Couldn't find related ParentOrder object with distance of {}. Exiting.", distance);
            return null;
        }

        return relatedOrderOpt.get();
    }

    private com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainRelatedOrder getRelatedOrder(com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrder serviceOrder, int distance) {

        Optional<com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainRelatedOrder> relatedOrderOpt = serviceOrder.getRelatedObjects().stream()
                .filter(pre -> "ParentOrder".equals(pre.getOrderType()) && pre.getDistance() == distance)
                .findFirst();

        if (relatedOrderOpt.isEmpty()) {
            log.error("Couldn't find related ParentOrder object with distance of {}. Exiting.", distance);
            return null;
        }

        return relatedOrderOpt.get();
    }

    @SneakyThrows
    public void createWholesaleProvisioningDatabaseEntries(String rtOrderId, String rtCustomer,
                                                           String wsOrderId, String wsCustomer,
                                                           String wsProvisioningId, String tenant) {
        log.debug("createWholesaleProvisioningDatabaseEntries: wsOrderId={}", wsOrderId);

        Long opiNumber = null;
        OrderingV1DomainOrder retailOrder = orderingService.getOrder(infonovaLinkService.getCorrectTenant(TenantType.RETAIL), rtOrderId);
        OffsetDateTime draftOrderPlacedDT = null;

        if (!CollectionUtils.isEmpty(retailOrder.getParameters()) &&
                retailOrder.getParameters().containsKey("opi_num")) {
            opiNumber = Long.parseLong(retailOrder.getParameters().get("opi_num"));
            String dt = retailOrder.getParameters().get(WF_DRAFT_ORDER_PLACED_DT.toLowerCase());
            draftOrderPlacedDT = OffsetDateTime.parse(dt);
        }

        if (draftOrderPlacedDT == null) {
            draftOrderPlacedDT = OffsetDateTime.now();
        }

        OrderingV1DomainOrderList orderHierarchy = orderingService.getOrderHierarchy(infonovaLinkService.getCorrectTenant(TenantType.WHOLESALE), wsOrderId);

        ProductorderingV1DomainProductOrder productOrder = productOrdersService.getProductOrder(infonovaLinkService.getCorrectTenant(TenantType.RETAIL), rtOrderId);

        for (OrderingV1DomainOrder order : orderHierarchy.getList()) {
            if ("NEW_SERVICE".equals(order.getType())) {

                if (TRAVEL_AND_EXPENSES_SERVICE.equals(order.getReferenceName())) {
                    log.debug("Service TravelAndExpenses skipped.");
                    continue;
                }

                saveOrderItems(rtOrderId, rtCustomer, wsOrderId, wsCustomer, wsProvisioningId, tenant, opiNumber,
                        draftOrderPlacedDT, orderHierarchy, productOrder, order);
            }
        }
    }

    private void saveOrderItems(String rtOrderId, String rtCustomer, String wsOrderId, String wsCustomer,
                                String wsProvisioningId, String tenant, Long opiNumber,
                                OffsetDateTime draftOrderPlacedDT, OrderingV1DomainOrderList orderHierarchy,
                                ProductorderingV1DomainProductOrder productOrder, OrderingV1DomainOrder order) throws JsonProcessingException {

        OrderingV1DomainOrder offerOrder = findInList(orderHierarchy, order.getRelationships().get(0).getId());

        ProvisioningOrderItemEntity entity = ProvisioningOrderItemEntity.builder()
                .retailOrderId(Long.parseLong(rtOrderId))
                .retailBillingAccountId(Long.parseLong(rtCustomer))
                .opiNumber(opiNumber)
                .wholesaleOrderId(Long.parseLong(wsOrderId))
                .wholesaleBillingAccountId(Long.parseLong(wsCustomer))
                .wholesaleServiceOrderId(order.getId())
                .wholesaleServiceOffer(offerOrder.getReferenceName())
                .wholesaleService(order.getReferenceName())
                .dafStatus(DafStatus.READY.name())
                .serviceProvisioningId(Long.parseLong(wsProvisioningId))
                .orderQuantity(getQuantityFromProductOrder(productOrder, order.getReferenceId()))
                .earliestActivationDate(draftOrderPlacedDT.toInstant())
                .tenant(tenant)
                .build();

        if (log.isDebugEnabled()) {
            log.debug("Entity: {}", objectMapper.writeValueAsString(entity));
        }

        provisioningOrderItemsRepository.save(entity);
    }

    protected OrderingV1DomainOrder findInList(OrderingV1DomainOrderList list, String id) {
        return list.getList().stream()
                .filter(pre -> pre.getId().equals(id))
                .findFirst().get();
    }

    protected Long getQuantityFromProductOrder(ProductorderingV1DomainProductOrder productOrder, String serviceId) throws JsonProcessingException {

        List<ProductorderingV1DomainServiceCharacteristic> collect = productOrder.getOrderItems().stream()
                .filter(pre -> pre.getProduct().getProducts()!=null)
                .flatMap(pre -> pre.getProduct().getProducts().stream())
                .flatMap(pre3 -> pre3.getServices().stream())
                .filter(pre4 -> serviceId.equals(pre4.getId().toString()))
                .flatMap(pre5 -> pre5.getServiceCharacteristics().stream())
                .filter(pre -> pre.getName()!=null && pre.getName().contains(":QUANTITY"))
                .collect(Collectors.toList());

        if (collect.size()==0) {
            return null;
        } else if (collect.size()==1) {
            return Long.valueOf(collect.get(0).getValue());
        } else {
            log.warn("We found more than one characteristic (returning 1st one). This might be some configuration error: {}", objectMapper.writeValueAsString(collect));
            return Long.valueOf(collect.get(0).getValue());
        }
    }


    protected String getCharacteristicValueFromServiceOrderItemOfEvent(ServiceorderingV1DomainServiceOrderItem orderItem, String characteristicName) {

        Optional<com.bearingpoint.beyond.test-bpintegration.infonova.api.server.events.model.ServiceorderingV1DomainServiceCharacteristic> first = orderItem
                .getService().getServiceCharacteristics().stream()
                .filter(pre -> pre.getName() != null && pre.getName().contains(":" + characteristicName))
                .findFirst();

        return first.map(serviceorderingV1DomainServiceCharacteristic -> serviceorderingV1DomainServiceCharacteristic.getValue()).orElse(null);
    }


    protected Long getQuantityFromServiceOrder(com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrder serviceOrder, String wholesaleServiceOrderId) {

        Optional<ServiceorderingV1DomainServiceCharacteristic> first = serviceOrder.getOrderItems().stream()
                .filter(pre -> wholesaleServiceOrderId.equals(pre.getId()))
                .flatMap(pre -> pre.getService().getServiceCharacteristics().stream())
                .filter(pre -> pre.getName() != null && pre.getName().contains(":QUANTITY"))
                .findFirst();

        return first.map(serviceorderingV1DomainServiceCharacteristic -> Long.valueOf(serviceorderingV1DomainServiceCharacteristic.getValue())).orElse(null);

    }


    /**
     * Get Wholesale Order Items.
     * @param onlyFree
     * @return
     */
    public List<ProvisioningOrderItemDto> getWholesaleOrderItems(Long wholesaleOrderId, boolean onlyFree) {
        return convertItems(provisioningOrderItemsRepository.findByWholesaleOrderIdAndDafWorkflowId(wholesaleOrderId, null), true);
    }

    public boolean checkIsAllOrderItemsComplete(Long wholesaleOrderId) {
        final List<ProvisioningOrderItemEntity> notCompletedOrderItems = provisioningOrderItemsRepository.findByWholesaleOrderId(
                        wholesaleOrderId)
                .stream()
                .filter(oi -> !DafStatus.COMPLETE.name().equals(oi.getDafStatus()))
                .collect(Collectors.toList());
        return notCompletedOrderItems.isEmpty();
    }

    public List<ProvisioningOrderItemDto> getWholesaleOrderItems(Long wholesaleOrderId) {
        return convertItems(provisioningOrderItemsRepository.findByWholesaleOrderId(wholesaleOrderId), false);
    }

    public List<ProvisioningOrderItemDto> convertItems(List<ProvisioningOrderItemEntity> inList, boolean shortList) {
        return inList.stream().map((orderItemDto) -> {
            ProvisioningOrderItemDto.ProvisioningOrderItemDtoBuilder builder = ProvisioningOrderItemDto.builder()
                    .id(orderItemDto.getId())
                    .opiNumber(orderItemDto.getOpiNumber())
                    .wholesaleOrderId(orderItemDto.getWholesaleOrderId())
                    .wholesaleService(orderItemDto.getWholesaleService())
                    .wholesaleServiceOrderId(orderItemDto.getWholesaleServiceOrderId())
                    .wholesaleServiceOffer(orderItemDto.getWholesaleServiceOffer())
                    .orderQuantity(orderItemDto.getOrderQuantity())
                    .requestedQuantity(orderItemDto.getRequestedQuantity())
                    .requestedActivationDate(getDateAsString(orderItemDto.getRequestedActivationDate()))
                    .earliestActivationDate(getDateAsString(orderItemDto.getEarliestActivationDate()));

            if (!shortList) {
                builder.retailOrderId(orderItemDto.getRetailOrderId())
                        .retailBillingAccountId(orderItemDto.getRetailBillingAccountId())
                        .wholesaleBillingAccountId(orderItemDto.getWholesaleBillingAccountId())
                        .dafWorkflowId(orderItemDto.getDafWorkflowId())
                        .dafStatus(orderItemDto.getDafStatus())
                        .serviceProvisioningId(orderItemDto.getServiceProvisioningId())
                        .tenant(orderItemDto.getTenant())
                        .taskId(orderItemDto.getTaskId())
                        .deleteAfter(orderItemDto.getDeleteAfter());
            }

            return builder.build();
        }).collect(Collectors.toList());
    }

    @Nullable
    public static String getDateAsString(Instant date) {
        return (date == null) ? null : LocalDate.ofInstant(date, ZoneId.of("UTC")).format(dateSimpleDateFormat);
    }

    public static Instant getDateFromStringAsInstant(String date) {
        if (StringUtils.isBlank(date)) {
            return null;
        }

        LocalDate dt = offsetFormatter.parse(date, LocalDate::from);
        OffsetDateTime offsetTime = OffsetDateTime.of(dt, LocalTime.of(0, 0, 0), ZoneOffset.UTC);

        return offsetTime.toInstant();
    }


    public static OffsetDateTime getDateFromStringAsOffsetDateTime(String date) {
        if (StringUtils.isBlank(date)) {
            return null;
        }

        LocalDate dt = offsetFormatter.parse(date, LocalDate::from);

        return OffsetDateTime.of(dt, LocalTime.of(0, 0, 0), ZoneOffset.UTC);
    }


    /**
     * Get Wholesale Order Items.
      * @param dafWorkflowId
     * @return
     */
    public List<ProvisioningOrderItemDto> getWholesaleOrderItems(String dafWorkflowId) {
        return convertItems(provisioningOrderItemsRepository.findByDafWorkflowId(dafWorkflowId), false);
    }

    @SneakyThrows
    public void setServiceOrderInProgress(String wsOrderId, String serviceExternalId, String serviceTenant) {

        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrder serviceOrderSource = serviceOrderingService.getServiceOrderByExternalId(serviceExternalId, serviceTenant);

        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrder order = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrder()
                .externalId(serviceOrderSource.getExternalId())
                .priority(serviceOrderSource.getPriority())
                .state("InProgress")
                .changeable(true)
                .cancelable(false)
                .completionDate(serviceOrderSource.getRequestedCompletionDate());

        for (com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrderItem orderItem : serviceOrderSource.getOrderItems()) {
            com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrderItem item = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrderItem()
                    .id(orderItem.getId())
                    .state("InProgress")
                    .service(new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainService()
                            .changeable(true)
                            .cancelable(true)
                            .id(orderItem.getService().getId()));

            order.addOrderItemsItem(item);
        }

        serviceOrderingService.sendOrderStateChangeNotification(serviceTenant, order, null);

        log.debug("Service order {} (for wholesaleOrder {}) set to state InProgress and flag changeable=true and cancelable=false and all services have flag changeable=true and cancelable=true.", serviceExternalId, wsOrderId);
    }


    @SneakyThrows
    public void startInflightOrderWorkflow(ServiceorderingV1DomainServiceOrder serviceOrder, String tenant) {

        if (log.isDebugEnabled()) {
            log.debug("Received Inflight service Order event: {}", objectMapper.writeValueAsString(serviceOrder));
        }

        InflightOrderChangesDto inflightOrderChangesDto = determineInflightOrderChanges(serviceOrder);

        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrder serviceOrderByExternalId = serviceOrderingService.getServiceOrderByExternalId(serviceOrder.getExternalId(), tenant);

        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainRelatedOrder retailRelatedOrder = getRelatedOrder(serviceOrderByExternalId, 2);
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainRelatedOrder wsRelatedOrder = getRelatedOrder(serviceOrderByExternalId, 1);

        if (retailRelatedOrder==null || wsRelatedOrder==null) {
            log.warn("Related orders objects are not found. retail={}, ws={}",
                    retailRelatedOrder==null ? "null" : retailRelatedOrder,
                    wsRelatedOrder==null ? "null" : wsRelatedOrder
            );
            return;
        }

        workflowUtil.startInflightWholesaleProvisioningWorkflow(retailRelatedOrder.getId(),
                retailRelatedOrder.getOwner(),
                wsRelatedOrder.getId(),
                wsRelatedOrder.getOwner(),
                serviceOrder.getExternalId(),
                tenant,
                inflightOrderChangesDto
        );

    }

    public InflightOrderChangesDto determineInflightOrderChanges(ServiceorderingV1DomainServiceOrder serviceOrder) throws JsonProcessingException {

        InflightOrderChangesDto inflightOrderChangesDto = new InflightOrderChangesDto();

        for (ServiceorderingV1DomainServiceOrderItem orderItem : serviceOrder.getOrderItems()) {

            if ("Cancelled".equals(orderItem.getState())) {
                inflightOrderChangesDto.addCancelledItem(orderItem.getId());
            } else if (orderItem.getOrderItemAction()==null) {
                // for now we will store data like this <item_id>=<quantity>, if we need to include
                // other values in the future, this might need to be extended into json
                String newQuantity = getCharacteristicValueFromServiceOrderItemOfEvent(orderItem, "QUANTITY");
                if (newQuantity!=null) {
                    inflightOrderChangesDto.addModifiedItem(orderItem.getId() + "=" + newQuantity);
                }
            } else if (ServiceorderingV1DomainServiceOrderItem.OrderItemActionEnum.CREATE == orderItem.getOrderItemAction()) {
                inflightOrderChangesDto.addAddedItem(orderItem.getId());
            } else {
                log.error("We received weird orderItem \"state\" in event: {}", objectMapper.writeValueAsString(orderItem));
            }

        }

        return inflightOrderChangesDto;
    }



    public List<String> getOrderItems(ServiceorderingV1DomainServiceOrderItem.OrderItemActionEnum itemAction,
                                      ServiceorderingV1DomainServiceOrder serviceOrder) {
        return serviceOrder.getOrderItems().stream()
                .filter(pre -> itemAction.equals(pre.getOrderItemAction()))
                .map(ServiceorderingV1DomainServiceOrderItem::getId)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public void createInflightWholesaleProvisioningDatabaseEntries(String rtOrderId, String rtCustomer,
                                                                   String wsOrderId, String wsCustomer,
                                                                   String wsProvisioningId, String tenant,
                                                                   String addedIds) {
        log.debug("createInflightWholesaleProvisioningDatabaseEntries: wsOrderId={}", wsOrderId);

        Long opiNumber = null;
        OrderingV1DomainOrder retailOrder = orderingService.getOrder(infonovaLinkService.getCorrectTenant(TenantType.RETAIL), rtOrderId);
        OffsetDateTime draftOrderPlacedDT = null;

        if (!CollectionUtils.isEmpty(retailOrder.getParameters()) &&
                retailOrder.getParameters().containsKey("opi_num")) {
            opiNumber = Long.parseLong(retailOrder.getParameters().get("opi_num"));
            String dt = retailOrder.getParameters().get(WF_DRAFT_ORDER_PLACED_DT.toLowerCase());
            draftOrderPlacedDT = OffsetDateTime.parse(dt);
        }

        if (draftOrderPlacedDT == null) {
            draftOrderPlacedDT = OffsetDateTime.now();
        }

        Set<String> newIds = Set.of(addedIds.split(", "));

        log.debug("New Services: {} ({})", addedIds, newIds.size());

        OrderingV1DomainOrderList orderHierarchy = orderingService.getOrderHierarchy(infonovaLinkService.getCorrectTenant(TenantType.WHOLESALE), wsOrderId);

        ProductorderingV1DomainProductOrder productOrder = productOrdersService.getProductOrder(infonovaLinkService.getCorrectTenant(TenantType.RETAIL), rtOrderId);

        for (OrderingV1DomainOrder order : orderHierarchy.getList()) {
            if ("NEW_SERVICE".equals(order.getType())) {

                if (!newIds.contains(order.getId())) {
                    log.debug("Order item with id {} ignored.", order.getId());
                    continue;
                }

                if (TRAVEL_AND_EXPENSES_SERVICE.equals(order.getReferenceName())) {
                    log.debug("Service TravelAndExpenses skipped.");
                    continue;
                }

                log.debug("Order item with id {} processed.", order.getId());

                saveOrderItems(rtOrderId, rtCustomer, wsOrderId, wsCustomer, wsProvisioningId, tenant, opiNumber,
                        draftOrderPlacedDT, orderHierarchy, productOrder, order);
            }
        }
    }


    public void inflightModifyWholesaleProvisioningItems(String wsOrderId, Set<String> modifiedEntries) throws JsonProcessingException {

        log.debug("inflightModifyWholesaleProvisioningItems: wsOrderId={}, modifedEntries={}", wsOrderId, modifiedEntries);

        Map<String, Long> modifiedIdsWithQuantity =
                modifiedEntries.stream()
                        .map(s -> s.split("="))
                        .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? Long.valueOf(a[1]) : null));

        List<ProvisioningOrderItemEntity> orderItems = findProvisioningItemsSpecified(wsOrderId, modifiedIdsWithQuantity.keySet());

        log.debug("REMOVE: Order Item Entity: {}", objectMapper.writeValueAsString(orderItems));

        for (ProvisioningOrderItemEntity entity : orderItems) {

            Long newQuantity = modifiedIdsWithQuantity.get(entity.getWholesaleServiceOrderId());

            if (newQuantity==null) {
                log.warn("New quantity found was null, which shouldn't happen. ServiceOrderItemId={}, ServiceOrder: {}", entity.getWholesaleServiceOrderId(), objectMapper.writeValueAsString(modifiedIdsWithQuantity));
                continue;
            }

            log.debug("Current Order Quantity in Db: {}, New Order Quantity in order: {}", entity.getOrderQuantity(), newQuantity);

            if (!Objects.equals(entity.getOrderQuantity(), newQuantity)) {
                entity.setOrderQuantity(newQuantity);
                provisioningOrderItemsRepository.save(entity);
                log.debug("New quantity {} was set for orderItem {}", newQuantity, entity.getWholesaleServiceOrderId());
            }
        }

    }


    public void removeProvisioningItemsFromDatabase(String wsOrderId, Set<String> removedIds) {

        log.debug("removeProvisioningItemsFromDatabase: wsOrderId={}, modifedEntries={}", wsOrderId, removedIds);

        List<ProvisioningOrderItemEntity> provisioningItemsSpecified = findProvisioningItemsSpecified(wsOrderId, removedIds);

        if (provisioningItemsSpecified.size()>0) {

            if (provisioningItemsSpecified.size()==removedIds.size()) {
                log.debug("Found {} items. Deleting them.", removedIds.size());
            } else {
                log.error("Found {} items, expected {} ", provisioningItemsSpecified.size(), removedIds.size());
            }

            provisioningOrderItemsRepository.deleteAll(provisioningItemsSpecified);
        }
    }

    protected List<ProvisioningOrderItemEntity> findProvisioningItemsSpecified(String wsOrderId, Set<String> removedIds) {
        List<ProvisioningOrderItemEntity> byWholesaleOrderId = provisioningOrderItemsRepository.findByWholesaleOrderId(Long.parseLong(wsOrderId));
        return byWholesaleOrderId.stream()
                .filter((entity) -> removedIds.contains(entity.getWholesaleServiceOrderId()))
                .collect(Collectors.toList());
    }

    public void completeInflightOrder(String provisioningId, String tenant) throws JsonProcessingException {
        serviceOrderingService.sendOrderInflightStateChangeNotification(tenant,
                new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrder()
                        .externalId(provisioningId), null,
                ServiceorderingV1DomainServiceOrderingNotification.InFlightOrderChangeStateEnum.CHANGESACCEPTED);
    }

    public void raiseOrderServiceProvisioningTask(Long wholesaleOrderId) {
        List<ProvisioningOrderItemEntity> provisioningOrderItems = provisioningOrderItemsRepository.findByWholesaleOrderIdAndTaskId(wholesaleOrderId, null);
        if(!provisioningOrderItems.isEmpty()) {
            OrderingV1DomainOrderList orderHierarchy = orderingService.getOrderHierarchy(infonovaLinkService.getCorrectTenant(TenantType.RETAIL), provisioningOrderItems.get(0).getRetailOrderId().toString());
            String referenceId = orderHierarchy.getList().stream()
                    .filter(order -> StringUtils.equals(order.getType(), "NEW_BUNDLE"))
                    .findFirst().map(OrderingV1DomainOrder::getReferenceId)
                    .orElseThrow(() -> new IllegalArgumentException("Bundle reference does not exist!"));

            ProductinventoryV1DomainProduct relatedBundle = productInventoryV1Api.getProductInventoryV1ProductsProduct(retailTenant, referenceId).block();

            TaskV2DomainTask task = new TaskV2DomainTask();
            task.setTaskDefinition(TELUS_WS_ORDER_SERVICE_PROVISIONING_TASK);
            task.setPriority(2);
            task.setBillingAccount(provisioningOrderItems.get(0).getWholesaleBillingAccountId().toString());
            task.setParameters(Map.ofEntries(
                    entry(PRODUCT_NAME, relatedBundle.getDisplayName()),
                    entry(RETAIL_ORDER_ID, provisioningOrderItems.get(0).getRetailOrderId().toString()),
                    entry(PROVISIONING_ORDER_ID, provisioningOrderItems.get(0).getServiceProvisioningId().toString()),
                    entry(RETAIL_BILLING_ACCOUNT_ID, provisioningOrderItems.get(0).getRetailBillingAccountId().toString()),
                    entry(WS_ORDER_ID, provisioningOrderItems.get(0).getWholesaleOrderId().toString()),
                    entry(WS_ORDER_LINK, infonovaLinkService.getOrderLink(TenantType.WHOLESALE_CSM, wholesaleOrderId.toString(), provisioningOrderItems.get(0).getWholesaleBillingAccountId().toString())),
                    entry(WS_SERVICE_ORDER_ID, provisioningOrderItems.get(0).getWholesaleServiceOrderId()),
                    entry(WS_BILLING_ACCOUNT_ID, provisioningOrderItems.get(0).getWholesaleBillingAccountId().toString()),
                    entry(WHOLESALE_SERVICE_OFFER, provisioningOrderItems.get(0).getWholesaleServiceOffer())
            ));
            TaskV2DomainTask responseTask = tasksService.createTask(provisioningOrderItems.get(0).getTenant(), task);
            log.debug("Task {} raised for wholesale order with id {}", responseTask, wholesaleOrderId);
            for (ProvisioningOrderItemEntity entity : provisioningOrderItems) {
                entity.setTaskId(Long.parseLong(responseTask.getId()));
                provisioningOrderItemsRepository.save(entity);
            }
        } else {
            log.error("No tasks were raised for wholesale order {}. There are no services in the database related to this wholesale order.", wholesaleOrderId);
        }
    }

    public List<ProvisioningOrderItemDto> getWholesaleOrderItemsByRetailId(Long retailOrderId) {
        return convertItems(provisioningOrderItemsRepository.findByRetailOrderId(retailOrderId), false);
    }
}
