package ru.teamscore.busroutes.config.test;


import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = {TestConfig.class})
@ActiveProfiles("test")
@Sql(value = "/testdb/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/testdb/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ExtendWith(DatabaseCleaner.class)
public @interface IntegrationTest {
}
