package com.driveeasy.location.client;

import com.driveeasy.location.dto.VehiculeDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Synchronous calls to vehicule-service, resolved through Eureka ("http://vehicule-service"). */
@Component
public class VehiculeServiceClient {

    private static final String SERVICE = "vehicule-service";
    private final RestClient restClient;

    public VehiculeServiceClient(RestClient.Builder loadBalancedBuilder) {
        // clone(): the shared builder is not modified (client-service uses it too)
        this.restClient = loadBalancedBuilder.clone().baseUrl("http://" + SERVICE).build();
    }

    /** GET /vehicules/{id} : price, category coefficient, statut, kilometrage. */
    public VehiculeDto getVehicule(Long id) {
        try {
            return restClient.get().uri("/vehicules/{id}", id).retrieve().body(VehiculeDto.class);
        } catch (RuntimeException ex) {
            throw RemoteErrorTranslator.translate(SERVICE, ex);
        }
    }

    /** GET /vehicules : whole fleet (statistics). */
    public List<VehiculeDto> lister() {
        try {
            return restClient.get().uri("/vehicules").retrieve()
                    .body(new ParameterizedTypeReference<List<VehiculeDto>>() {});
        } catch (RuntimeException ex) {
            throw RemoteErrorTranslator.translate(SERVICE, ex);
        }
    }

    /** PATCH /vehicules/{id}/statut : LOUE on departure, DISPONIBLE (+ km) on return. */
    public void changerStatut(Long id, String statut, Integer kilometrage) {
        Map<String, Object> body = new HashMap<>();
        body.put("statut", statut);
        body.put("kilometrage", kilometrage);   // null = unchanged
        try {
            restClient.patch().uri("/vehicules/{id}/statut", id).contentType(MediaType.APPLICATION_JSON).body(body).retrieve().toBodilessEntity();
        } catch (RuntimeException ex) {
            throw RemoteErrorTranslator.translate(SERVICE, ex);
        }
    }
}
