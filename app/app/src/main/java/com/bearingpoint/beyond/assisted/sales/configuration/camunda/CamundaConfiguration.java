package com.bearingpoint.beyond.test-bpintegration.configuration.camunda;

import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class CamundaConfiguration {
    @Value("${camunda.dbdriver}")
    private String camundaDriver;
    @Value("${camunda.dburl}")
    private String camundaUrl;
    @Value("${camunda.dbuser}")
    private String camudaUser;
    @Value("${camunda.dbpass}")
    private String camundaPass;

    @Value("${spring.datasource.driver}")
    private String dbDriver;
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.userName}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPass;

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(dbDriver)
                .url(dbUrl)
                .username(dbUser)
                .password(dbPass)
                .build();
    }
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager();
    }

    @Bean(name = "camundaBpmDataSource")
    @ConfigurationProperties(prefix = "datasource.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(camundaDriver)
                .url(camundaUrl)
                .username(camudaUser)
                .password(camundaPass)
                .build();
    }


    @Bean(name = "camundaBpmTransactionManager")
    public PlatformTransactionManager camundaTransactionManager(@Qualifier("camundaBpmDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SpringProcessEngineConfiguration engineConfiguration(@Qualifier("camundaBpmDataSource") DataSource secondaryDataSource, @Qualifier("camundaBpmTransactionManager") PlatformTransactionManager transactionManager,
                                                                @Value("classpath*:*.bpmn") Resource[] deploymentResources) {
        SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
        configuration.setDatabaseSchemaUpdate("true");
        configuration.setDataSource(secondaryDataSource);
        configuration.setTransactionManager(transactionManager);
        configuration.setJobExecutorActivate(true);
        configuration.setDeploymentResources(deploymentResources);
        configuration.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_FULL);
        configuration.setJobExecutorDeploymentAware(true);
        configuration.setDatabaseSchema("camunda_as");
        configuration.setDatabaseTablePrefix("camunda_as.");
        configuration.setProcessEngineName("assisted_sales_app");
        configuration.setDeploymentName("assisted_sales_app_deployment");
        configuration.setDeploymentTenantId("TELUS_GPC");
        return configuration;
    }

}
