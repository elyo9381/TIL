package me.elyowon.StackAndQueue.StackSort;

/* 스택 2개를 이용한 정렬
*   무엇이 되었든 2개로 정렬은 한다는것은 어디에 넣고 두수를 비교한다는것이다.
*   그렇다면 정렬할수랑 비교할수가 있을때 비교할수가 크다면 비교할수를 다른곳에 넣고
*   정렬할수를 먼저 채우면된다. >> 이것이 오름차순정렬이다.
*
*   반대로 생각해보자 정렬할수랑 비교할수가 있다. 비교할수가 정렬할수보다 작으면
*   비교할수를 다른곳에 넣고 정렬할수를 먼저 채우면 된다. 이렇게 되면 내림차순이다.
* */

import java.util.Stack;

public class Test {
    public static void main(String[] args) {
    Stack<Integer> s1 = new Stack<Integer>();
    
    s1.push(3);
    s1.push(5);
    s1.push(1);
    s1.push(6);
    
    sort(s1);

        System.out.println(s1.pop());
        System.out.println(s1.pop());
        System.out.println(s1.pop());
        System.out.println(s1.pop());
    }

    private static void sort(Stack<Integer> s1) {
        // 스택을 하나 더만든다.
        Stack<Integer> s2 = new Stack<Integer>();
        // s1스택이 비어있지 않을때
        while(!s1.isEmpty()){
            // s1에서 팝을 하고 그것을 s2와 비교한다.
            int tmp = s1.pop();
            // s2는 비어있으면 안되고 또 비교할것과 정렬할것중에 비교할것이 커야한다.
            while(!s2.isEmpty() && s2.peek() > tmp){
                s1.push(s2.pop());
            }
            s2.push(tmp);
        }


        // 위의상황을 진행하면 s2에는 역순으로 들어있게 되므로 반대로 입력해 출력하면
        // 정렬이된다.
        while(!s2.isEmpty()){
            s1.push(s2.pop());
        }
    }


}