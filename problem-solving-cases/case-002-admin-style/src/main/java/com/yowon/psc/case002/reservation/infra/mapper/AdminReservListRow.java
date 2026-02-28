package com.yowon.psc.case002.reservation.infra.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AdminReservListRow {

    private Long rvno;
    private LocalDateTime regdt;
    private String status;
    private String ticketName;
    private String itemDTOsJson;
    private BigDecimal payPrice;

    public AdminReservListRow() {
    }

    public AdminReservListRow(
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

    public void setRvno(Long rvno) {
        this.rvno = rvno;
    }

    public LocalDateTime getRegdt() {
        return regdt;
    }

    public void setRegdt(LocalDateTime regdt) {
        this.regdt = regdt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTicketName() {
        return ticketName;
    }

    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    public String getItemDTOsJson() {
        return itemDTOsJson;
    }

    public void setItemDTOsJson(String itemDTOsJson) {
        this.itemDTOsJson = itemDTOsJson;
    }

    public BigDecimal getPayPrice() {
        return payPrice;
    }

    public void setPayPrice(BigDecimal payPrice) {
        this.payPrice = payPrice;
    }
}
