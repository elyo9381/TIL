package me.elyowon.Middle.javaIO;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ByteExam4 {

    public static void main(String[] args) {
        //trywith
        try(DataInputStream in = new DataInputStream(new FileInputStream("data.txt"))){
            int i = in.readInt();
            boolean b = in.readBoolean();
            double d = in.readDouble();

            System.out.println(i);
            System.out.println(b);
            System.out.println(d);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}