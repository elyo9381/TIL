package me.elyowon.Middle.javaUtil.exam;

import java.lang.reflect.Method;
/*
*   1. 애노테이션 만드는 순서 @애노테이션 클래스를 정의
*   2. 애노테이션이 붙은 클래스 Myhello를 정의
*   3. MyhelloExam클래스에서 Count100이라는 애노테이션을 탐색하고
*   4. 애노테이션이 존재하면 hello.hello()메소드를 출력하는 순서이다.
*   이를 응용하여 나중에 더욱 많이 사용할수있다.
* */

public class MyhelloExam {
    public static void main(String[] args) {
        Myhello hello = new Myhello();
        try {
            Method method = hello.getClass().getDeclaredMethod("hello");
            if(method.isAnnotationPresent(Count100.class)){
                for(int i = 0; i<100; i++){
                    hello.hello();
                }
            } else {
                    hello.hello();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}