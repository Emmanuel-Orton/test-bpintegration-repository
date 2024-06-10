package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.model.TaskV2DomainTask;
import com.bearingpoint.beyond.test-bpintegration.service.defs.TenantType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
class InfonovaLinkServiceTest {

	private static final String ORDER_ID_PARAM = "49526835";
	private static final String BILLING_ACCOUNT_PARAM = "billing_account_param";
	private static final String MARKET_PLACE_URL = "https://infonova.com/marketpace";
	private static final String PARTNER_HUB_URL = "https://infonova.com/partnerhub";
	private static final String WHOLESALE_TENANT = "SB_TELUS_SIT_WS";
	private static final String RETAIL_TENANT = "SB_TELUS_SIT";
	private static final String BILLING_ACCOUNT = "92353465349";

	InfonovaLinkService unit;

	@BeforeEach
	void setUp() {
		unit = new InfonovaLinkService();
		ReflectionTestUtils.setField(unit, "marketPlaceUrl", MARKET_PLACE_URL);
		ReflectionTestUtils.setField(unit, "partnerHubUrl", PARTNER_HUB_URL);
		ReflectionTestUtils.setField(unit, "wholesaleTenant", WHOLESALE_TENANT);
		ReflectionTestUtils.setField(unit, "retailTenant", RETAIL_TENANT);

	}

	@Test
	void getOrderLinkFromTask() {
		final TaskV2DomainTask task = Mockito.mock(TaskV2DomainTask.class);

		Mockito.when(task.getParameters())
				.thenReturn(Map.of(ORDER_ID_PARAM, "ORDER_ID", BILLING_ACCOUNT_PARAM, "BILLING_ACCOUNT"));

		final String orderLinkFromTask = unit.getOrderLinkFromTask(task, TenantType.RETAIL, ORDER_ID_PARAM,
				BILLING_ACCOUNT_PARAM);

		assertThat(orderLinkFromTask)
				.isEqualTo(
						"https://infonova.com/marketpace/r6-ui/SB_TELUS_SIT/index#!/customermanagement/customer/BILLING_ACCOUNT/offer/orders/ORDER_ID");
	}

	@Test
	void getRetailCustomerAccountLink() {
		final String retailCustomerAccountLink = unit.getRetailCustomerAccountLink(BILLING_ACCOUNT);
		assertThat(retailCustomerAccountLink)
				.isEqualTo(
						"https://infonova.com/marketpace/r6-ui/SB_TELUS_SIT/index#!/customermanagement/customer/92353465349/account/profile");
	}

	@Test
	void getOrderLink() {
		final String orderLink = unit.getOrderLink(TenantType.RETAIL, ORDER_ID_PARAM, BILLING_ACCOUNT_PARAM);
		assertThat(orderLink)
				.isEqualTo(
						"https://infonova.com/marketpace/r6-ui/SB_TELUS_SIT/index#!/customermanagement/customer/billing_account_param/offer/orders/49526835");
	}

	@Test
	void getDraftOrderLink() {
		final String draftOrderLink = unit.getDraftOrderLink(TenantType.RETAIL, ORDER_ID_PARAM, BILLING_ACCOUNT_PARAM);
		assertThat(draftOrderLink)
				.isEqualTo(
						"https://infonova.com/marketpace/r6-ui/SB_TELUS_SIT/index#!/customermanagement/customer/billing_account_param/add/wizard/orderdetails?prospectOrderId=49526835&backTo=drafts");
	}

	@Test
	void getTenantApiPrefix() {
		BiConsumer<TenantType, String> checkTypeHasApiPrefix = (tenantType, expectedPrefix) ->
				assertThat(unit.getTenantApiPrefix(tenantType)).as(
						String.format("TenantType - %s has wrong api prefix", tenantType)).isEqualTo(expectedPrefix);

		for (TenantType tenantType : TenantType.values()) {
			switch (tenantType) {
			case RETAIL:
			case WHOLESALE:
				checkTypeHasApiPrefix.accept(tenantType, "r6-ui");
				break;
			case WHOLESALE_CSM:
				checkTypeHasApiPrefix.accept(tenantType, "r6-csm");
				break;
			default:
				fail(tenantType + " TenantType not covered by this test");
			}
		}
	}

	@Test
	void getCorrectTenantUrl() {
		BiConsumer<TenantType, String> checkTypeHasCorrectTenantUrl = (tenantType, expectedTenantUrl) ->
				assertThat(unit.getCorrectTenantUrl(tenantType)).as(
						String.format("TenantType - %s has wrong tenantUrl", tenantType)).isEqualTo(expectedTenantUrl);

		for (TenantType tenantType : TenantType.values()) {
			switch (tenantType) {
			case RETAIL:
				checkTypeHasCorrectTenantUrl.accept(tenantType, MARKET_PLACE_URL);
				break;
			case WHOLESALE:
			case WHOLESALE_CSM:
				checkTypeHasCorrectTenantUrl.accept(tenantType, PARTNER_HUB_URL);
				break;
			default:
				fail(tenantType + " TenantType not covered by this test");
			}
		}
	}

	@Test
	void getCorrectTenant() {
		BiConsumer<TenantType, String> check = (tenantType, expected) ->
				assertThat(unit.getCorrectTenant(tenantType)).as(
								String.format("getCorrectTenant() with TenantType - %s returns wrong result", tenantType))
						.isEqualTo(expected);

		for (TenantType tenantType : TenantType.values()) {
			switch (tenantType) {
			case RETAIL:
				check.accept(tenantType, RETAIL_TENANT);
				break;
			case WHOLESALE:
			case WHOLESALE_CSM:
				check.accept(tenantType, WHOLESALE_TENANT);
				break;
			default:
				fail(tenantType + " TenantType not covered by this test");
			}
		}
	}
}
