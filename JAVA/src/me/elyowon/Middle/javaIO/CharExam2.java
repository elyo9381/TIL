package me.elyowon.Middle.javaIO;

import java.io.*;

public class CharExam2 {
    public static void main(String[] args) {
        BufferedReader br = null;

        PrintWriter pw = null;
        try {
            // 파일 읽어오는 코드
            br = new BufferedReader(new FileReader("src/me/elyowon/Middle/javaIO/CharExam2.java"));
            pw = new PrintWriter((new FileWriter("test.txt")));

            String line = null;
                            //한줄씩 읽는코드
            while((line = br.readLine()) != null){
                // 파일에 한줄씩 쓰는코드
                pw.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //꼭 닫아줘야한다. 파일을 그래서 파일에 쓸수있다.
            pw.close();
            try{
                br.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }



    }

}