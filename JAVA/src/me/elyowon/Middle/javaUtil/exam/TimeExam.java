package me.elyowon.Middle.javaUtil.exam;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TimeExam {

    public static void main(String[] args) {
        LocalDateTime timepoint = LocalDateTime.now();
        System.out.println(timepoint);

        LocalDate ld1 = LocalDate.of(2012, 12 ,12);
        System.out.println(ld1);

        System.out.println(timepoint.toLocalTime());
    }

}