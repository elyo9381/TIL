package com.yowon.psc.case001.reservation.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tb_reserv", indexes = {
    @Index(name = "idx_tb_reserv_agno_id", columnList = "agno,id")
})
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long agno;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReservationTicketEntity> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReservationItemEntity> items = new ArrayList<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<PaymentEntity> pays = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAgno() {
        return agno;
    }

    public void setAgno(Long agno) {
        this.agno = agno;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ReservationTicketEntity> getTickets() {
        return tickets;
    }

    public void setTickets(List<ReservationTicketEntity> tickets) {
        this.tickets = tickets;
    }

    public List<ReservationItemEntity> getItems() {
        return items;
    }

    public void setItems(List<ReservationItemEntity> items) {
        this.items = items;
    }

    public List<PaymentEntity> getPays() {
        return pays;
    }

    public void setPays(List<PaymentEntity> pays) {
        this.pays = pays;
    }
}
