package com.example.orderservice.service;

public interface IdempotencyService {
    boolean exists(String key);
    void save(String key, String responseJson, int ttlSeconds);
    String getResponse(String key);
}
