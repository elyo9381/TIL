package elyowon.programers.prgmStudy.week_3;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.stream.IntStream;


/**
 * 컴퓨터의 개수 n 개가 주어지고 각 연결됨을 뜻하는 배열이 주어질때
 * 구성된 네트워크는 몇개 ?
 */
public class 네트워크 {


    public int solution2(int n,int[][] computers) {

        int answer = 0;

        boolean[] visited = new boolean[n];

        LinkedList<Integer> q = new LinkedList<>();

        for (int i = 0; i < computers.length; i++) {

            q.add(i);
            if(!visited[i]){
                visited[i] = true;
                answer++;
            }

            while (!q.isEmpty()) {
                int temp = q.poll();

                for(int j=0; j< computers[i].length; j++){
                    if(temp == j) continue;
                    if(!visited[j]){
                        if(computers[temp][j] ==1){
                            q.add(j);
                            visited[i] = true;
                        }
                    }
                }
            }
        }

        return answer;
    }

    public int solution1(int n,int[][] computers) {
        int answer = 0;
        boolean[] visited = new boolean[n];

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                dfs(n,i,computers,visited);
                answer++;
            }
        }
        return answer;
    }

    private void dfs(int n,int index,int[][] computers,boolean[] visited) {
        visited[index] = true;

        for (int i = 0; i < n; i++) {
            if (computers[index][i] == 1 && i != index && !visited[i])
                dfs(n,i,computers,visited);
        }
    }

    public int solution(int n,int[][] computers) {
        int count = 0;

        int[] set = IntStream.range(0,n).toArray();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (computers[i][j] == 1 && i!=j) {
                    if (!isfind(set,i,j)) {
                        unionParent(set,i,j);
                    }
                }
            }
        }

        for (int j = 0; j < set.length; j++) {
            if (set[j] == j) count++;
        }
        return count;
    }

    private void unionParent(int[] set,int a,int b) {
        a = getParent(set,a);
        b = getParent(set,b);
        if (a < b) set[b] = a;
        else set[a] = b;
    }

    private boolean isfind(int[] set,int a,int b) {
        a = getParent(set,a);
        b = getParent(set,b);
        if (a == b) return true;
        else return false;
    }

    private int getParent(int[] set,int x) {
        if (set[x] == x) return x;
        return set[x] = getParent(set,set[x]);
    }


}