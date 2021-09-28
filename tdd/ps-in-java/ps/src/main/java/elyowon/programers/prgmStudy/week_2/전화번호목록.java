package elyowon.programers.prgmStudy.week_2;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 지정된 Key의 값에 따라 Value를 어떻게 연산할지 정의, Value(리턴 값)을 반환
 */
public class 전화번호목록 {

    public boolean solution(String[] phone_book) {
        HashSet<String> hashSet = (HashSet<String>) Arrays.stream(phone_book).collect(Collectors.toSet());

        for (String key : hashSet)
            for (int j = 1; j <= key.length() - 1; j++) {
                if (hashSet.contains(key.substring(0, j))) return false;
            }
        return true;
    }
    public boolean solution1(String[] phone_book) {

        Arrays.sort(phone_book);

        int length = phone_book.length;
        for(int i =0; i<length-1; i++){
            if(phone_book[i+1].startsWith(phone_book[i])) return false;
        }
        return true;
    }

}
