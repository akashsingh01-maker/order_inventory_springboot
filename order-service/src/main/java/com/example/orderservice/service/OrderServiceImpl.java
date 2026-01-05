package com.example.orderservice.service;

import com.example.orderservice.persistence.OrderEntity;
import com.example.orderservice.persistence.OrderItemEntity;
import com.example.orderservice.persistence.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    IdempotencyService idempotencyService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Override
    @Transactional
    public String createOrder(String customerId, List<com.example.orderservice.dto.OrderItem> items, String idempotencyKey) {
        String id = UUID.randomUUID().toString();
        OrderEntity e = new OrderEntity();
        e.setId(id);
        e.setCustomerId(customerId);
        e.setStatus("CREATED");
        e.setCreatedAt(java.time.Instant.now().toString());
        e.setUpdatedAt(e.getCreatedAt());
        int total = items.stream().mapToInt(i->i.getQuantity()*i.getUnitPrice()).sum();
        e.setTotalAmount(total);
        List<OrderItemEntity> itemEntities = items.stream().map(i -> {
            OrderItemEntity ie = new OrderItemEntity();
            ie.setId(UUID.randomUUID().toString());
            ie.setProductId(i.getProductId());
            ie.setQuantity(i.getQuantity());
            ie.setUnitPrice(i.getUnitPrice());
            return ie;
        }).collect(Collectors.toList());
        e.setItems(itemEntities);
        orderRepository.save(e);
        if (idempotencyKey != null) idempotencyService.save(idempotencyKey, id, 3600);
        return id;
    }

    @Override
    @Transactional
    public void confirmOrder(String orderId, String idempotencyKey) throws Exception {
        OrderEntity order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("order not found"));
        if (!"CREATED".equals(order.getStatus())) throw new RuntimeException("invalid state");
        // call inventory service to reserve; inventory will lock rows pessimistically
        WebClient client = webClientBuilder.baseUrl("http://inventory-service:8082").build();
        try {
            for (OrderItemEntity item : order.getItems()) {
                client.post()
                        .uri(uriBuilder -> uriBuilder.path("/inventory/{productId}/reserve").build(item.getProductId()))
                        .bodyValue(java.util.Collections.singletonMap("quantity", item.getQuantity()))
                        .retrieve()
                        .toBodilessEntity()
                        .block();
            }
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("reservation failed: " + ex.getResponseBodyAsString());
        }
        order.setStatus("CONFIRMED");
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderId, String idempotencyKey) throws Exception {
        OrderEntity order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("order not found"));
        if ("CONFIRMED".equals(order.getStatus())) {
            WebClient client = webClientBuilder.baseUrl("http://inventory-service:8082").build();
            for (OrderItemEntity item : order.getItems()) {
                client.post()
                        .uri(uriBuilder -> uriBuilder.path("/inventory/{productId}/release").build(item.getProductId()))
                        .bodyValue(java.util.Collections.singletonMap("quantity", item.getQuantity()))
                        .retrieve()
                        .toBodilessEntity()
                        .block();
            }
        }
        order.setStatus("CANCELED");
        orderRepository.save(order);
    }
}
