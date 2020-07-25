package me.elyowon.StackAndQueue.StackWithMinExam;

/* 스택을 두개만들어서 푸시 팝 할때 가장작은 값을 하나의 스택에 넣어줍니다.
* 그러면 언제든지 다른 스택에서 최소값을 알수있기 때문에
*
* 자바는 A라는 스택을 사용할때 (생성자를 통해서 스택 B)를 생성할수있다.
* 이것은 클래스 내부에 종속적인것이 아니고 생성자를 통해서 또 다시 스택을 생성하는것이기
* 때문에 개별적이다.
*
* StackMInWith2라는 커다란 스택클래스가 생성될때 내부적으로 개별적인 Integer스택이 하나 생성된다.
*
*
* * */


import java.util.Stack;
import java.util.stream.DoubleStream;


class StackMInWith2 extends Stack<Integer>{

    Stack<Integer> s2;

    public StackMInWith2() {
        s2 = new Stack<Integer>();
    }

    public int min(){
        if(s2.isEmpty()){
            return Integer.MAX_VALUE;
        } else {
            return s2.peek();
        }
    }

    public void push(int value){
        if(value < min()){
            s2.push(value);
        }
        super.push(value);
    }

    public Integer pop() {
        int value = super.pop();
        if(value == min()){
            s2.pop();
        }
        return value;
    }

}

public class StackMinExma2{
    public static void main(String[] args) {
        StackMInWith2 s = new StackMInWith2();
        s.push(3);
        s.push(5);
        s.push(1);
        s.push(2);

        s.s2.isEmpty();

        System.out.println("min : " + s.min());
        System.out.println("pop : " + s.pop());
    }
}
