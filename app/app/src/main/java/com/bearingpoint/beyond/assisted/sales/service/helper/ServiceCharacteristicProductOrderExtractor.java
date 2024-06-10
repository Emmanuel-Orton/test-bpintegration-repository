package com.bearingpoint.beyond.test-bpintegration.service.helper;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrderItem;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainServiceCharacteristic;
import com.bearingpoint.beyond.test-bpintegration.service.helper.exc.ServiceCharacteristicExtractorException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ServiceCharacteristicProductOrderExtractor extends AbstractServiceCharacteristicExtractor {

	private final ProductorderingV1DomainProductOrder wsOrder;

	public ServiceCharacteristicProductOrderExtractor(ProductorderingV1DomainProductOrder wsOrder) {
		this.wsOrder = wsOrder;
		this.serviceCharacteristics = getServiceCharacteristics(wsOrder.getOrderItems());
	}

	private List<ProductorderingV1DomainServiceCharacteristic> getServiceCharacteristics(
			List<ProductorderingV1DomainProductOrderItem> orderItems) {
		if (orderItems == null || orderItems.size() < 1) {
			throw new ServiceCharacteristicExtractorException(
					String.format("can not find order items in the product order with id - %s", wsOrder.getId()));
		}

		final ProductorderingV1DomainProductOrderItem product = orderItems.get(0);
		if (product.getProduct() == null || product.getProduct().getServices() == null || product.getProduct()
				.getServices()
				.isEmpty()) {
			throw new ServiceCharacteristicExtractorException(
					String.format("inner product with id - %s does not have services, product order with id - %s",
							product.getId(), wsOrder.getId()));
		}

		return product.getProduct().getServices().get(0).getServiceCharacteristics();
	}

	@Override
	void logNotFoundCharacteristic(String characteristicName) {
		log.info("can not find a service characteristic for key {}, orderId {}", characteristicName, wsOrder.getId());
	}
}
