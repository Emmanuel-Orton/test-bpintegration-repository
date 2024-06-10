package com.bearingpoint.beyond.test-bpintegration.util;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;

import java.util.List;
import java.util.Objects;

public class ShoppingCartUtil {
	public static ProductorderingV1DomainProduct getOfferProduct(ProductorderingV1DomainShoppingCart shoppingCart) {
		final List<ProductorderingV1DomainProduct> draftCartProducts = shoppingCart.getProducts();
		if (draftCartProducts == null) {
			throw new IllegalArgumentException(
					String.format("shopping cart[id=%s] does not contain products", shoppingCart.getDraftId()));
		}

		return draftCartProducts.stream()
				.map(ProductorderingV1DomainProduct::getProducts)
				.filter(Objects::nonNull)
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException(
						String.format("Can not find any offer products in the shopping cart[id=%s]", shoppingCart.getDraftId())))
				.get(0);
	}
}
