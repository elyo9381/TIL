package me.elyowon.ArraysAndStrings;

//IsSubString이라는 함수가 존재하고 이를 한번 사용하여
// s1,s2를 비교하는 s2가 substring함수 한번만 사용하여 변경될수있는지를 검사하는 문제

// 접근방법 문자열 sort후 문자열 비교
// count정렬을 통해 비교

// 다른접근은 바뀌문자열 s2를 한번더 더해주면 같은 문자열이 생성될것이고
// 이를 통해서 s1의 문자열이 생성된것을 알수있다.

//stringstring //ringst
public class IsSubStringPS {
    public static void main(String[] args) {
        System.out.println(isRotation("string","ringst"));
        System.out.println(isRotation("string","rinsgt"));
    }
    private static boolean isRotation(String s1, String s2){
        if(s1.length() != s2.length()) return false;
        return isSubstring(s1+s1, s2);
    }
    // contains(str)은 내부적으로 indexOf(str)함수가 들어가있다.
    // indexOf 문자열의 같은지를 판별하고 인덱스를 반환한다.
    private static boolean isSubstring(String s1, String s2){
        System.out.println("output "+(s1.indexOf(s2)));
        return s1.contains(s2);
    }
}
