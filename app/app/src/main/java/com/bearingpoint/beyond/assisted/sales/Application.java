package com.bearingpoint.beyond.test-bpintegration;

import com.bearingpoint.beyond.test-bpintegration.configuration.InfonovaRoleProvider;
import com.bearingpoint.beyond.test-bpintegration.configuration.KeycloakRealmRoleConverter;
import com.bearingpoint.beyond.test-bpintegration.configuration.SecurityConfiguration;
import com.bearingpoint.beyond.test-bpintegration.infonova.configuration.ApiConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.ScheduledLockConfiguration;
import net.javacrumbs.shedlock.spring.ScheduledLockConfigurationBuilder;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.time.Duration;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
@EnableTransactionManagement
@Import({SecurityConfiguration.class, ApiConfiguration.class})
public class Application {
    private InfonovaRoleProvider cachedInfonovaRoleProvider;

    public Application(InfonovaRoleProvider cachedInfonovaRoleProvider) {
        this.cachedInfonovaRoleProvider = cachedInfonovaRoleProvider;
    }

    @Bean
    public KeycloakRealmRoleConverter keycloakRealmRoleConverter() {
        return new KeycloakRealmRoleConverter(this.cachedInfonovaRoleProvider);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Bean
    public ScheduledLockConfiguration schedLockTaskScheduler(LockProvider lockProvider) {
        return ScheduledLockConfigurationBuilder
                .withLockProvider(lockProvider)
                .withPoolSize(10)
                .withDefaultLockAtMostFor(Duration.ofMinutes(10))
                .build();
    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }
}
