package me.elyowon.ArraysAndStrings;

// 두개의 문자열이 서로 치환인지 알아내는 알고리즘
// 1번째 방법은 두개의 string을 비교하는데 정렬한 값을 equals메소드를 통해서 비교하여 리턴하는경우
// 2번째 경우는 letter[s.string] 의 인텍스별 카운트를 세고 letter[t.string] 동일한 내용의 카운트를 줄여서
// 이때 letter[]이  0 보다 작으면 없는 상태에서 내려간것으로 두 스트링이 같지 않는 경우이다.
// 그러므로 false를 한다.
public class StringPermutation {
    public static void main(String[] args) {
        System.out.println(permutation("abc","bca"));
        System.out.println(permutation("abc","bda"));
    }
    private static String sort(String s){
        char[] content = s.toCharArray();
        java.util.Arrays.sort(content);
        return new String(content);
    }
    private static boolean permutation(String s,String t){
         if(s.length() != t.length()) return false;
//         return sort(s).equals(sort(t));
        int[] letters = new int[128];
        for(int i = 0; i<s.length(); i++){


            letters[s.charAt(i)]++;
        }
        for(int i = 0; i<t.length(); i++){
            letters[t.charAt(i)]--;
            if(letters[t.charAt(i)] < 0) return false;
        }
        return true;
    }
}
