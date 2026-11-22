package com.driveeasy.client.controller;

import com.driveeasy.client.dto.*;
import com.driveeasy.client.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/** REST API of the customers (HTTP only; rules are in ClientService). */
@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    /** 200 - search, ex: /clients?nom=ben or /clients?cin=0912 */
    @GetMapping
    public List<ClientResponse> rechercher(@RequestParam(required = false) String nom,
                                           @RequestParam(required = false) String cin,
                                           @RequestParam(required = false) String email) {
        return service.rechercher(nom, cin, email);
    }

    /** 200 or 404 */
    @GetMapping("/{id}")
    public ClientResponse detail(@PathVariable Long id) {
        return service.trouverParId(id);
    }

    /** 201 + Location, or 400 / 409 */
    @PostMapping
    public ResponseEntity<ClientResponse> creer(@Valid @RequestBody ClientRequest request) {
        ClientResponse cree = service.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(cree.id()).toUri();
        return ResponseEntity.created(location).body(cree);
    }

    /** 200, or 400 / 404 / 409 */
    @PutMapping("/{id}")
    public ClientResponse modifier(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return service.modifier(id, request);
    }

    /** 204, or 404 / 409 (active rental) / 503 (location-service down) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    /** 200 - can this customer rent? optional dateFin = end of the planned rental */
    @GetMapping("/{id}/eligibilite")
    public EligibiliteResponse eligibilite(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return service.eligibilite(id, dateFin);
    }

    /** 200 - loyalty update, called by location-service */
    @PatchMapping("/{id}/categorie")
    public ClientResponse changerCategorie(@PathVariable Long id, @Valid @RequestBody CategorieRequest request) {
        return service.changerCategorie(id, request.categorie());
    }
}
