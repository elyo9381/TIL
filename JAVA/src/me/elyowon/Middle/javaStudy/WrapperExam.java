package me.elyowon.Middle.javaStudy;

/*
*   .wrapper라는 클래스가 존재한다.
*   wrapper class : 기본형 데이터 타입의 객체화를 가능하게 도와주는 클래스들
*
*   Object, String, StringBuffer, StringBuilder, System, Math등이
*   javalang패키지에 존재한다.
*
* */


public class WrapperExam {
    public static void main(String[] args) {
        int i = 5;
        Integer i2 = new Integer(5);

// 원래는 인티저를 통한 객체로 만들어서 넣어줘야하는데
// 오토박싱이 가능하다.
// what? 오토박싱? : 기본타입 데이터를 객체 타입의 데이터로 자동형변화 시켜주는 기능
        Integer i3 = 5;

// 원래 자바는 integer객체는 객체를 한번 벗겨주고
// 기본타입에 넣을수있었는데
        int i4 = i3.intValue();

// 최신 자바 버전은 자바컴파일러가 알아서 벗겨서 자동형변화 하여 넣어준다.
// 이것을 오토언박싱이라고 한다.
        int i5 = i3;
    }

}