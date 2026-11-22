package com.driveeasy.location.controller;

import com.driveeasy.location.dto.*;
import com.driveeasy.location.model.StatutLocation;
import com.driveeasy.location.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

/** REST API of the rentals (HTTP only; rules and orchestration are in LocationService). */
@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService service;

    public LocationController(LocationService service) {
        this.service = service;
    }

    /** 200 - paginated list, ex: /locations?statut=EN_COURS&clientId=1&page=0&size=10 */
    @GetMapping
    public PageResponse<LocationResponse> lister(
            @RequestParam(required = false) StatutLocation statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.rechercher(statut, clientId, vehiculeId, dateDebut, dateFin, page, size);
    }

    /** 200 - dashboard indicators */
    @GetMapping("/statistiques")
    public StatistiquesResponse statistiques() {
        return service.statistiques();
    }

    /** 200 - true if the customer has a RESERVEE / EN_COURS rental (called by client-service) */
    @GetMapping("/clients/{clientId}/actives")
    public boolean clientADesLocationsActives(@PathVariable Long clientId) {
        return service.clientADesLocationsActives(clientId);
    }

    /** 200 - rental + customer + vehicle, or 404 */
    @GetMapping("/{id}")
    public LocationDetailResponse detail(@PathVariable Long id) {
        return service.detail(id);
    }

    /** 200 - price simulation, nothing is created */
    @PostMapping("/simulation")
    public SimulationResponse simuler(@Valid @RequestBody LocationRequest request) {
        return service.simuler(request);
    }

    /** 201 - reservation, or 400 (not eligible / bad dates) / 404 / 409 (vehicle taken) / 503 */
    @PostMapping
    public ResponseEntity<LocationResponse> creer(@Valid @RequestBody LocationRequest request) {
        LocationResponse creee = service.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creee.id()).toUri();
        return ResponseEntity.created(location).body(creee);
    }

    /** 200 - RESERVEE -> EN_COURS */
    @PatchMapping("/{id}/depart")
    public LocationResponse depart(@PathVariable Long id) {
        return service.depart(id);
    }

    /** 200 - EN_COURS -> TERMINEE, penalties computed */
    @PatchMapping("/{id}/retour")
    public LocationResponse retour(@PathVariable Long id, @Valid @RequestBody RetourRequest request) {
        return service.retour(id, request);
    }

    /** 200 - RESERVEE -> ANNULEE */
    @PatchMapping("/{id}/annulation")
    public LocationResponse annuler(@PathVariable Long id) {
        return service.annuler(id);
    }
}
