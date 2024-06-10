package com.bearingpoint.beyond.test-bpintegration.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan(value = "com.bearingpoint.beyond.test-bpintegration.configuration")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String defaultIssuer;

    @Value("${cors.allowedOrigins}")
    private String allowedOrigins;

    @Value("${cors.allowedHeaders}")
    private String allowedHeaders;

    @Value("${cors.allowedMethods}")
    private String allowedMethods;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        JwtIssuerAuthenticationManagerResolver authenticationManagerResolver = new JwtIssuerAuthenticationManagerResolver
                (defaultIssuer, defaultIssuer + "_PART");
        http.csrf().disable();
        http
                .authorizeRequests(authz -> authz
                        .requestMatchers(EndpointRequest.to(InfoEndpoint.class, HealthEndpoint.class)).permitAll()
                        .antMatchers(HttpMethod.OPTIONS, "**").permitAll()//allow CORS option calls
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .authenticationManagerResolver(authenticationManagerResolver)
                );
        http.cors();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/**");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins.split(","))
                        .allowedHeaders(allowedHeaders.split(","))
                        .allowedMethods(allowedMethods.split(","))
                        .allowCredentials(true);
            }
        };
    }
}
