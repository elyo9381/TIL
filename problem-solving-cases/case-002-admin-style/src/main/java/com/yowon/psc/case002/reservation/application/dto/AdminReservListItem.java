package com.yowon.psc.case002.reservation.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminReservListItem {

    private final Long rvno;
    private final LocalDateTime regdt;
    private final String status;
    private final String ticketName;
    private final String itemDTOsJson;
    private final BigDecimal payPrice;

    public AdminReservListItem(
        Long rvno,
        LocalDateTime regdt,
        String status,
        String ticketName,
        String itemDTOsJson,
        BigDecimal payPrice
    ) {
        this.rvno = rvno;
        this.regdt = regdt;
        this.status = status;
        this.ticketName = ticketName;
        this.itemDTOsJson = itemDTOsJson;
        this.payPrice = payPrice;
    }

    public Long getRvno() {
        return rvno;
    }

    public LocalDateTime getRegdt() {
        return regdt;
    }

    public String getStatus() {
        return status;
    }

    public String getTicketName() {
        return ticketName;
    }

    public String getItemDTOsJson() {
        return itemDTOsJson;
    }

    public BigDecimal getPayPrice() {
        return payPrice;
    }
}
