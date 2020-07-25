package me.elyowon.StackAndQueue.SetOfStackExam;

/* 스택에 데이터를 쌓다가 어느지점에 이르면 새로운 스택에 저장하는식으로
* SetOfStack을 구현하시오
*
*   조건 1 : 내부적으로느느 여러개 스택으로 나뉘지만, push와 pop은 여전히
*   하나의 Stack인 것처럼 동작해야함
*   조건 2 : 추가적으로 세트중 하나의 스택에서 지정해서 pop하는 popat()을 만드세요
*
*   위의 문제는 ArrayList을 만들어서 배열의 인덱스를 인자로 스택을 구현하고
*   스택을 pop()할수있도록 한다. 그리고 스택이 비었으면 배열의 인자도 삭제하도록 만든다.
*
*   ArrayList.get(0) 리스트의 0번째 인덱스를 가져옵니다.
* */

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

class SetOfStacks{
    int capasity;
    ArrayList<Stack<Integer>> stacks = new ArrayList<Stack<Integer>>();

    SetOfStacks(int capasity){
        this.capasity = capasity;
    }

    public Stack<Integer> getLastStack(){
        if(stacks.size() == 0) return null;
        return stacks.get(stacks.size() -1);
    }

    // stacks라는 리스트에 스택 추가
    public void addStack(){
        stacks.add(new Stack<Integer>());
    }

    public void removeLastStack(){
        stacks.remove(stacks.size() -1);
    }

    public void push(int data){
        Stack<Integer> last = getLastStack();
        if(last == null || last.size() == capasity){
            addStack();
            last = getLastStack();
        }
        last.push(data);
    }

    public int pop(){
        Stack<Integer> last = getLastStack();
        // 비웠는지 확인하고 예외를 던진다.
        if(last == null || last.isEmpty()){
            throw new EmptyStackException();
        }
        int data = last.pop();
        if( last.isEmpty()){

            removeLastStack();
        }
        return data;
    }
}
public class SetOfStack1 {
    public static void main(String[] args) {
        SetOfStacks setOfStacks = new SetOfStacks(3);
        setOfStacks.push(1);
        setOfStacks.push(2);
        setOfStacks.push(3);

        setOfStacks.push(4);
        setOfStacks.push(5);
        setOfStacks.push(6);

        setOfStacks.push(7);
        setOfStacks.push(8);

        System.out.println(setOfStacks.pop());
        System.out.println(setOfStacks.pop());
        System.out.println(setOfStacks.pop());
        System.out.println(setOfStacks.pop());



    }
}