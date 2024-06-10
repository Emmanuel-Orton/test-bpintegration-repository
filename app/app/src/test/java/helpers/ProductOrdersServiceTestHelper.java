package helpers;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOfferingReference;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrderItem;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.constant.OrderParametersNames.OPI_NUM_ORDER_PARAMETER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProductOrdersServiceTestHelper {

	ProductOrdersService mockProductOrdersService;

	ProductorderingV1DomainProductOrder wsOrder;

	ProductorderingV1DomainProductOrder rtOrder;

	public ProductOrdersServiceTestHelper(ProductOrdersService mockProductOrdersService) {
		this.mockProductOrdersService = mockProductOrdersService;
		wsOrder = mock(ProductorderingV1DomainProductOrder.class);
		rtOrder = mock(ProductorderingV1DomainProductOrder.class);
	}

	public void mockWsOrderBillingAccount(String tenant, String wsBillingAccount) {
		when(mockProductOrdersService.getProductOrder(tenant, "1")).thenReturn(wsOrder);
		when(wsOrder.getBillingAccount()).thenReturn(wsBillingAccount);
	}

	public void mockRtOrderBillingAccount(String rtBillingAccount) {
		when(mockProductOrdersService.getRetailOrderForTheWholesale(wsOrder)).thenReturn(rtOrder);
		//when(rtOrder.getId()).thenReturn("2");
		when(rtOrder.getBillingAccount()).thenReturn(rtBillingAccount);
	}

	public void expectRtOrder(String rtBillingAccount, String orderId, String operatorUsername,
			String productOfferingName) {
		ProductorderingV1DomainProductOrder order = new ProductorderingV1DomainProductOrder()
				.billingAccount(rtBillingAccount)
				.orderItems(Collections.singletonList(mockProductItemAndProductOfferingName("")))
				.id(orderId)
				.operator(operatorUsername);

		when(mockProductOrdersService.getRetailOrderForTheWholesale(wsOrder)).thenReturn(order);
	}



	public void mockProductOfferingName(String productOfferingName) {
		final ProductorderingV1DomainProductOrderItem productOrderItem = mock(
				ProductorderingV1DomainProductOrderItem.class);
		when(rtOrder.getOrderItems()).thenReturn(Collections.singletonList(productOrderItem));

		final ProductorderingV1DomainProduct product = mock(ProductorderingV1DomainProduct.class);
		when(productOrderItem.getProduct()).thenReturn(product);

		final ProductorderingV1DomainProductOfferingReference productOffering = mock(
				ProductorderingV1DomainProductOfferingReference.class);
		when(product.getProductOffering()).thenReturn(productOffering);

		when(productOffering.getName()).thenReturn(productOfferingName);
	}

	public void mockOpiNumOrderParameter(String productOfferingName) {
		@SuppressWarnings("unchecked")
		final Map<String, String> rtOrderParameters = (HashMap<String, String>) mock(HashMap.class);
		when(rtOrder.getParameters()).thenReturn(rtOrderParameters);

		when(rtOrderParameters.get(OPI_NUM_ORDER_PARAMETER)).thenReturn(productOfferingName);
	}

	private ProductorderingV1DomainProductOrderItem mockProductItemAndProductOfferingName(String productOfferingName) {
		final ProductorderingV1DomainProductOrderItem productOrderItem = mock(
				ProductorderingV1DomainProductOrderItem.class);

		final ProductorderingV1DomainProduct product = mock(ProductorderingV1DomainProduct.class);
		when(productOrderItem.getProduct()).thenReturn(product);

		final ProductorderingV1DomainProductOfferingReference productOffering = mock(
				ProductorderingV1DomainProductOfferingReference.class);
		when(product.getProductOffering()).thenReturn(productOffering);

		when(productOffering.getName()).thenReturn(productOfferingName);
		return productOrderItem;
	}
}
