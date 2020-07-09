package me.elyowon.Middle.javaUtil.exam;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MapExam {
    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("001","kim");
        map.put("002","won");
        map.put("003","choi");

        map.put("001","kang");

        System.out.println(map.size());

        Set<String> keys = map.keySet();

        Iterator<String> itor = keys.iterator();

        while(itor.hasNext()){
            String KEY = itor.next();
            String VALUE = map.get(KEY);
            System.out.println(KEY +":"+VALUE);
        }
    }

}