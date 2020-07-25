package me.elyowon.StackAndQueue;
import java.util.NoSuchElementException;

/*
*   Queue<T> q = new LinkedList<T>();
*   q.offer(); / 데이터 삽입(순차보관)
*   q.poll(); / 데이터 삭제
*   q.peek(); / 현제 데이터 반환(front)
*   q.Empty(); 사용가능
*
* */

class Queue<T>{
    class Node<T>{
        private T data;
        private Node<T> next;

        public Node(T data){
            this.data=data;
        }
    }

    private Node<T> first;
    private Node<T> last;

    public void offer(T item){
        Node<T> t= new Node<T>(item);
        if(last !=null){
            last.next = t;
        }
        last = t;
        if(first ==null){
            first = last;
        }
    }

    public T poll(){
        if(first == null){
            throw new NoSuchElementException();
        }
        T data = first.data;
        first = first.next;

        if(first == null){
            last = null;
        }
        return data;
    }

    public T peek(){
        if(first == null){
            throw new NoSuchElementException();
        }
       return first.data;
    }
    public boolean isEmpty(){
        return first == null;
    }
}
public class QueueImplement {
    public static void main(String[] args) {
        Queue<Integer> q = new Queue<Integer>();
        q.offer(1);
        q.offer(2);
        q.offer(3);
        q.offer(4);
        System.out.println(q.poll());
        System.out.println(q.poll());
        System.out.println(q.peek());
        System.out.println(q.isEmpty());
        System.out.println(q.peek());
        System.out.println(q.poll());
        System.out.println(q.isEmpty());
        System.out.println(q.poll());
        System.out.println(q.isEmpty());
    }
}
