package com.driveeasy.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway: single entry point (port 8080).
 * The frontend only knows http://localhost:8080/api/... and never calls the services directly.
 * Routes are declared in config-repo/api-gateway.yml.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
