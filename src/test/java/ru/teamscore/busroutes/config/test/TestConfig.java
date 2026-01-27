package ru.teamscore.busroutes.config.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

@TestConfiguration
public class TestConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:16-alpine");
    }

    @Bean
    public Clock clock() {
        Instant instant = Instant.parse("2025-01-29T13:03:00.00Z");
        ZoneId zoneId = ZoneOffset.ofHours(4);
        return Clock.fixed(instant, zoneId);
    }

}
