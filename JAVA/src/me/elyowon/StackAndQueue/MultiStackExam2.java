package me.elyowon.StackAndQueue;

import java.util.EmptyStackException;


class MultiStack{
    private class StackInfo{
     public int start, dataSize, stackSize;
     public StackInfo(int start, int stackSize){
         this.start = start;
         this.stackSize = stackSize;
         this.dataSize = 0;
     }
     public boolean isWithinStack(int index){
         if(index < 0 || index >= values.length){
            return false;
         }
         int virtualIndex = index < start? index + values.length:
                 index;
         int end = start + stackSize;
         return start <= virtualIndex && virtualIndex <end;
     }

     public int getLastStackIndex() {
         return adjustIndex(start + stackSize - 1);
     }
     public int getLsatDataIndex(){
         return adjustIndex(start + dataSize - 1);
     }
     public int getNewDataIndex() {
         return adjustIndex(getLsatDataIndex() + 1);
     }
     public boolean isFull(){
         return dataSize == stackSize;
     }
     public boolean isEmpty(){
         return dataSize == 0;
     }
    }

    private StackInfo[] info;
    private int[] values;

    public MultiStack(int numOfStacks, int defaultSize){
        info = new StackInfo[numOfStacks];
        for(int i = 0; i<numOfStacks; i++){
            info[i] = new StackInfo(defaultSize * i, defaultSize);
        }
        values =  new int[numOfStacks * defaultSize];
    }

    private void expand(int stackNum) {
        int nextStack = (stackNum + 1) % info.length;
        shift(nextStack);
        info[stackNum].stackSize++;
    }
    private void shift(int stackNum){
        StackInfo stack = info[stackNum];
        if(stack.dataSize >= stack.stackSize){
            int nextStack = (stackNum+1) % info.length;
            shift(nextStack);
            stack.stackSize++;
        }
        int index = stack.getLastStackIndex();
        while(stack.isWithinStack(index)){
            values[index] = values[previousIndex(index)];
            index= previousIndex(index);
        }
        values[stack.start] = 0;
        stack.start = nextIndex(stack.start);
        stack.stackSize--;
    }

    public int numberOfElements(){
        int totalDataSize = 0;
        for(StackInfo sd : info){
            totalDataSize +=sd.dataSize;
        }
        return totalDataSize;
    }

    public boolean allStacksAreFull(){
        return numberOfElements() == values.length;
    }
    private int adjustIndex(int index){
        int max = values.length;
        return ((index % max) + max ) %max;
    }

    private int nextIndex(int index){
        return adjustIndex(index +1);
    }

    private int previousIndex(int index){
        return adjustIndex(index -1);
    }

    public void push(int stackNum, int value) throws FullStackException{
        if (allStacksAreFull()){
            throw new FullStackException();
        }
        StackInfo stack = info[stackNum];
        if(stack.isFull()){
            expand(stackNum);
        }
        values[stack.getNewDataIndex()] = value;
        stack.dataSize++;
    }
    public int pop(int stackNum) {
        StackInfo stack = info[stackNum];
        if(stack.isEmpty()){
            throw new EmptyStackException();
        }
        int last = stack.getLsatDataIndex();
        int value = values[last];
        values[last] = 0;
        stack.dataSize--;
        return value;
    }

    public int peek(int stackNum){
        StackInfo stack = info[stackNum];
        if(stack.isEmpty()){
            throw new EmptyStackException();
        }
        return values[stack.getLsatDataIndex()];
    }

}

public class MultiStackExam2 {
    public static void main(String[] args) {
        MultiStack ms = new MultiStack(3,5);

        try{
            ms.push(0,1);
            ms.push(0,2);
            ms.push(0,3);
            ms.push(0,4);
            ms.push(0,5);
            ms.push(0,6);
            ms.push(0,7);
            ms.push(0,8);
            ms.push(0,9);

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
            System.out.println("stack #0" + ms.pop( 0));
        } catch(EmptyStackException e) {
            System.out.println("It's empty");
        }
    }

}