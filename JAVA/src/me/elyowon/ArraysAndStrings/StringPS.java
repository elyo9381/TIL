package me.elyowon.ArraysAndStrings;

import java.util.HashMap;

// 문자열 안의 문자들이 고유한지 확인하
public class StringPS {

    public static void main(String[] args) {
        System.out.println(isUniqueAsc("abcdefgghijk"));
        System.out.println(isUniqueAsc("abcdefghijk"));
        System.out.println(isUniqueUni("abcdefgghijk"));
        System.out.println(isUniqueUni("abcdefghijk"));
    }

    // ascii 일때
    private static boolean isUniqueAsc(String str){
        if(str.length() > 128) return false;
        boolean[] char_set = new boolean[128];
        for(int i=0; i< str.length(); i++){
            int val = str.charAt(i);
            if(char_set[val]){
                return false;
            }
            else char_set[val] = true;
        }
        return true;
    }
    //unicode 일때 hash map 사용
    private static boolean isUniqueUni(String str){
        HashMap<Integer, Boolean> hashmap = new HashMap<Integer,Boolean>();
        for(int i = 0; i< str.length(); i++){
            int c = str.charAt(i);
            if(hashmap.containsKey(c)){
                return false;
            }
            hashmap.put(c, true);
        }
        return true;
    }
    // 이진수 사용
    private static boolean isUnique(String str){
        int checker = 0;
        for(int i = 0; i<str.length(); i++){
            int val =  str.charAt(i) - 'a';
            if((checker & (1<< val)) >0){
                return false;
            }
            checker |= (1<<val);
        }
        return true;
    }
    // 나중에 저장공간 없이 하는 방법은 이중포문 방법 그리고 큌소드를 2번 돌리는 방법등이 있다.

}
