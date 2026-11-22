package com.driveeasy.location;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * location-service : rental lifecycle (reservation, departure, return, cancellation).
 * Orchestrates calls to vehicule-service and client-service.
 */
@SpringBootApplication
@ConfigurationPropertiesScan   // activates PricingProperties (driveeasy.location.*)
public class LocationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocationServiceApplication.class, args);
    }
}
