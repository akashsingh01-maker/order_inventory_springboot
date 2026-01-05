package com.example.orderservice.model;

import java.util.List;

public class Order {
    private String id;
    private String createdAt;
    private String updatedAt;
    private String status; // CREATED, CONFIRMED, CANCELED
    private String customerId;
    private List<com.example.orderservice.dto.OrderItem> items;
    private int totalAmount;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public List<com.example.orderservice.dto.OrderItem> getItems() { return items; }
    public void setItems(List<com.example.orderservice.dto.OrderItem> items) { this.items = items; }
    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
}
