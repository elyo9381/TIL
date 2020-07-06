package me.elyowon.ArraysAndStrings;

/*
*   str + "" 연산을 통해서 문자열을 한자한자 붙이는것은 매우 비효율적인 연산이다.
*   그렇기 때문에 StringBuilder를 통해서 배열의 사이즈를 생성자를 통해서 입력받고
*   배열을 미리 할달하여 append하는것이 바람직한 방법이고 연산을 줄일수가있다.
*
*   입력된 str을 compressString 메소드에서 compress메소드를 통해 str을 카운트하면서
*   append를 통해서 문자열을 붙인다. stringbuilder의 생성자를 통해서 초기에 사이즈를 리턴해여
*   불필요한 더블링을 막는다.
*
*
* */

public class StringCompression {
    public static void main(String[] args) {
        System.out.println(compressString("aaabbbbbbbbbbbbbbbcccdd"));
        System.out.println(compressString("abcd"));
    }
    private static String compressString(String str){
        String newStr = compress(str);
        return str.length() < newStr.length()? str : newStr;
    }
    private static String compress(String str){
        int count = 0;
        java.lang.StringBuilder newString = new java.lang.StringBuilder(getTotal(str));
        for(int i =0; i< str.length(); i++){
            count++;
            if(i+1 >= str.length() || str.charAt(i) != str.charAt(i+1)){
                newString.append(str.charAt(i));
                newString.append(count);
                count = 0;
            }
        }
        return newString.toString();
    }
    private static int getTotal(String str){
        int count = 0;
        int total = 0;
        for(int i = 0; i<str.length(); i++){
            count++;
            //aaabbbbb
            //String.valueOf() << 타입의 값을 String으로 변환한다.
            //String.valueOf(count).length() << count에 들어온 숫자값을 문자열로 바꾸고
            //문자열의 자릿수(길이)를 반환하는 함수
            if(i+1>=str.length() || str.charAt(i)!=str.charAt(i+1)){
                total += 1 + String.valueOf(count).length();
                count = 0;
            }
        }
        return total;
    }
}
