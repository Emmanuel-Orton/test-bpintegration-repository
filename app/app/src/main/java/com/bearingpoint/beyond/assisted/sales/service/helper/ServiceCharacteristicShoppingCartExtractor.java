package com.bearingpoint.beyond.test-bpintegration.service.helper;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainService;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainServiceCharacteristic;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;
import com.bearingpoint.beyond.test-bpintegration.service.helper.exc.ServiceCharacteristicExtractorException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ServiceCharacteristicShoppingCartExtractor extends AbstractServiceCharacteristicExtractor {

	private final ProductorderingV1DomainShoppingCart shoppingCart;

	public ServiceCharacteristicShoppingCartExtractor(ProductorderingV1DomainShoppingCart shoppingCart) {
		this.shoppingCart = shoppingCart;
		this.serviceCharacteristics = getServiceCharacteristics(shoppingCart);
	}

	private List<ProductorderingV1DomainServiceCharacteristic> getServiceCharacteristics(
			ProductorderingV1DomainShoppingCart shoppingCart) {
		List<ProductorderingV1DomainProduct> products = shoppingCart.getProducts();
		if (products == null || products.size() < 1) {
			throw new ServiceCharacteristicExtractorException(
					String.format("can not find a product in the shopping cart with id - %s", shoppingCart.getId()));
		}

		final ProductorderingV1DomainProduct product = products.get(0);
		if (product.getServices() != null && !product.getServices().isEmpty()) {
			return product.getServices().get(0).getServiceCharacteristics();
		}

		if (product.getProducts() == null || product.getProducts().size() < 1) {
			throw new ServiceCharacteristicExtractorException(
					String.format("can not find a product in a bundle product with id - %s, shopping cart id - %s",
							product.getId(), shoppingCart.getId()));
		}
		final ProductorderingV1DomainProduct innerProduct = product.getProducts().get(0);
		List<ProductorderingV1DomainService> services = innerProduct.getServices();

		if (services == null || services.size() < 1) {
			throw new ServiceCharacteristicExtractorException(
					String.format("product with id - %s do not have services, shopping cart id - %s",
							innerProduct.getId(), shoppingCart.getId()));
		}

		return services.get(0).getServiceCharacteristics();
	}

	@Override
	void logNotFoundCharacteristic(String characteristicName) {
		log.info("can not find a service characteristic for key {}, shoppingCartId {}", characteristicName,
				shoppingCart.getId());
	}
}
