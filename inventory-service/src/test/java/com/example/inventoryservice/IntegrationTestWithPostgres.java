package com.example.inventoryservice;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
public class IntegrationTestWithPostgres {

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:14")
            .withDatabaseName("inventorydb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Test
    public void contextLoads() {
        // This test ensures Testcontainers starts a Postgres instance and Spring context loads
    }
}
