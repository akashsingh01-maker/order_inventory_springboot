package com.example.orderservice.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryIdempotencyService implements IdempotencyService {
    private final ConcurrentMap<String, String> store = new ConcurrentHashMap<>();

    @Override
    public boolean exists(String key) {
        return store.containsKey(key);
    }

    @Override
    public void save(String key, String responseJson, int ttlSeconds) {
        store.put(key, responseJson);
        // Note: TTL eviction not implemented in this example
    }

    @Override
    public String getResponse(String key) {
        return store.get(key);
    }
}
