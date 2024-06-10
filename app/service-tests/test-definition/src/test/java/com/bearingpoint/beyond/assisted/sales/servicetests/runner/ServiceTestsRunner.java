package com.bearingpoint.beyond.test-bpintegration.servicetests.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/",
        glue = { "com.bearingpoint.beyond.test-bpintegration.servicetests.steps", "com.bearingpoint.beyond.test-bpintegration.servicetests.context", "com.bearingpoint.beyond.test-bpintegration.servicetests.config",
                "com.bearingpoint.beyond.test-bpintegration.servicetests.hooks"},
        plugin = {
                "pretty",
                "json:target/cucumber-reports/CucumberTestReport.json",
                "html:target/cucumber-reports/CucumberTestReport.html",
                "rerun:target/cucumber-reports/rerun.txt"
        }
)
public class ServiceTestsRunner {

}
