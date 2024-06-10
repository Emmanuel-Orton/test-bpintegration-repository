package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.api.ProductOrderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.*;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceinventory.model.ServiceinventoryV1DomainServiceList;
import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductOrdersServiceTest {

	private static final String RT_TENANT = "SB_TELUS_SIT";
	private static final String WS_TENANT = "SB_TELUS_SIT_WS";
	private static final String DRAFT_ORDER_ID = "2378942394";
	private static final String ORDER_ID = "36852368";
	private static final String DRAFT_ID = "555235423894";
	private static final String CART_ID = "38478293";
	private static final String PRODUCT_ID = "560d7b6f-cbbc-4d78-9ee5-03d89b8e2649";

	@Mock
	ProductOrderingV1Api productOrderingV1Api;

	@InjectMocks
	ProductOrdersService unit;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(unit, "objectMapper", new ObjectMapper());
		ReflectionTestUtils.setField(unit, "rtTenant", RT_TENANT);
	}

	@Test
	void getDraftOrder() {
		final ProductorderingV1DomainDraftOrderList orders = mock(ProductorderingV1DomainDraftOrderList.class);
		when(productOrderingV1Api.getProductOrderingV1DraftOrders(WS_TENANT, null,
				BigDecimal.ONE, BigDecimal.ONE, "draftOrder:2378942394", false, null, null, null)).thenReturn(
				Mono.just(orders));

		final ProductorderingV1DomainDraftOrder draftOrder1 = mock(ProductorderingV1DomainDraftOrder.class);
		final ProductorderingV1DomainDraftOrder draftOrder2 = mock(ProductorderingV1DomainDraftOrder.class);

		when(orders.getList()).thenReturn(List.of(draftOrder1, draftOrder2));

		final ProductorderingV1DomainDraftOrder draftOrder = unit.getDraftOrder(WS_TENANT, DRAFT_ORDER_ID);
		assertThat(draftOrder).isEqualTo(draftOrder1);
	}

	@Test
	void getDraftOrder_draftOrderNotExists() {
		final ProductorderingV1DomainDraftOrderList orders = mock(ProductorderingV1DomainDraftOrderList.class);
		when(productOrderingV1Api.getProductOrderingV1DraftOrders(WS_TENANT, null,
				BigDecimal.ONE, BigDecimal.ONE, "draftOrder:2378942394", false, null, null, null)).thenReturn(
				Mono.just(orders));

		when(orders.getList()).thenReturn(Collections.emptyList());
		Exception exception = assertThrows(IllegalArgumentException.class,
				() -> unit.getDraftOrder(WS_TENANT, DRAFT_ORDER_ID));
		assertThat(exception).hasMessageContaining("Draft order 2378942394 not found");
	}

	@Test
	void getProductOrder() {
		final ProductorderingV1DomainProductOrder po = mock(ProductorderingV1DomainProductOrder.class);
		when(productOrderingV1Api.getProductOrderingV1ProductOrdersId(WS_TENANT, ORDER_ID)).thenReturn(Mono.just(po));
		assertThat(unit.getProductOrder(WS_TENANT, ORDER_ID)).isEqualTo(po);
	}

	@Test
	void createCartFromDraft() {
		ProductorderingV1DomainShoppingCart cartBody = new ProductorderingV1DomainShoppingCart()
				.draftId(DRAFT_ID);
		final ProductorderingV1DomainShoppingCart shcrt = mock(ProductorderingV1DomainShoppingCart.class);
		when(productOrderingV1Api.postProductOrderingV1Carts(eq(WS_TENANT), eq(cartBody), anyString())).thenReturn(
				Mono.just(shcrt));

		final ProductorderingV1DomainShoppingCart cartFromDraft = unit.createCartFromDraft(WS_TENANT, DRAFT_ID);

		assertThat(cartFromDraft).isEqualTo(shcrt);

	}

	@Test
	void postShoppingCart() {
		final ProductorderingV1DomainShoppingCart cartBody = mock(ProductorderingV1DomainShoppingCart.class);
		final ProductorderingV1DomainShoppingCart updatedCartBody = mock(ProductorderingV1DomainShoppingCart.class);

		when(productOrderingV1Api.postProductOrderingV1Carts(anyString(), eq(cartBody), anyString())).thenReturn(
				Mono.just(updatedCartBody));

		final ProductorderingV1DomainShoppingCart domainShoppingCart = unit.postShoppingCart(WS_TENANT,
				cartBody);

		assertThat(domainShoppingCart).isEqualTo(updatedCartBody);
		verify(productOrderingV1Api, times(1)).postProductOrderingV1Carts(eq(WS_TENANT), eq(cartBody), anyString());
	}

	@Test
	void patchShoppingCart() {
		final ProductorderingV1DomainShoppingCart cartBody = mock(ProductorderingV1DomainShoppingCart.class);
		when(cartBody.getId()).thenReturn(CART_ID);
		final ProductorderingV1DomainShoppingCart updatedCartBody = mock(ProductorderingV1DomainShoppingCart.class);

		when(productOrderingV1Api.patchProductOrderingV1CartsCart(anyString(), eq(CART_ID), eq(cartBody))).thenReturn(
				Mono.just(updatedCartBody));

		final ProductorderingV1DomainShoppingCart domainShoppingCart = unit.patchShoppingCart(WS_TENANT,
				cartBody);

		assertThat(domainShoppingCart).isEqualTo(updatedCartBody);
		verify(productOrderingV1Api, times(1)).patchProductOrderingV1CartsCart(WS_TENANT, CART_ID, cartBody);
	}

	@Test
	void submitShoppingCart() {
		final ProductorderingV1DomainShoppingCart shoppingCart = mock(ProductorderingV1DomainShoppingCart.class);
		when(shoppingCart.getId()).thenReturn(CART_ID);
		final ProductorderingV1DomainProductOrder po = mock(ProductorderingV1DomainProductOrder.class);
		when(productOrderingV1Api.postProductOrderingV1CartsCartSubmit(eq(WS_TENANT),
				eq(CART_ID), any(ProductorderingV1DomainProductOrderDetails.class), isNull())
		).thenReturn(Mono.just(po));

		assertThat(unit.submitShoppingCart(WS_TENANT, shoppingCart)).isEqualTo(po);
	}

	@Test
	void getRetailOrderForTheWholesale() {
		final ProductorderingV1DomainProductOrder wsOrder = mock(ProductorderingV1DomainProductOrder.class);
		final ProductorderingV1DomainRelatedOrder relatedOrderShouldPass = mock(ProductorderingV1DomainRelatedOrder.class);
		when(relatedOrderShouldPass.getType()).thenReturn("Order");
		when(relatedOrderShouldPass.getOrderType()).thenReturn("ParentOrder");
		when(relatedOrderShouldPass.getId()).thenReturn("relatedObjectId");

		when(wsOrder.getRelatedObjects()).thenReturn(List.of(relatedOrderShouldPass, mock(ProductorderingV1DomainRelatedOrder.class)));

		final ProductorderingV1DomainProductOrder po = mock(ProductorderingV1DomainProductOrder.class);
		when(productOrderingV1Api.getProductOrderingV1ProductOrdersId(RT_TENANT, "relatedObjectId")).thenReturn(Mono.just(po));

		assertThat(unit.getRetailOrderForTheWholesale(wsOrder)).isEqualTo(po);
	}

	@Test
	void getOrderSummary() {
		final ProductorderingV1SummaryDomainCustomerOrderSummary os = mock(
				ProductorderingV1SummaryDomainCustomerOrderSummary.class);
		when(productOrderingV1Api.getProductOrderingV1CartsCartCustomerOrderSummary(WS_TENANT, CART_ID)).thenReturn(Mono.just(os));
		assertThat(unit.getOrderSummary(CART_ID, WS_TENANT)).isEqualTo(os);
	}

	@Test
	@Disabled
	void submitShoppingCartWithStartDate() {
		// TODO
	}

	@Test
	void getProductOrders() throws Exception {
		when(productOrderingV1Api.getProductOrderingV1ProductOrders(WS_TENANT, null, null, "productReference:560d7b6f-cbbc-4d78-9ee5-03d89b8e2649 type:\"NEW\"", null, null))
				.thenReturn(Mono.just(TestUtils.readObjectFromFile("/productOrdersByReference.json", ProductorderingV1DomainProductOrderList.class)));

		ProductorderingV1DomainProductOrderList productOrders = unit.getProductOrders(WS_TENANT, "productReference:560d7b6f-cbbc-4d78-9ee5-03d89b8e2649 type:\"NEW\"");

		Assertions.assertNotNull(productOrders);
		Assertions.assertEquals(1, productOrders.getList().size());
	}

	@Test
	void getProductOrderWithProductReferenceAndType() throws Exception {
		when(productOrderingV1Api.getProductOrderingV1ProductOrders(WS_TENANT, null, null, "productReference:560d7b6f-cbbc-4d78-9ee5-03d89b8e2649 type:\"NEW\"", null, null))
				.thenReturn(Mono.just(TestUtils.readObjectFromFile("/productOrdersByReference.json", ProductorderingV1DomainProductOrderList.class)));

		ProductorderingV1DomainProductOrder order = unit.getProductOrderWithProductReferenceAndType(WS_TENANT, PRODUCT_ID, "NEW");

		Assertions.assertNotNull(order);
		Assertions.assertEquals("7289311", order.getId());
	}

}
