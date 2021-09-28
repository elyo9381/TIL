package elyowon.programers.prgmStudy.week_3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class 네트워크Test {


    @Test
    public void test() throws Exception{
        //given
        네트워크 v = new 네트워크();

        int n = 3;
        int[][] computers = new int[n][n];
        computers[0] = new int[]{1,1,0};
        computers[1] = new int[]{1,1,0};
        computers[2] = new int[]{0,0,1};

        int dfdsff;
        int solution = v.solution(n,computers);
        System.out.println(solution);
        //when

        //then
    }

}