package com.tsystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class E2EFlowTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void context_and_database_are_up() {
        // if Flyway neither PostgreSQL haven't started test would fail
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from users",
                Integer.class
        );

        assertNotNull(count);
    }
}

