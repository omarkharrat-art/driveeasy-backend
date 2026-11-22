package com.driveeasy.location.client;

import com.driveeasy.location.dto.ClientDto;
import com.driveeasy.location.dto.EligibiliteDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Map;

/** Synchronous calls to client-service, resolved through Eureka ("http://client-service"). */
@Component
public class ClientServiceClient {

    private static final String SERVICE = "client-service";
    private final RestClient restClient;

    public ClientServiceClient(RestClient.Builder loadBalancedBuilder) {
        this.restClient = loadBalancedBuilder.clone().baseUrl("http://" + SERVICE).build();
    }

    public ClientDto getClient(Long id) {
        try {
            return restClient.get().uri("/clients/{id}", id).retrieve().body(ClientDto.class);
        } catch (RuntimeException ex) {
            throw RemoteErrorTranslator.translate(SERVICE, ex);
        }
    }

    /** GET /clients/{id}/eligibilite?dateFin=.. : age >= 21 and licence valid until the end of the rental. */
    public EligibiliteDto eligibilite(Long id, LocalDate dateFin) {
        try {
            return restClient.get()
                    .uri("/clients/{id}/eligibilite?dateFin={dateFin}", id, dateFin)
                    .retrieve().body(EligibiliteDto.class);
        } catch (RuntimeException ex) {
            throw RemoteErrorTranslator.translate(SERVICE, ex);
        }
    }

    /** PATCH /clients/{id}/categorie : loyalty upgrade (FIDELE / VIP). */
    public void changerCategorie(Long id, String categorie) {
        try {
            restClient.patch().uri("/clients/{id}/categorie", id)
                    .contentType(MediaType.APPLICATION_JSON).body(Map.of("categorie", categorie)).retrieve().toBodilessEntity();
        } catch (RuntimeException ex) {
            throw RemoteErrorTranslator.translate(SERVICE, ex);
        }
    }
}
