package me.elyowon.Middle.javaUtil.exam;

import java.util.Calendar;

public class CalendarExam {
    public static void main(String[] args) {
        // 추상클래스는 new~~ 사용하지 못한다.
        // gregorianCalendar를 가져오는데
        // 나중에 새로운달력이 만들어질수도있으므로 Calendar를 추상클래스로 만듬

        Calendar cal = Calendar.getInstance();
        System.out.println(cal.get(Calendar.YEAR));
        System.out.println(cal.get(Calendar.MONTH)+1);
        System.out.println(cal.get(Calendar.DATE));

        //12
        System.out.println(cal.get(Calendar.HOUR));
        //24
        System.out.println(cal.get(Calendar.HOUR_OF_DAY));

    }
}