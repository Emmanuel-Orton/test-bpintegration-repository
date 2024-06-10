package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.templaterendering.api.TemplateRenderingV1Api;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateRenderingServiceTest {

	private static final String TENANT = "SB_TELUS_SIT_WS";
	private static final String TEMPLATE_NAME = "templateName";
	private static final String ORDER_SUMMARY = "orderSummary";

	@Mock
	TemplateRenderingV1Api templateRenderingV1Api;

	@InjectMocks
	TemplateRenderingService unit;

	@Test
	void renderPdf() {
		when(templateRenderingV1Api.postTemplateRenderingV1ReportTemplatesName(anyString(), anyString(), isNull(),
				anyString())).thenReturn(
				Mono.just("test".getBytes(StandardCharsets.UTF_8)));

		Assertions.assertThat(unit.renderPdf(TENANT, TEMPLATE_NAME, ORDER_SUMMARY))
				.isEqualTo("test".getBytes(StandardCharsets.UTF_8));

		verify(templateRenderingV1Api,times(1))
				.postTemplateRenderingV1ReportTemplatesName(eq(TENANT), eq(TEMPLATE_NAME), isNull(),
						eq(ORDER_SUMMARY));
	}
}