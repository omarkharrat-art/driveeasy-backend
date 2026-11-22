package com.driveeasy.client.repository;

import com.driveeasy.client.model.Client;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/** In-memory storage (no database allowed), thread-safe. */
@Repository
public class ClientRepository {

    private final Map<Long, Client> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public List<Client> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Client> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Client save(Client c) {
        if (c.getId() == null) {
            c.setId(sequence.incrementAndGet());
        }
        store.put(c.getId(), c);
        return c;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }

    public Optional<Client> findByCin(String cin) {
        return findBy(Client::getCin, cin);
    }

    public Optional<Client> findByEmail(String email) {
        return findBy(Client::getEmail, email);
    }

    public Optional<Client> findByNumeroPermis(String numeroPermis) {
        return findBy(Client::getNumeroPermis, numeroPermis);
    }

    /** Generic search on one text field, case-insensitive (used for the uniqueness rules). */
    private Optional<Client> findBy(Function<Client, String> field, String value) {
        return store.values().stream()
                .filter(c -> field.apply(c).equalsIgnoreCase(value))
                .findFirst();
    }
}
