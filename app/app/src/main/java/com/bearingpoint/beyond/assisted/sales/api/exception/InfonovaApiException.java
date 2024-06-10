package com.bearingpoint.beyond.test-bpintegration.api.exception;

import org.springframework.web.reactive.function.client.WebClientResponseException;

public class InfonovaApiException extends RuntimeException {

    public InfonovaApiException(WebClientResponseException e) {
        super(e.getMessage() + " Response: " + e.getResponseBodyAsString() + ", Cause: " + e.getCause());
    }
}
