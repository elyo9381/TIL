package me.elyowon.Middle.javaUtil.exam;

public class Box<T> {
    private T obj;

    public void setObj(T obj){
        this.obj = obj;
    }

    public T getObj(){
        return obj;
    }

}