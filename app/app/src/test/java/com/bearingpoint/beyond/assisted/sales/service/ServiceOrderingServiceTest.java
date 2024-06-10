package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.api.ServiceOrderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrder;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrderingNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.time.temporal.ChronoUnit;

import static com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrderingNotification.EventTypeEnum.ORDERINFLIGHTSTATECHANGENOTIFICATION;
import static com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.model.ServiceorderingV1DomainServiceOrderingNotification.EventTypeEnum.ORDERSTATECHANGENOTIFICATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderingServiceTest {

	private static final String EXTERNAL_ID = "23974234";
	private static final String TENANT = "SB_TELUS_SIT_WS";
	private static final String SERVICE_ORDER_ID = "21414";
	private static final String REQUEST_ID = "253235253";

	@Mock
	ServiceOrderingV1Api serviceOrderingV1Api;

	@InjectMocks
	ServiceOrderingService unit;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(unit, "objectMapper", mock(ObjectMapper.class));
	}

	@Test
	void getServiceOrderByExternalId() {
		final ServiceorderingV1DomainServiceOrder so = mock(ServiceorderingV1DomainServiceOrder.class);
		when(serviceOrderingV1Api.getServiceorderingV1Id(TENANT, EXTERNAL_ID)).thenReturn(Mono.just(so));
		assertThat(unit.getServiceOrderByExternalId(EXTERNAL_ID, TENANT)).isEqualTo(so);
	}

	@Test
	void setServiceOrderInProgress() throws Exception {

		final ArgumentCaptor<ServiceorderingV1DomainServiceOrderingNotification> completeServOrderArgCpt = ArgumentCaptor.forClass(
				ServiceorderingV1DomainServiceOrderingNotification.class);
		when(serviceOrderingV1Api.postServiceorderingV1Notification(eq(TENANT), completeServOrderArgCpt.capture(),
				eq(REQUEST_ID))).thenReturn(Mono.empty());

		unit.setServiceOrderInProgress(TENANT, SERVICE_ORDER_ID, REQUEST_ID);

		assertThat(completeServOrderArgCpt.getValue()).isNotNull();
		assertThat(completeServOrderArgCpt.getValue().getEventType()).isEqualTo(ORDERSTATECHANGENOTIFICATION);
		assertThat(completeServOrderArgCpt.getValue().getId()).isNotBlank();
		assertThat(completeServOrderArgCpt.getValue().getDateTime()).isCloseToUtcNow(byLessThan(1, ChronoUnit.SECONDS));

		final ServiceorderingV1DomainServiceOrder serviceOrder = completeServOrderArgCpt.getValue().getServiceOrder();
		assertThat(serviceOrder).isNotNull();
		assertThat(serviceOrder.getExternalId()).isEqualTo(SERVICE_ORDER_ID);
		assertThat(serviceOrder.getPriority()).isEqualTo(4);
		assertThat(serviceOrder.getCancelable()).isFalse();
		assertThat(serviceOrder.getState()).isEqualTo("InProgress");
	}

	@Test
	void sendOrderStateChangeNotification() throws Exception {
		final ArgumentCaptor<ServiceorderingV1DomainServiceOrderingNotification> completeServOrderArgCpt = ArgumentCaptor.forClass(
				ServiceorderingV1DomainServiceOrderingNotification.class);
		when(serviceOrderingV1Api.postServiceorderingV1Notification(eq(TENANT), completeServOrderArgCpt.capture(),
				eq(REQUEST_ID))).thenReturn(Mono.
				empty());
		final ServiceorderingV1DomainServiceOrder serviceOrder = mock(ServiceorderingV1DomainServiceOrder.class);
		when(serviceOrder.getState()).thenReturn("serviceOrderState");

		unit.sendOrderStateChangeNotification(TENANT, serviceOrder, REQUEST_ID);

		assertThat(completeServOrderArgCpt.getValue()).isNotNull();
		assertThat(completeServOrderArgCpt.getValue().getEventType()).isEqualTo(ORDERSTATECHANGENOTIFICATION);
		assertThat(completeServOrderArgCpt.getValue().getId()).isNotBlank();
		assertThat(completeServOrderArgCpt.getValue().getDateTime()).isCloseToUtcNow(byLessThan(1, ChronoUnit.SECONDS));

		assertThat(completeServOrderArgCpt.getValue().getServiceOrder()).isEqualTo(serviceOrder);
	}

	@Test
	void sendOrderInflightStateChangeNotification() throws Exception {
		final ArgumentCaptor<ServiceorderingV1DomainServiceOrderingNotification> completeServOrderArgCpt = ArgumentCaptor.forClass(
				ServiceorderingV1DomainServiceOrderingNotification.class);
		when(serviceOrderingV1Api.postServiceorderingV1Notification(eq(TENANT), completeServOrderArgCpt.capture(),
				eq(REQUEST_ID))).thenReturn(Mono.
				empty());
		final ServiceorderingV1DomainServiceOrder serviceOrder = mock(ServiceorderingV1DomainServiceOrder.class);
		final ServiceorderingV1DomainServiceOrderingNotification.InFlightOrderChangeStateEnum changesaccepted =
				ServiceorderingV1DomainServiceOrderingNotification.InFlightOrderChangeStateEnum.CHANGESACCEPTED;

		unit.sendOrderInflightStateChangeNotification(TENANT, serviceOrder, REQUEST_ID, changesaccepted);

		assertThat(completeServOrderArgCpt.getValue()).isNotNull();
		assertThat(completeServOrderArgCpt.getValue().getEventType()).isEqualTo(ORDERINFLIGHTSTATECHANGENOTIFICATION);
		assertThat(completeServOrderArgCpt.getValue().getId()).isNotBlank();
		assertThat(completeServOrderArgCpt.getValue().getDateTime()).isCloseToUtcNow(byLessThan(1, ChronoUnit.SECONDS));
		assertThat(completeServOrderArgCpt.getValue().getInFlightOrderChangeState()).isEqualTo(changesaccepted);
		assertThat(completeServOrderArgCpt.getValue().getServiceOrder()).isEqualTo(serviceOrder);

	}
}