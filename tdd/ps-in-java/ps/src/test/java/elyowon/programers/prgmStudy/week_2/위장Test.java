package elyowon.programers.prgmStudy.week_2;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class 위장Test {

    public int solution(String[][] clothes) {
        int answer = 0;
        List<String> collect = Arrays.stream(clothes).map(clothe -> clothe[1]).distinct().collect(Collectors.toList());
        for (String s : collect) {
            System.out.println("s = " + s);
        }
        long count = Arrays.stream(clothes).filter(s -> s[1].equals("headgear")).count();
        System.out.println("count = " + count);
        int ints = Arrays.stream(clothes).map(clothe -> clothe[1])
                .distinct()
                .mapToInt(strings -> (int) Arrays.stream(clothes).filter(s -> s[1] == strings).count()+1)
                .reduce(1,(a,b) -> (a) * (b)) -1;
//                .stream().reduce(1L,(a,b) -> a * b);

//        for (int aLong : ints) {
//            System.out.println("aLong = " + aLong);
//        }

        return ints;

    }

    @Test
    public void test() throws Exception{
        //given
//        위장 v = new 위장();
        String[][] a = new String[3][];
        a[0] = new String[]{"yellowhat","headgear"};
        a[1] = new String[]{"bluesunglasses", "eyewear"};
        a[2] =  new String[]{"green_turban", "headgear"};
        //when
//        int solution = v.solution(a);
//        int solution = v.solution2(a);
        int solution = solution(a);
        System.out.println("solution = " + solution);
        //then
    }
}