package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1SummaryDomainCustomerOrderSummary;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1SummaryDomainProductOrderSummary;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1SummaryDomainProductSummary;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1SummaryDomainServiceCharacteristicSummary;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1SummaryDomainServiceSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateDataFilteringService {

	public void filterOrderSummaryServiceCharacteristics(
			ProductorderingV1SummaryDomainCustomerOrderSummary orderSummary,
			String draftOrderId, Set<String> names) {
		final ProductorderingV1SummaryDomainProductOrderSummary productOrderSummary = orderSummary.getProductOrderSummary();
		if (productOrderSummary == null) {
			throw new IllegalArgumentException(
					String.format("order summary for draftOrder=%s does not have orderSummary.productOrderSummary",
							draftOrderId));
		}

		final List<ProductorderingV1SummaryDomainProductSummary> outerProducts = productOrderSummary.getProducts();

		if (outerProducts == null) {
			throw new IllegalArgumentException(
					String.format(
							"order summary for draftOrder=%s does not have orderSummary.productOrderSummary.products",
							draftOrderId));
		}

		outerProducts.stream()
				.filter(ps -> ps.getServices() != null)
				.flatMap(ps -> ps.getServices().stream())
				.map(ProductorderingV1SummaryDomainServiceSummary::getServiceCharacteristics)
				.collect(Collectors.toList())
				.forEach(x -> removeByNames(x, names));

		outerProducts.stream()
				.filter(ps -> ps.getProducts() != null)
				.flatMap(ps -> ps.getProducts().stream())
				.filter(p -> p.getServices() != null)
				.flatMap(p -> p.getServices().stream())
				.map(ProductorderingV1SummaryDomainServiceSummary::getServiceCharacteristics)
				.collect(Collectors.toList())
				.forEach(x -> removeByNames(x, names));

	}

	private void removeByNames(List<ProductorderingV1SummaryDomainServiceCharacteristicSummary> serviceCharacteristics,
			Set<String> names) {
		serviceCharacteristics.removeIf(
				el -> names.stream().anyMatch(s -> el.getName() != null && el.getName().endsWith(":" + s)));
	}
}
