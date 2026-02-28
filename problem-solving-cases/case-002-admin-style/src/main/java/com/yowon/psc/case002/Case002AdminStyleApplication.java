package com.yowon.psc.case002;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yowon.psc.case002.reservation.infra.mapper")
public class Case002AdminStyleApplication {

    public static void main(String[] args) {
        SpringApplication.run(Case002AdminStyleApplication.class, args);
    }
}
