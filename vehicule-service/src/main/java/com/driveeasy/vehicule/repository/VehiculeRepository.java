package com.driveeasy.vehicule.repository;

import com.driveeasy.vehicule.model.Vehicule;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory storage (no database allowed).
 * ConcurrentHashMap + AtomicLong: safe when several HTTP requests arrive at the same time.
 * Data is lost when the service restarts (re-created by DataInitializer).
 */
@Repository
public class VehiculeRepository {

    private final Map<Long, Vehicule> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public List<Vehicule> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Vehicule> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    /** Creates (id null -> new id) or replaces a vehicle. */
    public Vehicule save(Vehicule v) {
        if (v.getId() == null) {
            v.setId(sequence.incrementAndGet());
        }
        store.put(v.getId(), v);
        return v;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }

    /** Used for the uniqueness rule (case-insensitive). */
    public Optional<Vehicule> findByImmatriculation(String immatriculation) {
        return store.values().stream()
                .filter(v -> v.getImmatriculation().equalsIgnoreCase(immatriculation))
                .findFirst();
    }
}
