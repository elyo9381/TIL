package com.yowon.psc.case003.seed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Case003SchemaSeedInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.schema.auto-create:true}")
    private boolean autoCreateSchema;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.reservation-count:30000}")
    private int reservationCount;

    public Case003SchemaSeedInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        if (autoCreateSchema) {
            createSchemaIfMissing();
        }
        if (!seedEnabled || hasEnoughData()) {
            return;
        }
        seedData(Math.max(5000, reservationCount));
    }

    private void createSchemaIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tb_reserv (" +
            "rvno BIGINT PRIMARY KEY, " +
            "agno BIGINT NOT NULL, " +
            "tkno BIGINT NOT NULL, " +
            "regdt DATETIME NOT NULL, " +
            "status VARCHAR(30), " +
            "INDEX idx_reserv_agno_regdt (agno, regdt), " +
            "INDEX idx_reserv_tkno (tkno), " +
            "INDEX idx_reserv_regdt (regdt)" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tb_ticket (" +
            "tkno BIGINT PRIMARY KEY, " +
            "name VARCHAR(120) NOT NULL" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tb_ticket_route (" +
            "tkno BIGINT NOT NULL, " +
            "runo INT NOT NULL, " +
            "INDEX idx_tr_tkno (tkno), " +
            "INDEX idx_tr_runo (runo)" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tb_route (" +
            "runo INT PRIMARY KEY, " +
            "service_type VARCHAR(30) NOT NULL, " +
            "INDEX idx_route_service_type (service_type)" +
            ")");
    }

    private boolean hasEnoughData() {
        Long reservCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_reserv", Long.class);
        Long routeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_route", Long.class);
        Long ticketRouteCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tb_ticket_route", Long.class);
        return reservCount != null && reservCount >= 5000
            && routeCount != null && routeCount >= 1000
            && ticketRouteCount != null && ticketRouteCount >= 22000;
    }

    private void seedData(int totalReservations) {
        jdbcTemplate.execute("DELETE FROM tb_ticket_route");
        jdbcTemplate.execute("DELETE FROM tb_reserv");
        jdbcTemplate.execute("DELETE FROM tb_route");
        jdbcTemplate.execute("DELETE FROM tb_ticket");
        jdbcTemplate.execute("SET SESSION cte_max_recursion_depth = 40000");

        jdbcTemplate.execute(
            "INSERT INTO tb_ticket (tkno, name) " +
                "WITH RECURSIVE seq(n) AS ( " +
                    "SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 100 " +
                ") " +
                "SELECT n, CONCAT('ticket-', n) FROM seq"
        );

        jdbcTemplate.execute(
            "INSERT INTO tb_route (runo, service_type) " +
                "WITH RECURSIVE seq(n) AS ( " +
                    "SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 1000 " +
                ") " +
                "SELECT n, CASE " +
                    "WHEN n <= 900 THEN 'GENERAL' " +
                    "WHEN n <= 980 THEN 'PREMIUM' " +
                    "ELSE 'VIP' END " +
                "FROM seq"
        );

        jdbcTemplate.execute(
            "INSERT INTO tb_ticket_route (tkno, runo) " +
                "SELECT t.tkno, r.runo " +
                "FROM tb_ticket t " +
                "JOIN tb_route r ON (r.runo <= 200 OR r.runo > 980)"
        );

        jdbcTemplate.execute(
            "INSERT INTO tb_reserv (rvno, agno, tkno, regdt, status) " +
                "WITH RECURSIVE seq(n) AS ( " +
                    "SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < " + totalReservations +
                ") " +
                "SELECT n, " +
                    "CASE WHEN n <= 120 THEN 10 ELSE 20 END, " +
                    "(n % 100) + 1, " +
                    "TIMESTAMP('2026-02-01 00:00:00') + INTERVAL n SECOND, " +
                    "'PAY_SUCCESS' " +
                "FROM seq"
        );

        jdbcTemplate.execute("ANALYZE TABLE tb_reserv, tb_ticket, tb_ticket_route, tb_route");
    }
}
