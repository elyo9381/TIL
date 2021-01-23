package jpabook.jpashop.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class Order {

    @Id @GeneratedValue()
    private Long id;

    private Long memberId;
    private LocalDateTime orderDate;
    private OrderStatus status;
}