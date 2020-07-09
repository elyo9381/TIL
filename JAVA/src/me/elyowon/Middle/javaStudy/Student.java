package me.elyowon.Middle.javaStudy;


/*
*   equals : 객체가 가진 값을 비교할 떄 사용
*   toString : 객체가 가진 값을 문자열로 반환
*   hashCode : 객체의 해시코드 값을 반환
*
*   Object가 가지고 있는 메소드를 알아보자
*   객체를 비교하기 위해서는 override를 통해서 메소드를 만들어준뒤
*   비교를 하거나 해시코드를 만들거나 toString할수있다.
*
*
* */


import java.util.Objects;

public class Student {
        String name;
        String number;
        int birthYear;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student middle_1 = (Student) o;
        return Objects.equals(number, middle_1.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return "Student {" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", birthYear=" + birthYear +
                '}';
    }

    public static void main(String[] args) {
        Student s1 = new Student();
        s1.name = "홍길동";
        s1.number = "1234";
        s1.birthYear = 1995;

        Student s2 = new Student();
        s2.name = "홍길동";
        s2.number = "1234";
        s2.birthYear = 1995;

        if(s1.equals(s2))
            System.out.println("s1 == s2");
        else
            System.out.println("s1 != s2");

        System.out.println(s1.hashCode());
        System.out.println(s2.hashCode());
        System.out.println(s1.toString());
    }
}