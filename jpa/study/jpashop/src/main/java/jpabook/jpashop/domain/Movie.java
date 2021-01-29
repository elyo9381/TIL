package jpabook.jpashop.domain;

import javax.persistence.Entity;

@Entity
public class Movie extends Item {



    private String direction;
    private String actor;

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }
}