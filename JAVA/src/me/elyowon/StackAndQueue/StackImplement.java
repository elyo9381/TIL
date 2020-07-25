package me.elyowon.StackAndQueue;
import java.util.EmptyStackException;

/*
*   stack stack = new stack(); / 가능
*   Stack<T> stack = new Stack<T>(); /제너릭 가능
*   stack.push()/ 데이터삽입
*   stack.pop() / 데이터삭제
*   stack.peek() / 현재 top반환
*   stack.empty()/ 비어있는지판별
*   stack.search() / 자료가 몇번째에 들어있는지 반환(인덱스x, 자료순번)
*
* */

class Stack<T>{
    class Node<T>{
        private T data;
        private Node<T> next;
        public Node(T data){
            this.data = data;
        }
    }
    private Node<T> top;
    public T pop(){
        if(top == null){
            throw new EmptyStackException();
        }
        T item = top.data;
        top = top.next; // 탑의 다음에를 탑으로 만들어준다.
        return item;
    }

    // item을 data에 넣는 Node t를 생성하고
    public void push(T item){
        Node<T> t = new Node<T>(item);
        t.next = top; // top앞에 위치시킨다.
        top = t; //  그리고 t node를 top으로 위치시킨다.
    }
    public T peek(){
        if(top == null){
            throw new EmptyStackException();
        }
        return top.data;
    }
    public boolean isEmpty(){
        return top == null;
    }
}
public class StackImplement {
    public static void main(String[] args) {
        Stack<Integer> s= new Stack<Integer>();
        s.push(1);
        s.push(2);
        s.push(3);
        s.push(4);
        System.out.println(s.pop());
        System.out.println(s.pop());
        System.out.println(s.peek());
        System.out.println(s.pop());
        System.out.println(s.isEmpty());
        System.out.println(s.pop());
        System.out.println(s.isEmpty());
    }
}
