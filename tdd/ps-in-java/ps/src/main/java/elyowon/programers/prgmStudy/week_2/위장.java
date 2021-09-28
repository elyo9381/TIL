package elyowon.programers.prgmStudy.week_2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 지정된 Key의 값에 따라 Value를 어떻게 연산할지 정의, Value(리턴 값)을 반환
 */
public class 위장 {

    public int solution(String[][] clothes) {
        int answer = 1;

        HashMap<String, Integer> map = new HashMap<>();



        for (String[] clothe : clothes) {
            if (map.get(clothe[1]) != null) {
                map.put(clothe[1],map.get(clothe[1]) + 1);
            } else {
                map.put(clothe[1],1);
            }
        }

        answer = map.values().stream().mapToInt(value -> value + 1).reduce(1,(a,b) -> a * b);

        answer--;

        return answer;
    }

    public int solution2(String[][] clothes) {
        int ints = Arrays.stream(clothes).map(clothe -> clothe[1])
                .distinct()
                .mapToInt(strings -> (int) Arrays.stream(clothes).filter(s -> s[1].equals(strings)).count()+1)
                .reduce(1,(a,b) -> (a) * (b)) -1;
        return ints;
    }

    public int solution1(String[][] clothes) {

        List<List<String>> subsets = subsets(clothes);

        for (List<String> subset : subsets) {
            System.out.println("subset = " + subset);
        }

        int answer = subsets.size() - 1;

        return answer;

    }

    private List<List<String>> subsets(String[][] clothes) {
        List<List<String>> result = new LinkedList<>();
        HashSet visited = new HashSet();
        process(0,clothes,result,new LinkedList<String>(),visited);
        return result;
    }

    private void process(int start,String[][] clothes,List<List<String>> result,List<String> curr,HashSet visited) {
        result.add(new LinkedList<String>(curr));

        for (int i = start; i < clothes.length; i++) {
            if (!visited.add(clothes[i][1])) continue;
            curr.add(clothes[i][0]);
            process(i + 1,clothes,result,curr,visited);
            curr.remove(curr.size() - 1);
            visited.remove(clothes[i][1]);
        }
    }
}