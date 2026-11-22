package com.driveeasy.client.client;

import com.driveeasy.client.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Calls location-service to know if a customer still has an active rental
 * (RESERVEE or EN_COURS). Used before deleting a customer.
 */
@Component
public class LocationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(LocationServiceClient.class);

    private final RestClient restClient;

    public LocationServiceClient(RestClient.Builder loadBalancedBuilder) {
        // "location-service" = name registered in Eureka
        this.restClient = loadBalancedBuilder.baseUrl("http://location-service").build();
    }

    public boolean aDesLocationsActives(Long clientId) {
        try {
            Boolean actives = restClient.get()
                    .uri("/locations/clients/{clientId}/actives", clientId)
                    .retrieve()
                    .body(Boolean.class);
            return Boolean.TRUE.equals(actives);
        } catch (RuntimeException ex) {
            // location-service down or unreachable: we refuse rather than risk an inconsistency
            log.warn("location-service injoignable : {}", ex.getMessage());
            throw new ServiceUnavailableException(
                    "Impossible de verifier les locations du client (location-service indisponible)");
        }
    }
}
