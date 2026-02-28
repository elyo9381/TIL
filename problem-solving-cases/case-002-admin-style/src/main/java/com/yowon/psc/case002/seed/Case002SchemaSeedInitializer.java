package com.yowon.psc.case002.seed;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Case002SchemaSeedInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.schema.auto-create:true}")
    private boolean autoCreateSchema;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.agno:10}")
    private long agno;

    @Value("${app.seed.reservation-count:20}")
    private int reservationCount;

    public Case002SchemaSeedInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        if (autoCreateSchema) {
            createSchemaIfMissing();
        }
        validateSchemaCompatibility();

        if (!seedEnabled || hasReservations(agno)) {
            return;
        }
        seedData(agno, Math.max(1, reservationCount));
    }

    private void createSchemaIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tb_reserv (" +
            "rvno BIGINT PRIMARY KEY, " +
            "agno BIGINT NOT NULL, " +
            "tkno BIGINT NOT NULL, " +
            "status VARCHAR(40), " +
            "regdt DATETIME NOT NULL, " +
            "INDEX idx_tb_reserv_agno_regdt (agno, regdt)" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tb_ticket (" +
            "tkno BIGINT PRIMARY KEY, " +
            "name VARCHAR(120) NOT NULL" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tb_ticket_lang (" +
            "tkno BIGINT NOT NULL, " +
            "locale VARCHAR(20) NOT NULL, " +
            "name VARCHAR(120) NOT NULL, " +
            "INDEX idx_tb_ticket_lang_tkno_locale (tkno, locale)" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tb_reserv_ticket (" +
            "reno BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "rvno BIGINT NOT NULL, " +
            "runo INT NOT NULL, " +
            "rsno INT NULL, " +
            "origin JSON NULL, " +
            "dest JSON NULL, " +
            "ticket JSON NULL, " +
            "INDEX idx_tb_reserv_ticket_rvno (rvno)" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tb_ticket_route (" +
            "tkno BIGINT NOT NULL, " +
            "runo INT NOT NULL, " +
            "INDEX idx_tb_ticket_route_tkno_runo (tkno, runo)" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tb_pay (" +
            "pyno BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "rvno BIGINT NOT NULL, " +
            "price DECIMAL(18,2) NOT NULL, " +
            "INDEX idx_tb_pay_rvno (rvno)" +
            ")");
    }

    private void validateSchemaCompatibility() {
        assertColumns("tb_reserv", "rvno", "agno", "tkno", "status", "regdt");
        assertColumns("tb_ticket", "tkno", "name");
        assertColumns("tb_ticket_lang", "tkno", "locale", "name");
        assertColumns("tb_reserv_ticket", "rvno", "runo", "rsno", "origin", "dest", "ticket");
        assertColumns("tb_ticket_route", "tkno", "runo");
        assertColumns("tb_pay", "rvno", "price");
    }

    private void assertColumns(String tableName, String... columns) {
        for (String column : columns) {
            Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                tableName,
                column
            );
            if (exists == null || exists == 0) {
                throw new IllegalStateException(
                    "case-002 admin schema mismatch: required column not found - " +
                        tableName + "." + column +
                        ". Use a dedicated DB (MYSQL_DATABASE_CASE002)."
                );
            }
        }
    }

    private boolean hasReservations(long targetAgno) {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM tb_reserv WHERE agno = ?",
            Long.class,
            targetAgno
        );
        return count != null && count > 0;
    }

    private void seedData(long targetAgno, int count) {
        jdbcTemplate.update("INSERT IGNORE INTO tb_ticket(tkno, name) VALUES (?, ?)", 100L, "City Tour");
        jdbcTemplate.update("INSERT IGNORE INTO tb_ticket_lang(tkno, locale, name) VALUES (?, ?, ?)", 100L, "ko", "시티 투어");
        jdbcTemplate.update("INSERT IGNORE INTO tb_ticket_route(tkno, runo) VALUES (?, ?)", 100L, 11);

        LocalDateTime base = LocalDateTime.of(2026, 2, 28, 14, 0, 0);
        for (int i = 0; i < count; i++) {
            long rvno = 1000L + i;
            LocalDateTime regdt = base.minusMinutes(i);

            jdbcTemplate.update(
                "INSERT INTO tb_reserv(rvno, agno, tkno, status, regdt) VALUES (?, ?, ?, ?, ?)",
                rvno,
                targetAgno,
                100L,
                "PAY_SUCCESS",
                regdt
            );

            jdbcTemplate.update(
                "INSERT INTO tb_reserv_ticket(rvno, runo, rsno, origin, dest, ticket) " +
                    "VALUES (?, ?, ?, CAST(? AS JSON), CAST(? AS JSON), CAST(? AS JSON))",
                rvno,
                11,
                101,
                "{\"stno\":1}",
                "{\"stno\":2}",
                "{\"ticket\":1}"
            );

            jdbcTemplate.update("INSERT INTO tb_pay(rvno, price) VALUES (?, ?)", rvno, 8000 + (i * 100));
            if ((i % 2) == 0) {
                jdbcTemplate.update("INSERT INTO tb_pay(rvno, price) VALUES (?, ?)", rvno, 2000);
            }
        }
    }
}
