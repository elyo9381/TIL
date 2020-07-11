package me.elyowon.StackAndQueue;

import java.util.EmptyStackException;
/*
*   배열을 통해서 스택만들기 !!!
*   배열을 통해서 배열을 만들려고 할때
*   multiStack이라는것은 exam)예를 들면 배열 중첩시켜 스택을 만드는것이다.
*   스택을 구성하는 클래스를 하나 만들고 클래스의 멤버변수로는 배열의 갯수, 배열사이즈
*   value를 넣을 배열, size를 넣을 배열 등을 만든다.
*
*
* */

class FullStackException extends Exception {
    public FullStackException(){
        super();
    }
    public FullStackException(String msg){
        super(msg);
    }
}

class FixedMultiStacks {
    private int numOfStacks = 3;
    private int stackSize;
    private int[] values;
    private int[] sizes;

    public FixedMultiStacks(int stackSize){
        this.stackSize = stackSize;
        this.sizes = new int[numOfStacks];
        this.values = new int[numOfStacks*stackSize];
    }

    public boolean isEmpty(int stackNum){
        return sizes[stackNum] == 0;
    }

    public boolean isFull(int stackNum){
        return sizes[stackNum] == stackSize;
    }

    public int getTopIndex(int stackNum){
        // 기준이 되는 주소에 더해진값 == offset
        int offset = stackSize * stackNum;
        int size = sizes[stackNum];
        return offset + size  ;
    }

    public void push(int stackNum, int data) throws FullStackException{
        if(isFull(stackNum)){
            throw new FullStackException();
        }
        values[getTopIndex(stackNum)] = data;
        sizes[stackNum]++;
    }
    public int pop(int stackNum){
        if(isEmpty(stackNum)){
            throw new EmptyStackException();
        }
        int top = getTopIndex(stackNum)-1;
        int data = values[top];
        values[top] = 0;
        sizes[stackNum]--;
        return data;
    }
    public int peek(int stackNum){
        if(isEmpty(stackNum)){
            throw new EmptyStackException();
        }
        return values[getTopIndex(stackNum)];
    }
}
public class MultiStackExam1 {
    public static void main(String[] args) {
        FixedMultiStacks ms = new FixedMultiStacks(5);
        try{
            ms.push(0,1);
            ms.push(0,2);
            ms.push(0,3);
            ms.push(0,4);
            ms.push(0,5);

            ms.push(1,1);
            ms.push(1,2);
            ms.push(1,3);
            ms.push(1,4);
            ms.push(1,5);
        } catch (FullStackException e){
            System.out.println("It's full");
        }

        try{
            System.out.println("stack #0" + ms.pop(0));
            System.out.println("stack #0" + ms.pop(0));
            System.out.println("stack #0" + ms.peek(0));
            System.out.println("stack #0" + ms.pop(0));
            System.out.println("stack #0" + ms.isEmpty(0));
            System.out.println("stack #0" + ms.pop(0));
            System.out.println("stack #0" + ms.pop(0));
            System.out.println("stack #0" + ms.isEmpty(0));

            System.out.println("stack #1" + ms.pop(1));
            System.out.println("stack #1" + ms.pop(1));
            System.out.println("stack #1" + ms.peek(1));
            System.out.println("stack #1" + ms.pop(1));
            System.out.println("stack #1" + ms.isEmpty(1));
            System.out.println("stack #1" + ms.pop(1));
            System.out.println("stack #1" + ms.pop(1));
            System.out.println("stack #1" + ms.isEmpty(1));

        } catch(EmptyStackException e) {
            System.out.println("It's empty");
        }
    }

}