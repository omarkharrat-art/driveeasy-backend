package com.driveeasy.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * client-service : manages customers, their identity and driving licence validity.
 * Data is kept in memory (no database, as required by the spec).
 */
@SpringBootApplication
public class ClientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientServiceApplication.class, args);
    }
}
