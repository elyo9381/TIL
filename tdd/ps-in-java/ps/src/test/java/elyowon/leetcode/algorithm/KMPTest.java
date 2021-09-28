package elyowon.leetcode.algorithm;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class KMPTest {


    public String origin, patten;

    @Test
    public void test() throws Exception {
        //given
        origin = "ababacabacaabacaaba";
        patten = "abacaaba";

        KMP kmp = new KMP();
        //when
        kmp.failure(patten);
        kmp.kmp(origin,patten);
        //then
    }

}