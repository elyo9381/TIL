package me.elyowon.collectionExam;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashMapExam {

    public static void main(String[] args) {

        HashMap<String,Integer> map = new HashMap<String,Integer>();

        map.put("string",1);
        map.put("integer",2);
        map.put("boolean",3);

        System.out.println(map.get("string"));
        System.out.println(map.get("integer"));
        System.out.println(map.get("boolean"));


        iteratorUsingForEach(map);


    }

    // 생략가능 <String, Integer>
     /*Map에는 entrySet이라는 메소드가 존재한다. 이것은 map타입의 데이터를 set에 넣을
     * 수있도록 하는 메소드이다.
     * 즉 쉽게 말하면 map형식의 데이터  {"string",1} 이것이 set의 첫번째 인자로 들어가게
     * 되는것이다. c++의 페어와 비슷한개념으로 등록될수있다.     *
     * */
    static void iteratorUsingForEach(HashMap<String, Integer> map) {
        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        for(Map.Entry<String,Integer> entry : entries){
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

}