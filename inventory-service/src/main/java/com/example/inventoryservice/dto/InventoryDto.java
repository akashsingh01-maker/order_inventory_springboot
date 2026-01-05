package com.example.inventoryservice.dto;

public class InventoryDto {
    private String productId;
    private int available;
    private int reserved;
    private String updatedAt;

    public InventoryDto() {}
    public InventoryDto(String productId, int available, int reserved, String updatedAt) {
        this.productId = productId; this.available = available; this.reserved = reserved; this.updatedAt = updatedAt;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }
    public int getReserved() { return reserved; }
    public void setReserved(int reserved) { this.reserved = reserved; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
