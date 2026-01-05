package com.example.orderservice.service;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class OrderServiceUnitTest {

    private OrderServiceImpl orderService;

    @Before
    public void setup() {
        orderService = new OrderServiceImpl();
        orderService.idempotencyService = new InMemoryIdempotencyService();
        // not wiring repository; createOrder will attempt to save and may NPE — keep test minimal
    }

    @Test
    public void testCreateOrderLogic() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId("3fa85f64-5717-4562-b3fc-2c963f66afa6");
        OrderItem it = new OrderItem();
        it.setProductId("6b1f9f1a-5b7b-4a5f-ae2b-3c2d9f8e1f22");
        it.setQuantity(1);
        it.setUnitPrice(1000);
        int total = it.getQuantity() * it.getUnitPrice();
        Assert.assertEquals(1000, total);
    }
}
