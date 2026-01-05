package com.example.orderservice.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderConfirmIntegrationTest {

    public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:14")
            .withDatabaseName("inventorydb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Test
    public void contextLoads() {
        // placeholder integration test
    }
}
