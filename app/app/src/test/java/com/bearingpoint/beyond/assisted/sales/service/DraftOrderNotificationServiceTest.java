package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DraftOrderNotification;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.api.ProductOrderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftDetails;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.templaterendering.api.TemplateRenderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.users.api.UsersV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.users.model.UserV1DomainRelatedIndividual;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.users.model.UserV1DomainUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

import static com.bearingpoint.beyond.test-bpintegration.service.DraftOrderNotificationService.DRAFTORDER_EMAIL_BODY;
import static com.bearingpoint.beyond.test-bpintegration.service.DraftOrderNotificationService.DRAFTORDER_EMAIL_SUBJECT;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftOrderNotificationServiceTest {

	private static final String TENANT = "SB_TELUS_SIT";
	private static final String DRAFT_ORDER_ID = "1029842174";
	private static final String SHOPPING_CART_ID = "723453256848";
	private static final String SUBJECT = "subject";
	private static final String BODY = "body";
	private static final String RELATED_INDIVIDUAL_NAME = "related_individual_name";
	private static final String OPERATOR_NAME = "operator_name";
	private static final String CUSTOMER_BUSINESS_NAME = "customer_business_name";

	@Mock
	private SendGridService sendGridService;

	@Mock
	private TemplateRenderingV1Api templateRenderingV1Api;

	@Mock
	private UsersV1Api usersV1Api;

	@Mock
	private ProductOrderingV1Api productOrderingV1Api;

	@InjectMocks
	DraftOrderNotificationService unit;

	@Test
	void sendNotificationAndUpdateOrder() throws Exception {
		final DraftOrderNotification dratOrderNotification = mock(DraftOrderNotification.class);
		when(dratOrderNotification.getBillingAccountId()).thenReturn(SHOPPING_CART_ID);
		when(dratOrderNotification.getOperatorName()).thenReturn(OPERATOR_NAME);
		when(dratOrderNotification.getCustomerBusinessName()).thenReturn(CUSTOMER_BUSINESS_NAME);
		when(dratOrderNotification.getTo()).thenReturn(SHOPPING_CART_ID);
		when(dratOrderNotification.getCc()).thenReturn(SHOPPING_CART_ID);

		final ProductorderingV1DomainShoppingCart shoppingCart = mock(ProductorderingV1DomainShoppingCart.class);
		when(shoppingCart.getId()).thenReturn(SHOPPING_CART_ID);
		when(productOrderingV1Api.postProductOrderingV1Carts(any(), any(), any()))
				.thenReturn(Mono.just(shoppingCart));
		ArgumentCaptor<ProductorderingV1DomainShoppingCart> shoppingCartArgumentCaptor = ArgumentCaptor.forClass(
				ProductorderingV1DomainShoppingCart.class);

		when(productOrderingV1Api.patchProductOrderingV1CartsCart(eq(TENANT), eq(SHOPPING_CART_ID),
				shoppingCartArgumentCaptor.capture())).thenReturn(Mono.empty());

		when(templateRenderingV1Api.postTemplateRenderingV1TemplatesName(eq(TENANT), eq(DRAFTORDER_EMAIL_SUBJECT),
				anyString(), eq(Collections.emptyMap()))).thenReturn(Mono.just(
				SUBJECT));


		when(productOrderingV1Api.postProductOrderingV1CartsCartSave(
				anyString(),
				anyString(),
				any(ProductorderingV1DomainDraftDetails.class),
				ArgumentMatchers.isNull()
		)).thenReturn(Mono.empty());

		final UserV1DomainUser domainUser = mock(UserV1DomainUser.class);
		when(usersV1Api.getUsersV1UsersName(TENANT, OPERATOR_NAME)).thenReturn(Mono.just(domainUser));

		final UserV1DomainRelatedIndividual relatedIndividual = mock(UserV1DomainRelatedIndividual.class);
		when(relatedIndividual.getName()).thenReturn(RELATED_INDIVIDUAL_NAME);
		when(domainUser.getRelatedParty()).thenReturn(relatedIndividual);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
		when(templateRenderingV1Api.postTemplateRenderingV1TemplatesName(eq(TENANT), eq(DRAFTORDER_EMAIL_BODY),
				anyString(), paramsCaptor.capture())).thenReturn(Mono.just(
				BODY));

		unit.sendNotificationAndUpdateOrder(TENANT, DRAFT_ORDER_ID, dratOrderNotification);

		Assertions.assertThat(shoppingCartArgumentCaptor.getValue()).isNotNull();
		Assertions.assertThat(shoppingCartArgumentCaptor.getValue().getParameters())
				.containsEntry("availableForCustomerApproval", "true");

		Assertions.assertThat(paramsCaptor.getValue()).isNotNull();
		Assertions.assertThat(paramsCaptor.getValue()).containsEntry("SALES_USERNAME", RELATED_INDIVIDUAL_NAME);
		Assertions.assertThat(paramsCaptor.getValue()).containsEntry("DRAFT_ORDER_LINK", "");
		Assertions.assertThat(paramsCaptor.getValue()).containsEntry("DRAFT_ORDER_ID", DRAFT_ORDER_ID);
		Assertions.assertThat(paramsCaptor.getValue()).containsEntry("CUSTOMER_BUSINESS_NAME", CUSTOMER_BUSINESS_NAME);
		Assertions.assertThat(paramsCaptor.getValue()).containsEntry("CUSTOMER_NAME", CUSTOMER_BUSINESS_NAME);

		ProductorderingV1DomainDraftDetails productorderingV1DomainDraftDetails = new ProductorderingV1DomainDraftDetails();
		productorderingV1DomainDraftDetails.setNotes(dratOrderNotification.getNotice());

		verify(productOrderingV1Api,times(1)).postProductOrderingV1CartsCartSave(TENANT, shoppingCart.getId(),
				productorderingV1DomainDraftDetails, null);
	}
}