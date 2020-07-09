package me.elyowon.Middle.javaUtil.exam;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class setExam {
    public static void main(String[] args) {
        // set은 interface이므로 구현체를 인스턴스로 받아야한다.
        // 그렇시 때문에 hashSet을 받은것이다.
        Set<String> set1 = new HashSet<>();
        boolean flag = set1.add("won");
        boolean flag2 = set1.add("Lee");
        boolean flag3 = set1.add("won");

        System.out.println(set1.size());

        //set은 중복을 허용하지 않는다.
        System.out.println(flag);
        System.out.println(flag2);
        System.out.println(flag3);

        //iterator는 인터페이스이므로 구현체를 인스턴스로 생성해야한다.
        // 그렇기 때문에 set1의 객체의 iterator()메소드를 불러온것이다.
        // 그리고 set1의 내용을 확인하기 위해서이므로
        Iterator<String> iter = set1.iterator();
        while(iter.hasNext()){
            String str = iter.next();
            System.out.println(str);
        }
    }

}