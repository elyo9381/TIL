package me.elyowon.Middle.javaIO;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ByteExam3 {

    public static void main(String[] args) {
        //trywith
        try(DataOutputStream out = new DataOutputStream(new FileOutputStream("data.txt"))){
            out.writeInt(100);
            out.writeBoolean(true);
            out.writeDouble(59.9 );
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}