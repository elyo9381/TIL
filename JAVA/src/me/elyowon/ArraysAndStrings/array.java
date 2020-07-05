package me.elyowon.ArraysAndStrings;

/*
 * 자바의 Arrarylist은 동적으로 배열이 늘어난다.
 * 검색시간은 O(1), 추가시간도 O(1)
 * 동적으로 1개입력하고 공간이 부족하면 더블링한다.
 * 그리고 추가하고 공간이 부족하면 더블링 한다.
 * 이를 통해서 계속적으로 공간을 늘려 배열을 동적으로 할당하는 방법이다.
 * */

class ArrayList{
    private Object[] data;
    private int size;
    private int index;

    public ArrayList(){
        this.size = 1;
        this.data = new Object[this.size];
        this.index = 0;
    }

    public void add(Object obj){
        if(this.index == this.size -1){
            doubling();
        }
        data[this.index] = obj;
        this.index++;
    }

    private void doubling(){
        this.size = this.size *2;
        Object[] newData = new Object[this.size];
        for(int i = 0; i<data.length; i++){
            newData[i] = data[i];
        }
        this.data = newData;
    }

    public Object get(int i) throws Exception{
        if(i > this.index -1){
            throw new Exception("ArrayIndexOutOfBound");
        }else if(i<0){
            throw new Exception("Negative Value");
        }else return this.data[i];
    }

    public void remove(int i) throws Exception{
        if(i > this.index -1){
            throw new Exception("ArrayIndexOutOfBound");
        }else if(i<0){
            throw new Exception("Negative Value");
        }
        for(int x = i; x <this.data.length-1; x++){
            data[x] = data[x+1];
        }
        this.index--;
    }
}

public class array {
    public static void main(String[] args) throws Exception {
        ArrayList al = new ArrayList();
        al.add("0");
        al.add("1");
        al.add("2");
        al.add("3");
        al.add("4");
        al.add("5");
        al.add("6");
        al.add("7");
        al.add("8");
        al.add("9");
        al.add("10");
        System.out.println(al.get(5));
        al.remove(5);
        System.out.println(al.get(5));
    }
}
