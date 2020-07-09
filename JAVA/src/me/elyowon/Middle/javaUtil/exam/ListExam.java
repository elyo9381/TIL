package me.elyowon.Middle.javaUtil.exam;

import java.util.ArrayList;
import java.util.List;

public class ListExam {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("kim");
        list.add("won");
        list.add("kim");

        System.out.println(list.size());

        for(int i = 0; i<list.size(); i++){
            String str = list.get(i);
            System.out.println(str);
        }
    }

}