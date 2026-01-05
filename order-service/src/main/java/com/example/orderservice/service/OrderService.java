package com.example.orderservice.service;

import java.util.UUID;

public interface OrderService {
    String createOrder(String customerId, java.util.List<com.example.orderservice.dto.OrderItem> items, String idempotencyKey);
    void confirmOrder(String orderId, String idempotencyKey) throws Exception;
    void cancelOrder(String orderId, String idempotencyKey) throws Exception;
}
