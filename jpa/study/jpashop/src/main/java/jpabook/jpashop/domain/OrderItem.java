package jpabook.jpashop.domain;

import javax.persistence.Entity;

@Entity
public class OrderItem {

    private Long id;
    private Long orderId;
    private Long itemId;
    private int orderPrice;
    private int count;
}