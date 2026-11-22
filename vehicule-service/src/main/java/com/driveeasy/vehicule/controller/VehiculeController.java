package com.driveeasy.vehicule.controller;

import com.driveeasy.vehicule.dto.StatutRequest;
import com.driveeasy.vehicule.dto.VehiculeRequest;
import com.driveeasy.vehicule.dto.VehiculeResponse;
import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.Categorie;
import com.driveeasy.vehicule.model.StatutVehicule;
import com.driveeasy.vehicule.service.VehiculeService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * REST API of the fleet. Only HTTP concerns here (URLs, codes, bodies);
 * business rules are delegated to VehiculeService.
 */
@RestController
@RequestMapping("/vehicules")
public class VehiculeController {

    private final VehiculeService service;

    public VehiculeController(VehiculeService service) {
        this.service = service;
    }

    /** 200 - list with optional filters, ex: /vehicules?carburant=DIESEL&prixMax=100 */
    @GetMapping
    public List<VehiculeResponse> lister(@RequestParam(required = false) Categorie categorie,
                                         @RequestParam(required = false) Carburant carburant,
                                         @RequestParam(required = false) StatutVehicule statut,
                                         @RequestParam(required = false) BigDecimal prixMax) {
        return service.rechercher(categorie, carburant, statut, prixMax);
    }

    /** 200 - available vehicles, ex: /vehicules/disponibles?dateDebut=2026-12-01&dateFin=2026-12-05 */
    @GetMapping("/disponibles")
    public List<VehiculeResponse> disponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return service.disponibles(dateDebut, dateFin);
    }

    /** 200 or 404 */
    @GetMapping("/{id}")
    public VehiculeResponse detail(@PathVariable Long id) {
        return service.trouverParId(id);
    }

    /** 201 Created + Location header, or 400 / 409 */
    @PostMapping
    public ResponseEntity<VehiculeResponse> creer(@Valid @RequestBody VehiculeRequest request) {
        VehiculeResponse cree = service.creer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(cree.id()).toUri();
        return ResponseEntity.created(location).body(cree);
    }

    /** 200, or 400 / 404 / 409 */
    @PutMapping("/{id}")
    public VehiculeResponse modifier(@PathVariable Long id, @Valid @RequestBody VehiculeRequest request) {
        return service.modifier(id, request);
    }

    /** 204 No Content, or 404 / 409 (vehicle rented) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    /** 200 - used by location-service on departure / return */
    @PatchMapping("/{id}/statut")
    public VehiculeResponse changerStatut(@PathVariable Long id, @Valid @RequestBody StatutRequest request) {
        return service.changerStatut(id, request);
    }
}
