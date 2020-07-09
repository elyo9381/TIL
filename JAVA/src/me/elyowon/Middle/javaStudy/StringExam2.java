package me.elyowon.Middle.javaStudy;

public class StringExam2 {

    public static void main(String[] args) {
        String str1 = "hello world";
        String str2 = str1.substring(5);
        System.out.println(str1);
        System.out.println(str2);

        String str3 = str1 + str2;
        System.out.println(str3);

        //실제로 문자열 더해지는 과정
        String str4 = new StringBuffer().append(str1).append(str2).toString();
        System.out.println(str4);

        String str5 ="";
        for(int i = 0; i< 100; i++){
            str5  = str5 + "*";
        }
        System.out.println(str5);

//        --------------------------------------------
        //위와같은 코드는 내부적으로 new가 계속 생성되어 매우 느린코드가 된다.
        // 그렇기 때문에  new는 되도록 한번만 사용하는것이좋다.
        // stringBuffer를 이용해도 좋지만 StringBuilder를 이용해도 된다.
        StringBuffer sb = new StringBuffer();
        for(int  i = 0; i< 100; i++){
            sb.append("*");
        }
        String str6 =  sb.toString();
        System.out.println(str6);
    }

}