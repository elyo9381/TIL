package com.yowon.psc.case003.repro.infra;

import com.yowon.psc.case003.repro.domain.ExplainRow;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JoinHintReproJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public JoinHintReproJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ExplainRow> explainWithoutHint(String serviceType, int limit) {
        return explain(buildWithoutHintSql(), serviceType, limit);
    }

    public List<ExplainRow> explainWithHint(String serviceType, int limit) {
        return explain(buildWithHintSql(), serviceType, limit);
    }

    public List<Long> findIdsWithoutHint(String serviceType, int limit) {
        return jdbcTemplate.queryForList(buildWithoutHintSql(), Long.class, serviceType, limit);
    }

    public List<Long> findIdsWithHint(String serviceType, int limit) {
        return jdbcTemplate.queryForList(buildWithHintSql(), Long.class, serviceType, limit);
    }

    public String explainJsonWithoutHint(String serviceType, int limit) {
        return jdbcTemplate.queryForObject(
            "EXPLAIN FORMAT=JSON " + buildWithoutHintSql(),
            String.class,
            serviceType,
            limit
        );
    }

    public String explainJsonWithHint(String serviceType, int limit) {
        return jdbcTemplate.queryForObject(
            "EXPLAIN FORMAT=JSON " + buildWithHintSql(),
            String.class,
            serviceType,
            limit
        );
    }

    private List<ExplainRow> explain(String sql, String serviceType, int limit) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("EXPLAIN " + sql, serviceType, limit);
        List<ExplainRow> explainRows = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            explainRows.add(new ExplainRow(
                asLong(row.get("id")),
                asString(row.get("select_type")),
                asString(row.get("table")),
                asString(row.get("type")),
                asString(row.get("key")),
                asLong(row.get("rows")),
                asString(row.get("Extra"))
            ));
        }
        return explainRows;
    }

    private String buildWithoutHintSql() {
        return "WITH ticket AS ( " +
            "SELECT t.tkno, tr.runo, ru.service_type " +
            "FROM tb_ticket t " +
            "LEFT JOIN tb_ticket_route tr ON t.tkno = tr.tkno " +
            "LEFT JOIN tb_route ru ON tr.runo = ru.runo " +
            ") " +
            "SELECT r.rvno " +
            "FROM tb_reserv r " +
            "LEFT JOIN ticket ON r.tkno = ticket.tkno " +
            "WHERE ticket.service_type = ? " +
            "ORDER BY r.regdt DESC " +
            "LIMIT ?";
    }

    private String buildWithHintSql() {
        return "SELECT STRAIGHT_JOIN r.rvno " +
            "FROM tb_reserv r FORCE INDEX (idx_reserv_regdt) " +
            "LEFT JOIN tb_ticket t ON r.tkno = t.tkno " +
            "LEFT JOIN tb_ticket_route tr ON t.tkno = tr.tkno " +
            "LEFT JOIN tb_route ru ON tr.runo = ru.runo " +
            "WHERE ru.service_type = ? " +
            "ORDER BY r.regdt DESC " +
            "LIMIT ?";
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
