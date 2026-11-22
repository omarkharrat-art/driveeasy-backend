package com.driveeasy.location.config;

import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * HTTP client used to call the other microservices.
 * - @LoadBalanced : "http://<service-name>" is resolved through Eureka (no hard-coded host/port)
 * - RestClientBuilderConfigurer : keeps Spring Boot's settings, including tracing,
 *   so the traceId is propagated to the called service (visible in Zipkin)
 */
@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder(RestClientBuilderConfigurer configurer) {
        return configurer.configure(RestClient.builder());
    }
}
