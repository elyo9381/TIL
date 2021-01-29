package jpabook.jpashop.domain;

import javax.persistence.Entity;

@Entity
public class Book extends Item{

    private String author;
    private String ispn;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIspn() {
        return ispn;
    }

    public void setIspn(String ispn) {
        this.ispn = ispn;
    }
}