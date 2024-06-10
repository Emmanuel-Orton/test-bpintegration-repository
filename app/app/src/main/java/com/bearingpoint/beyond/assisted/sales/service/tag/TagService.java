package com.bearingpoint.beyond.test-bpintegration.service.tag;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainDraftOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProduct;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainProductOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.model.ProductorderingV1DomainShoppingCart;
import com.bearingpoint.beyond.test-bpintegration.service.CustomerDataService;
import com.bearingpoint.beyond.test-bpintegration.service.ProductOrdersService;
import com.bearingpoint.beyond.test-bpintegration.service.helper.ServiceCharacteristicProductOrderExtractor;
import com.bearingpoint.beyond.test-bpintegration.service.helper.ServiceCharacteristicShoppingCartExtractor;
import com.bearingpoint.beyond.test-bpintegration.util.ShoppingCartUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Slf4j
@Component
@AllArgsConstructor
public class TagService {

	private final Function<String, String> replaceSpaceWithUnderscore = s -> s.replaceAll(" ", "_");

	private final ProductOrdersService productOrdersService;
	private final CustomerDataService customerDataService;

	public List<String> getCommonTagsForSow(String rtTenant, String wsTenant, String productName,
			ProductorderingV1DomainDraftOrder draftOrder, String wsBillingAccount) {
		ProductorderingV1DomainShoppingCart draftCart = productOrdersService.createCartFromDraft(rtTenant,
				draftOrder.getId());

		if (wsBillingAccount == null) {
			ProductorderingV1DomainProduct offerProduct = ShoppingCartUtil.getOfferProduct(draftCart);
			wsBillingAccount = customerDataService.getWholesaleBillingAccount(rtTenant,
					offerProduct.getProductOffering().getName());
		}

		final ServiceCharacteristicShoppingCartExtractor chrExtr = new ServiceCharacteristicShoppingCartExtractor(
				draftCart);

		String customerLegalName = chrExtr.getCustomerLegalName();
		String opiNumber = chrExtr.getOpiNumber();
		String salesAgentEmail = chrExtr.getSalesAgentEmail();

		String partnerName = customerDataService.getCustomerTradingName(wsTenant, wsBillingAccount);

		return replaceSpacesAndFilterBlankTags(List.of(
				customerLegalName,
				opiNumber,
				partnerName,
				productName,
				salesAgentEmail
		));
	}

	public List<String> getCommonTagsForSow(String rtTenant, String wsTenant, String productName,
			ProductorderingV1DomainDraftOrder draftOrder) {
		return getCommonTagsForSow(rtTenant, wsTenant, productName, draftOrder, null);
	}

	public List<String> getCommonTagsForDaf(String tenant, String wsBillingAccount, String productName,
			String wsOrderId) {
		final ProductorderingV1DomainProductOrder wsOrder = productOrdersService.getProductOrder(tenant,
				wsOrderId);
		return getCommonTagsForDaf(tenant, wsBillingAccount, productName, wsOrder);
	}

	public List<String> getCommonTagsForDaf(String wsTenant, String wsBillingAccount, String productName,
			ProductorderingV1DomainProductOrder wsOrder) {
		return getCommonTagsForDafAndCo(wsTenant, wsBillingAccount, productName, wsOrder);
	}

	public List<String> getCommonTagsForCo(String tenant, String billingAccount, String productName,
			ProductorderingV1DomainProductOrder wsOrder) {
		return getCommonTagsForDafAndCo(tenant, billingAccount, productName, wsOrder);
	}

	private List<String> getCommonTagsForDafAndCo(String wsTenant, String wsBillingAccount, String productName,
			ProductorderingV1DomainProductOrder wsOrder) {
		final ServiceCharacteristicProductOrderExtractor chrExtr = new ServiceCharacteristicProductOrderExtractor(
				wsOrder);

		String customerLegalName = chrExtr.getCustomerLegalName();
		String opiNumber = chrExtr.getOpiNumber();
		String salesAgentEmail = chrExtr.getSalesAgentEmail();

		String partnerName = customerDataService.getCustomerTradingName(wsTenant, wsBillingAccount);

		return replaceSpacesAndFilterBlankTags(List.of(
				customerLegalName,
				opiNumber,
				partnerName,
				productName,
				salesAgentEmail
		));
	}

	protected List<String> replaceSpacesAndFilterBlankTags(List<String> tags) {
		return tags.stream()
				.filter(not(String::isBlank))
				.map(replaceSpaceWithUnderscore)
				.collect(Collectors.toList());
	}
}
