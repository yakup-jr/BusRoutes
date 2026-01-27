package ru.teamscore.busroutes.config.test;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

public class DatabaseCleaner implements AfterEachCallback {

    private JdbcTemplate jdbcTemplate;

    @Override
    public void afterEach(@NonNull ExtensionContext context) {
        if (jdbcTemplate == null) {
            jdbcTemplate = SpringExtension.getApplicationContext(context)
                .getBean(JdbcTemplate.class);
        }
        cleanDatabase();
    }

    public void cleanDatabase() {
        List<String> tableNames = jdbcTemplate.queryForList(
            "select table_name from information_schema.tables where table_schema='transport'",
            String.class);
        for (String tableName : tableNames) {
            jdbcTemplate.execute("TRUNCATE TABLE transport." + tableName + " CASCADE");
        }
    }
}
