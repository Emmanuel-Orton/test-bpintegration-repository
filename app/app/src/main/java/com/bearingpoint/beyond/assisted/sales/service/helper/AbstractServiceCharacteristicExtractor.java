package com.bearingpoint.beyond.test-bpintegration.service.helper;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainServiceCharacteristic;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.bearingpoint.beyond.test-bpintegration.constant.ServiceCharacteristicsKeys.OPI_NUMBER_CH_KEY;
import static com.bearingpoint.beyond.test-bpintegration.constant.ServiceCharacteristicsKeys.RT_CUSTOMER_CH_KEY;
import static com.bearingpoint.beyond.test-bpintegration.constant.ServiceCharacteristicsKeys.SALES_AGENT_EMAIL_CH_KEY;

@Slf4j
public abstract class AbstractServiceCharacteristicExtractor {
	protected List<ProductorderingV1DomainServiceCharacteristic> serviceCharacteristics;

	public String getCustomerLegalName() {
		return getCharacteristicValue(RT_CUSTOMER_CH_KEY);
	}

	public String getOpiNumber() {
		return getCharacteristicValue(OPI_NUMBER_CH_KEY);
	}

	public String getSalesAgentEmail() {
		return getCharacteristicValue(SALES_AGENT_EMAIL_CH_KEY);
	}

	protected String getCharacteristicValue(String characteristicKey) {
		return this.serviceCharacteristics.stream()
				.filter(pc -> pc.getName() != null)
				.filter(pc -> pc.getName().endsWith(":" + characteristicKey))
				.findFirst()
				.map(ProductorderingV1DomainServiceCharacteristic::getValue).orElseGet(() -> {
					logNotFoundCharacteristic(characteristicKey);
					return "";
				});
	}

	abstract void logNotFoundCharacteristic(String characteristicName);
}
