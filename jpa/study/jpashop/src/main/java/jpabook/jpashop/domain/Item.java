package jpabook.jpashop.domain;

import javax.persistence.Entity;

@Entity
public class Item {

    private Long id;
    private String name;
    private int price;
    private int stockQuntity;

}