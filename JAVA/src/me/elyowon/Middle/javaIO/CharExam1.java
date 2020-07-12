package me.elyowon.Middle.javaIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CharExam1 {
    public static void main(String[] args) {
        // decorator pattern (데코레이터 패턴)
        // 객체에 추가적인 기능을 동ㅈ거으로 첨가하는 방식

        // 키보드로 부터 한줄식 입력을 받는
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = null;
        try {
                    // readLine() 한줄씩 읽는 수행코드
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(line);

    }
}