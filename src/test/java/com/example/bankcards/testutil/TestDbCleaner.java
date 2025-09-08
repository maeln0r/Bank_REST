package com.example.bankcards.testutil;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestDbCleaner {
    private final JdbcTemplate jdbc;

    public TestDbCleaner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Полностью очищает все таблицы в схеме public, кроме служебных Liquibase.
     * TRUNCATE ... RESTART IDENTITY CASCADE — сбрасывает автоинкременты и учитывает FK.
     */
    public void truncateAll() {
        List<String> tables = jdbc.queryForList(
                "select table_name from information_schema.tables " +
                        "where table_schema='public' and table_type='BASE TABLE' " +
                        "and table_name not in ('databasechangelog','databasechangeloglock')",
                String.class);
        if (tables.isEmpty()) return;
        String joined = tables.stream()
                .map(t -> "public." + t)
                .collect(Collectors.joining(", "));
        jdbc.execute("TRUNCATE TABLE " + joined + " RESTART IDENTITY CASCADE");
    }
}