package elyowon.leetcode.algorithm;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 *
 * 접미사와 접두사가 일치하는 배열을 만듭니다.
 * 그리고 매칭되는
 */

public class KMP {

    public int result,  table[];

    public void failure(String patten) {
        int pattenSize = patten.length();

        table = new int[pattenSize];
        int j = 0;
        for(int i = 1; i< patten.length(); i++){
            while( j > 0 && patten.charAt(i) != patten.charAt(j)){
                j = this.table[j-1];
            }
            if(patten.charAt(i) == patten.charAt(j)){
                this.table[i] = ++j;
            }
        }
    }

    public void kmp(String origin,String patten) {
        int pattenIdx = 0;
        for (int originIdx = 0; originIdx < origin.length(); originIdx++) {
            while( pattenIdx > 0 && origin.charAt(originIdx) != patten.charAt(pattenIdx)){
                pattenIdx = table[pattenIdx-1];
            }
            if(origin.charAt(originIdx) == patten.charAt(pattenIdx)){
                if(pattenIdx == patten.length()-1){
                    System.out.println( originIdx - patten.length() + 2 + "번째에서 찾았습니다.");
                    pattenIdx = table[pattenIdx];
                }
                else ++pattenIdx;
            }
        }
    }

}