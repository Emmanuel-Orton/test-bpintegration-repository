package com.bearingpoint.beyond.test-bpintegration.api.frontend;

import com.bearingpoint.beyond.test-bpintegration.api.frontend.domain.DraftOrderNotification;
import com.bearingpoint.beyond.test-bpintegration.service.DraftOrderNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DraftOrderNotificationControllerTest {
	private static final String TENANT_URL = "/SB_TELUS_SIT";
	private static final String TENANT = "SB_TELUS_SIT";
	private static final String DRAFT_ORDER = "draftOrder";

	MockMvc mockMvc;

	ObjectMapper om;

	@Mock
	DraftOrderNotificationService draftOrderNotificationService;

	@InjectMocks
	DraftOrderNotificationController controller;

	@BeforeEach
	public void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		om = new ObjectMapper();
	}

	@Test
	void sendNotificationAndUpdateOrder() throws Exception {
		final DraftOrderNotification notification = DraftOrderNotification.builder()
				.billingAccountId("1")
				.to("2")
				.cc("3")
				.operatorName("operatorName")
				.customerBusinessName("customerBusinessName")
				.notice("notice")
				.build();
		mockMvc.perform(post(TENANT_URL + "/v1/draftOrdering/" + DRAFT_ORDER)
						.contentType(MediaType.APPLICATION_JSON)
						.content(om.writeValueAsString(notification))
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isAccepted());
		verify(draftOrderNotificationService, times(1)).sendNotificationAndUpdateOrder(TENANT, DRAFT_ORDER,
				notification);
	}
}