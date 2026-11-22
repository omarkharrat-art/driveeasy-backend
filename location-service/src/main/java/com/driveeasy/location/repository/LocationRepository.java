package com.driveeasy.location.repository;

import com.driveeasy.location.model.Location;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory storage (no database allowed), thread-safe. */
@Repository
public class LocationRepository {

    private final Map<Long, Location> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public List<Location> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Location> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Location save(Location l) {
        if (l.getId() == null) {
            l.setId(sequence.incrementAndGet());
        }
        store.put(l.getId(), l);
        return l;
    }

    public List<Location> findByClientId(Long clientId) {
        return store.values().stream().filter(l -> l.getClientId().equals(clientId)).toList();
    }

    public List<Location> findByVehiculeId(Long vehiculeId) {
        return store.values().stream().filter(l -> l.getVehiculeId().equals(vehiculeId)).toList();
    }
}
