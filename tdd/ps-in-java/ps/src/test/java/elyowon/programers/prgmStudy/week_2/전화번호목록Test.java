package elyowon.programers.prgmStudy.week_2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class 전화번호목록Test {


    @Test
    public void test() throws Exception{
        //given
        전화번호목록 v = new 전화번호목록();
        //when
        String[] strArray = new String[]{"119", "97674223", "1195524421"};
        boolean solution = v.solution1(strArray);
        System.out.println("solution = " + solution);
        //then
    }
}

