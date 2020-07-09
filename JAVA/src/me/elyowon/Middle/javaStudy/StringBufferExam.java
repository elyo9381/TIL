package me.elyowon.Middle.javaStudy;

/*
*   StringBuffer는 메소드 체이닝
*   : 자기자신을 리턴하여 계속해서 자신의 메소드를 호출하는 방식
*
*
* */
public class StringBufferExam {

    public static void main(String[] args) {
        StringBuffer sb = new StringBuffer();

        //문자열 추가하는 메소드 .append();
        sb.append("hello");
        sb.append(" ");
        sb.append("world");


        String str = sb.toString();
        System.out.println(str);

        // StringBuffer는 자기자신을 리턴한다.
        // 그렇기 때문에 sb2.append를 하였을때 자신의 값을 sb3에
        // 대입한것이다.
        StringBuffer sb2 = new StringBuffer();
        StringBuffer sb3 = sb2.append("hellworld");
        if(sb2 == sb3){
            System.out.println("sb2==sb3");
        }

        String str2 = new StringBuffer().append("hello").append("  ").append("world !!!").toString();
        System.out.println(str2);
    }
}