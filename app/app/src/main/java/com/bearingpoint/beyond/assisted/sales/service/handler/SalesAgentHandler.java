package com.bearingpoint.beyond.test-bpintegration.service.handler;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainServiceCharacteristic;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.TasksService;
import com.bearingpoint.beyond.test-bpintegration.util.ShoppingCartUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class SalesAgentHandler {

    public static final String SALES_AGENT_NAME = "SALES_AGENT_NAME";
    public static final String SALES_AGENT_EMAIL = "SALES_AGENT_EMAIL";
    public static final String SALES_AGENT_USERNAME = "SALES_AGENT_USERNAME";

    private static final Map<String, Boolean> SALES_AGENT_CHARACTERISTICS_MAP = Map.of(
            SALES_AGENT_NAME, false,
            SALES_AGENT_EMAIL, true,
            SALES_AGENT_USERNAME, true
    );

    private final TasksService taskService;

    public void addAgentDetailsFromShoppingCart(ProductorderingV1DomainShoppingCart shoppingCart, TaskV2DomainTask task) {
        Map<String, String> agentDetailsMap = this.getAgentDetailsByServiceCharacteristics(
                ShoppingCartUtil.getOfferProduct(shoppingCart).getServices().get(0).getServiceCharacteristics());
        taskService.addAdditionalParametersToTask(agentDetailsMap, task);
    }

    public void addAgentDetailsFromProductOrder(ProductorderingV1DomainProductOrder productOrder, TaskV2DomainTask task) {
        Map<String, String> agentDetailsMap = this.getAgentDetailsByServiceCharacteristics(productOrder.getOrderItems()
                .get(0).getProduct().getServices().get(0).getServiceCharacteristics());
        taskService.addAdditionalParametersToTask(agentDetailsMap, task);
    }

    public Map<String, String> getAgentDetailsByServiceCharacteristics(List<ProductorderingV1DomainServiceCharacteristic> serviceCharacteristics) {
        final Map<String, String> salesAgent = new HashMap<>();

        SALES_AGENT_CHARACTERISTICS_MAP.forEach((charName, isRequired) -> {
            ProductorderingV1DomainServiceCharacteristic characteristic = serviceCharacteristics.stream()
                    .filter((serviceCharacteristic) -> serviceCharacteristic.getName() != null)
                    .filter((serviceCharacteristic) -> serviceCharacteristic.getName().endsWith(String.format(":%s", charName)))
                    .findFirst()
                    .orElse(null);

            if (isRequired) {
                if (characteristic == null) {
                    throw new IllegalArgumentException(String.format("Service Characteristic %s is not present.", charName));
                }

                if (characteristic.getValue() == null) {
                    throw new IllegalArgumentException(String.format("Parameter %s doesn't have a value", characteristic.getName()));
                }

                salesAgent.put(charName, characteristic.getValue());
            } else {
                if (characteristic != null && characteristic.getValue() != null) {
                    salesAgent.put(charName, characteristic.getValue());
                }
            }
        });

        return salesAgent;
    }
}
