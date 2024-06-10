package com.bearingpoint.beyond.test-bpintegration.servicetests.config;


import com.bearingpoint.beyond.test-bpintegration.servicetests.api.configuration.TestApiConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration(classes = {TestApiConfiguration.class, TestConfiguration.class})
public class CucumberConfiguration {

}
