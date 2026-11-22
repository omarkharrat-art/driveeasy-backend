package com.driveeasy.vehicule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * vehicule-service : manages the fleet (cars, categories, availability).
 * Data is kept in memory (no database, as required by the spec).
 */
@SpringBootApplication
public class VehiculeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehiculeServiceApplication.class, args);
    }
}
