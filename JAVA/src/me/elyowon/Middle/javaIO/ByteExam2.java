package me.elyowon.Middle.javaIO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


// 파일로부터 한바이트식 읽어서 한바이트씩 써주는 코드

public class ByteExam2 {
    public static void main(String[] args) throws FileNotFoundException {
        long start = System.currentTimeMillis();
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream("src/me/elyowon/Middle/javaIO/ByteExam1.java");
            fos = new FileOutputStream("byte.txt");

            int readCount = -1;
            // 바이트의 수만큼만 읽어온다.
            // 자바는 1바이트 읽어올때 기본적으로 512바이트를 읽고 그중에서 511을 버리는 방법으로 읽는다.
            // 하지만 지금의 코드는 기본적으로 512읽고 하나도 버림없이 모두 읽음으로 낭비되는 것이없다.
            byte[] buffer = new byte[1024];
            while((readCount = fis.read(buffer))!= -1){
                //buffer를 0부터 readCount만큼써주세요
                fos.write(buffer,0,readCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        System.out.println(System.currentTimeMillis() - start);


    }
}