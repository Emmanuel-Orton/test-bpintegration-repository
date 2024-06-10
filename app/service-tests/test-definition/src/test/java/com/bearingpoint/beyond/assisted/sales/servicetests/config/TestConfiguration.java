package com.bearingpoint.beyond.test-bpintegration.servicetests.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"com.bearingpoint.beyond.test-bpintegration.servicetests.helper", "com.bearingpoint.beyond.test-bpintegration.servicetests.context"})
public class TestConfiguration {
}
