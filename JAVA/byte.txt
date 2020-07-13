package me.elyowon.Middle.javaIO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


// 파일로부터 한바이트식 읽어서 한바이트씩 써주는 코드

public class ByteExam1 {
    public static void main(String[] args) throws FileNotFoundException {
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream("src/me/elyowon/Middle/javaIO/ByteExam1.java");
            fos = new FileOutputStream("byte.txt");

            int readData = -1;
                while((readData = fis.read())!= -1){
                fos.write(readData);
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


    }
}