package com.yowon.psc.case002.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowon.psc.case002.reservation.application.dto.AdminReservSearchCondition;
import com.yowon.psc.case002.reservation.infra.mapper.AdminReservListRow;
import com.yowon.psc.case002.reservation.infra.mapper.ReservQueryMapper;
import com.yowon.psc.case002.support.MySqlIntegrationTestSupport;
import com.yowon.psc.common.metrics.SqlMeasuredExecution;
import com.yowon.psc.common.metrics.SqlMetricsRunner;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class Case002ReservQueryMapperIntegrationTest extends MySqlIntegrationTestSupport {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservQueryMapper reservQueryMapper;

    @Autowired
    private SqlMetricsRunner sqlMetricsRunner;

    @BeforeEach
    void setUpSchemaAndData() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS tb_pay");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tb_ticket_route");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tb_reserv_ticket");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tb_ticket_lang");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tb_ticket");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tb_reserv");

        jdbcTemplate.execute("CREATE TABLE tb_reserv (rvno BIGINT PRIMARY KEY, agno BIGINT NOT NULL, tkno BIGINT NOT NULL, status VARCHAR(40), regdt DATETIME NOT NULL)");
        jdbcTemplate.execute("CREATE TABLE tb_ticket (tkno BIGINT PRIMARY KEY, name VARCHAR(120) NOT NULL)");
        jdbcTemplate.execute("CREATE TABLE tb_ticket_lang (tkno BIGINT NOT NULL, locale VARCHAR(20) NOT NULL, name VARCHAR(120) NOT NULL)");
        jdbcTemplate.execute("CREATE TABLE tb_reserv_ticket (reno BIGINT AUTO_INCREMENT PRIMARY KEY, rvno BIGINT NOT NULL, runo INT NOT NULL, rsno INT NULL, origin JSON NULL, dest JSON NULL, ticket JSON NULL)");
        jdbcTemplate.execute("CREATE TABLE tb_ticket_route (tkno BIGINT NOT NULL, runo INT NOT NULL)");
        jdbcTemplate.execute("CREATE TABLE tb_pay (pyno BIGINT AUTO_INCREMENT PRIMARY KEY, rvno BIGINT NOT NULL, price DECIMAL(18,2) NOT NULL)");

        jdbcTemplate.update("INSERT INTO tb_ticket(tkno, name) VALUES (?, ?)", 100L, "City Tour");
        jdbcTemplate.update("INSERT INTO tb_ticket_lang(tkno, locale, name) VALUES (?, ?, ?)", 100L, "ko", "시티 투어");

        jdbcTemplate.update("INSERT INTO tb_reserv(rvno, agno, tkno, status, regdt) VALUES (?, ?, ?, ?, ?)", 1L, 10L, 100L, "PAY_SUCCESS", "2026-02-28 12:00:00");
        jdbcTemplate.update("INSERT INTO tb_reserv(rvno, agno, tkno, status, regdt) VALUES (?, ?, ?, ?, ?)", 2L, 10L, 100L, "PAY_SUCCESS", "2026-02-28 13:00:00");

        jdbcTemplate.update("INSERT INTO tb_reserv_ticket(rvno, runo, rsno, origin, dest, ticket) VALUES (?, ?, ?, CAST(? AS JSON), CAST(? AS JSON), CAST(? AS JSON))",
            1L, 11, 101, "{\"stno\":1}", "{\"stno\":2}", "{\"ticket\":2}");
        jdbcTemplate.update("INSERT INTO tb_reserv_ticket(rvno, runo, rsno, origin, dest, ticket) VALUES (?, ?, ?, CAST(? AS JSON), CAST(? AS JSON), CAST(? AS JSON))",
            2L, 11, 101, "{\"stno\":3}", "{\"stno\":4}", "{\"ticket\":1}");

        jdbcTemplate.update("INSERT INTO tb_ticket_route(tkno, runo) VALUES (?, ?)", 100L, 11);
        jdbcTemplate.update("INSERT INTO tb_ticket_route(tkno, runo) VALUES (?, ?)", 100L, 11);

        jdbcTemplate.update("INSERT INTO tb_pay(rvno, price) VALUES (?, ?)", 1L, new BigDecimal("10000"));
        jdbcTemplate.update("INSERT INTO tb_pay(rvno, price) VALUES (?, ?)", 1L, new BigDecimal("5000"));
        jdbcTemplate.update("INSERT INTO tb_pay(rvno, price) VALUES (?, ?)", 2L, new BigDecimal("7000"));
    }

    @Test
    void mapperShouldReturnOneRowPerReservationEvenWithMultiplePays() {
        List<AdminReservListRow> rows = reservQueryMapper.findReservList(new AdminReservSearchCondition(10L, 0, 20, "ko"));

        assertThat(rows).hasSize(2);

        Map<Long, AdminReservListRow> rowsById = rows.stream()
            .collect(Collectors.toMap(AdminReservListRow::getRvno, Function.identity()));

        assertThat(rowsById.get(1L).getPayPrice()).isEqualByComparingTo("15000");
        assertThat(rowsById.get(2L).getPayPrice()).isEqualByComparingTo("7000");
        assertThat(rowsById.get(1L).getItemDTOsJson()).contains("runo");
    }

    @Test
    void mapperShouldExposeActualSqlCount() {
        SqlMeasuredExecution<List<AdminReservListRow>> measured = sqlMetricsRunner.measure(
            () -> reservQueryMapper.findReservList(new AdminReservSearchCondition(10L, 0, 20, "ko"))
        );

        assertThat(measured.getResult()).hasSize(2);
        assertThat(measured.getSqlCount()).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void itemJsonShouldNotDuplicateWhenTicketRouteHasDuplicateRows() throws Exception {
        List<AdminReservListRow> rows = reservQueryMapper.findReservList(new AdminReservSearchCondition(10L, 0, 20, "ko"));

        Map<Long, AdminReservListRow> rowsById = rows.stream()
            .collect(Collectors.toMap(AdminReservListRow::getRvno, Function.identity()));

        JsonNode itemsForReservation1 = objectMapper.readTree(rowsById.get(1L).getItemDTOsJson());
        JsonNode itemsForReservation2 = objectMapper.readTree(rowsById.get(2L).getItemDTOsJson());

        assertThat(itemsForReservation1.isArray()).isTrue();
        assertThat(itemsForReservation2.isArray()).isTrue();
        assertThat(itemsForReservation1.size()).isEqualTo(1);
        assertThat(itemsForReservation2.size()).isEqualTo(1);
    }
}
