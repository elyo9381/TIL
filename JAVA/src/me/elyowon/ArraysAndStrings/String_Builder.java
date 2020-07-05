package me.elyowon.ArraysAndStrings;
class StringBuilder{
    private char[] value;
    private int size;
    private int index;

    StringBuilder(){
        size = 1;
        value = new char[size];
        index = 0;
    }
    public void append(String str){
        if( str == null) str = "null";
        int len = str.length();
        ensureCapacity(len);
        for(int i =0; i<len; i++){
            value[index] = str.charAt(i);
            index++;
        }
    }
    private void ensureCapacity(int len){
        if(index + len > size){
            size = (index + len) *2;
            char[] newValue = new char[size];
            for(int i = 0 ;i< value.length; i++){
                newValue[i] = value[i];
            }
            value = newValue;
        }
    }
    public String toString(){
        return new String(value, 0, index);
    }
}
public class String_Builder {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("elyo");
        sb.append(" is");
        sb.append(" goodman~");
        System.out.println(sb.toString());
    }
}
