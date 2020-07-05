package me.elyowon.ArraysAndStrings;

//url encoding
//문자열을 입력받고 문자열의 길이를 같이 입력받는다.
public class StringPS2 {
    public static void main(String[] args) {
        System.out.println(URLify("My john Smith          ",13));
    }
    // String을 받아서 char배열로 변환해주는 메소드
    private static String URLify(String str, int len){
        return URLify(str.toCharArray(),len);
    }
    // char[]에서 공백갯수를 카운트 하고
    // index변수에 길이와 공백의 갯수를 더한 사이즈를 체크한다.
    // 그리고 뒤에서 부터 index위치로 각각의 캐릭터배열인덱스를 이동한다.
    private static String URLify(char[] str, int len){
        int spaces = 0;
        for(int i = 0; i<len ; i++){
            if(str[i] == ' ') spaces++;
        }
        int index = len + spaces * 2-1;
        for(int  p = len -1; p>=0; p--) {
            if (str[p] == ' ') {
                str[index--] = '0';
                str[index--] = '2';
                str[index--] = '%';
            } else {
                str[index--] = str[p];
            }
        }
        return new String(str);
    }
}
