package com.bearingpoint.beyond.test-bpintegration.service;

import com.bearingpoint.beyond.test-bpintegration.api.exception.InfonovaApiException;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.templaterendering.api.TemplateRenderingV1Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateRenderingService {

    private final TemplateRenderingV1Api templateRenderingV1Api;

    public byte[] renderPdf(String tenant, String templateName, Object orderSummary) {
        return templateRenderingV1Api.postTemplateRenderingV1ReportTemplatesName(tenant, templateName, null, orderSummary)
                .onErrorMap(WebClientResponseException.class, InfonovaApiException::new)
                .block();
    }
}
