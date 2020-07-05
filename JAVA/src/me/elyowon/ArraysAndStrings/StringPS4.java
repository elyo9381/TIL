package me.elyowon.ArraysAndStrings;

/*
*   단한번만 편집된 문자열인지 알아내는 문제
*   insert, remove, replace등에서 한번만 편집되었는지 알아야한다.
*   insert와 remove는 같은 논리로 해결할수있다.
*   replace는 하나만 달라야 하므로 flag를 설정하여 해결한다.
* */
public class StringPS4 {
    public static void main(String[] args) {
        System.out.println(isOneAway("paie","pai"));
        System.out.println(isOneAway("pai","paie"));
        System.out.println(isOneAway("pvie","paia"));
    }
    private static boolean isOneAway(String s1, String s2){
        String ss, ls;
        if(s1.length() < s2.length()){
            ss = s1;
            ls = s2;
        } else {
            ss = s2;
            ls = s1;
        }
        if(ls.length() - ss.length() > 1) return false;
        boolean flag = false;
        for (int i = 0, j =0; i<ss.length(); i++, j++){
            if(ss.charAt(i) != ls.charAt(j)){
                if(flag){
                    return false;
                }
                flag = true;
                if(ss.length() != ls.length()){
                    j++;
                }
            }
        }
        return true;
    }
}
