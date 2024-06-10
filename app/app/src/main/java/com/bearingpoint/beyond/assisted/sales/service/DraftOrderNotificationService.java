package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.api.exception.InfonovaApiException;
import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DraftOrderNotification;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.authorization.api.AuthorizationV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.api.ProductOrderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftDetails;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.templaterendering.api.TemplateRenderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.users.api.UsersV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.users.model.UserV1DomainUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DraftOrderNotificationService {

    private final SendGridService sendGridService;
    private final TemplateRenderingV1Api templateRenderingV1Api;
    private final UsersV1Api usersV1Api;
    private final ProductOrderingV1Api productOrderingV1Api;

    public static final String DRAFTORDER_EMAIL_BODY = "notification.telus_draft_order_approval.email_my_messages.body.html.ftl";
    public static final String DRAFTORDER_EMAIL_SUBJECT = "notification.telus_draft_order_approval.email_my_messages.subject.ftl";

    public void sendNotificationAndUpdateOrder(String tenant, String draftOrderId, DraftOrderNotification dratOrderNotification) throws IOException  {

        ProductorderingV1DomainShoppingCart shoppingCartRequest = new ProductorderingV1DomainShoppingCart();
        shoppingCartRequest.billingAccount(dratOrderNotification.getBillingAccountId());
        shoppingCartRequest.draftId(draftOrderId);

        ProductorderingV1DomainShoppingCart shoppingCart = productOrderingV1Api.postProductOrderingV1Carts(tenant, shoppingCartRequest,UUID.randomUUID().toString())
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
//                .doOnError(error -> {
//                    WebClientResponseException ex = (WebClientResponseException) error;
//                    log.error("Error retrieving shopping cart for draft order {} with error : {} and response body: {}", draftOrderId, error.getMessage(), ex.getResponseBodyAsString());
//                })
                .block();

        ProductorderingV1DomainShoppingCart updatedShoppingCart = new ProductorderingV1DomainShoppingCart();

        updatedShoppingCart.setParameters(new HashMap<>());
        updatedShoppingCart.getParameters().put("availableForCustomerApproval", "true");
        updatedShoppingCart.setDraftId(draftOrderId);

        productOrderingV1Api.patchProductOrderingV1CartsCart(tenant, shoppingCart.getId(), updatedShoppingCart)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
//                .doOnError(error -> {
//                    WebClientResponseException ex = (WebClientResponseException) error;
//                    log.error("Error patching shopping cart {} with error : {} and response body: {}", shoppingCart.getId(), error.getMessage(), ex.getResponseBodyAsString());
//                })
                .block();

        ProductorderingV1DomainDraftDetails productorderingV1DomainDraftDetails = new ProductorderingV1DomainDraftDetails();
        productorderingV1DomainDraftDetails.setNotes(dratOrderNotification.getNotice());

        productOrderingV1Api.postProductOrderingV1CartsCartSave(tenant, shoppingCart.getId(), productorderingV1DomainDraftDetails, null)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();

        String subject = templateRenderingV1Api.postTemplateRenderingV1TemplatesName(tenant, DRAFTORDER_EMAIL_SUBJECT, UUID.randomUUID().toString(), Collections.emptyMap()).block();

        UserV1DomainUser domainUser = usersV1Api.getUsersV1UsersName(tenant, dratOrderNotification.getOperatorName())
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
//                .doOnError(error -> {
//                    WebClientResponseException ex = (WebClientResponseException) error;
//                    log.error("Error retrieving user information with error: {} and response body: {}", error.getMessage(), ex.getResponseBodyAsString());
//                })
                .block();
        Map<String, String> params = new HashMap<>();
        params.put("SALES_USERNAME", domainUser.getRelatedParty() != null ? domainUser.getRelatedParty().getName() : dratOrderNotification.getOperatorName());
        params.put("DRAFT_ORDER_LINK", "");
        params.put("DRAFT_ORDER_ID", draftOrderId);
        params.put("CUSTOMER_BUSINESS_NAME", dratOrderNotification.getCustomerBusinessName());
        params.put("CUSTOMER_NAME", dratOrderNotification.getCustomerBusinessName());
        String body = templateRenderingV1Api.postTemplateRenderingV1TemplatesName(tenant, DRAFTORDER_EMAIL_BODY, UUID.randomUUID().toString(), params)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
//                .doOnError(error -> {
//                    WebClientResponseException ex = (WebClientResponseException) error;
//                    log.error("Error rendering template {}: With error: {}", error.getMessage(), ex.getResponseBodyAsString());
//                })
                .block();

        sendGridService.sendEmail(dratOrderNotification.getTo(), dratOrderNotification.getCc(), subject, body);
    }


}
