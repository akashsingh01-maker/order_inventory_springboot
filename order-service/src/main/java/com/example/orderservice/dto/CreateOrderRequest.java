package com.example.orderservice.dto;

import java.util.List;
import java.util.Map;

public class CreateOrderRequest {
    private String customerId;
    private List<OrderItem> items;
    private Map<String,Object> metadata;

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public Map<String,Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String,Object> metadata) { this.metadata = metadata; }
}
