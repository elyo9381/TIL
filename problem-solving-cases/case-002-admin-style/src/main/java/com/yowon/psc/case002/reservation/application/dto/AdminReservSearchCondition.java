package com.yowon.psc.case002.reservation.application.dto;

public class AdminReservSearchCondition {

    private final Long agno;
    private final int offset;
    private final int limit;
    private final String locale;

    public AdminReservSearchCondition(Long agno, int offset, int limit, String locale) {
        this.agno = agno;
        this.offset = offset;
        this.limit = limit;
        this.locale = locale;
    }

    public Long getAgno() {
        return agno;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public String getLocale() {
        return locale;
    }
}
