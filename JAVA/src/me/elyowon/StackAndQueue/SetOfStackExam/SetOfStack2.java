package me.elyowon.StackAndQueue.SetOfStackExam;

/*
*   SetOfStack1 클래스는 문제가 있다.
*   팝을하면 이전데이터를 쉬프트해야한다.
*
*   그래야 지정된 스택에서 popAt()을 수행할수있다.
*   하지만 모든 스택을 탐색하기에는 너무 오랜과정이 걸린다.
*
*   그래서 스택에 top, bottom이라는 포인터를 두어서 확인하려고한다.
*   또한 스택은 탑다운방식으로 아래 누가있는지는 알수있만 위에 누가 있는지는 모른다.
*   그러므로 위아래 어떤 데이터가 있는지 알아야한다.
*
*   이것은 리스트로 스택 구현하기 이다.
*
*
* */

import java.util.ArrayList;
import java.util.EmptyStackException;

class FullStackException extends Exception{
    FullStackException(){
        super();
    }
}
class EmptyStackSetException extends Exception{
    EmptyStackSetException(){
        super();
    }
}

// AL인덱스상에서 구현될 stack이고 stack은 위아래 데이터를 알아야하므로 노드로 구성되어있다.
class Stack<E> {
    //스택의 요소를 노드로 구성함 why? 기준스택요소의 위아래를 알고싶어서
    class Node {
        E data;
        Node below;
        Node above;
        Node (E data) {
            this.data= data;
        }
    }
    int capacity; // 스택을 쌓을수있는 용량
    int size; // 스택의 사이즈
    Node top; // 기준 스택의 윗놈
    Node bottom; // 기준 스택의 아랫놈

    // 생성자 스택의 용량설정
    Stack(int capacity){
        this.capacity = capacity;
    }

    // 스택이 비었는지 확인
    public boolean isEmpty() {return size() == 0;}
    // 스택이 가득찼는지 확인
    public boolean isFull() {return size == capacity;}
    public int size(){return size;}

    // 스택에 push할때 꽉차있으면 예외
    // 그리고 스택의 요소를 푸시
    public void push(E d) throws FullStackException{
        if(isFull()) throw new FullStackException();

        Node n = new Node(d);
        push(n); // 노드에 데이터푸시
    }

    // 노드에 데이터 푸시하는 메소드
    public void push(Node n) throws FullStackException{
        // 노드는 결국 스택의 요소로 사용되므로 스택이 꽉차면 더이상 넣을수없다.
        // 그럴땐 예외처리 한다.
        if(isFull()) throw new FullStackException();

        // 비어있으면 넣고 비어있지않으면 기존에 있던 top과 n의 위치를 다시 설정한다.
        if(isEmpty()) {
            top = n;
            bottom = n;
        } else {
            top.above = n; // n이  추가되었다면 top위에 올라갔다고 표시
            n.below =top; // n아래 top이 존재한다고 표시
            top = n; //  위의 표시를 다했으면 n이 새로운 top이 되었다고 표시
        }
        size++; // 사이즈를 늘려준다. why? capacity 많큼 넣어야하므로
    }


    public E pop(){
        // 비어있다면 예외처리
        if(isEmpty()) throw new EmptyStackException();
        E data = top.data; //pop할 데이터를 임의의 변수에 담는다. 타입에 맞춰서
        top = top.below; // 그리고 탑의 아랫것이 탑이라고 지정해준다.
        if(top == null){ // 탑이 널이면 아무것도 없는것이므로
            bottom = null; // 바텀도 널
        } else {
            // 팝이 되면 탑이 나가고 그아랫것이 탑이 되는데 이때 나간탑자리를 null로 만들어준다.
            top.above = null;
        }
        size--; // 하나를 지웠으므로 사이즈도 줄여준다.
        return data; // 팝한 데이터를 반환한다.
    }

    // shift하면 오른쪽에 있는것을 삭제하고 가져와야하므로 왼쪽에 푸시해야하므로
    public Node popBottom() {
        // 비어있다는 것은 스택이 없는거 (팝할게없다).
        if (isEmpty()) throw new EmptyStackException();
        Node n = bottom; // 바텀을 임시노드 n에다가 넣는다.
        //  데이터가 2개 있다면 바텀의 위를 바텀으로 만들어주고 현재바텀은 삭제 되어야한다.
        bottom = bottom.above;
        if (bottom == null) {
            top = null;
        } else {
            bottom.below = null;
        }
        size--;
        return n;
    }

}
// 스택을 값으로 갖는 리스트 클래스 구현
class SetOfStack{
    int capacity; // 리스트에서 스택의 용량
    //스택으로 값는 리스트 동적 할당
    ArrayList<Stack<Integer>> stacks = new ArrayList<Stack<Integer>>();

    // 생성자에 capacity를 추가해서 몇개의 스택을 쌓을지 결정
    SetOfStack(int capacity){
        this.capacity = capacity;
    }

    // 스택을 값으로하는 리스트 추가
    public void addStack(){
        stacks.add(new Stack<Integer>(capacity));
    }
    //마지막 리스트 삭제
    public void removeLastStack(){
        stacks.remove(stacks.size() -1);
    }

    // 리스트의 마지막 값 반환
    public Stack<Integer> getLastStack(){
        if(stacks.size() == 0) return null;
        return stacks.get(stacks.size()-1);
    }


    public void push(int data){
        Stack<Integer> last= getLastStack();
        if(last == null|| last.isFull()){
            addStack();
            last = getLastStack();
        }
        try{last.push(data);} catch (FullStackException e) {}
    }

    public int pop(){
        Stack<Integer> last = getLastStack();
        if(last==null || last.isEmpty()){
            throw new EmptyStackException();
        }
        int data = last.pop();
        if(last.isEmpty()) removeLastStack();
        return data;
    }

    // 리스트(스택)에서 특정 리스트 인덱스에서 pop()수행 >> 리스트의 인덱스의 스택에서 top을 pop한다.
    public int popAt(int index){
        // 원소가 없다는거 예외를 표현하기 위해서 사용
        if(stacks.size() <= 0){
            throw new EmptyStackException();
        }
        //범위를 벗엇난것
        if(index < 0 || index >= stacks.size()){
            throw new IndexOutOfBoundsException();
        }

        // index의 스택에서 하나를 pop하고 다음 인덱스의 스택에 아래부분의 값을 가져옴
        Stack<Integer> stack = stacks.get(index);
        if(stack == null || stack.isEmpty())
            throw new EmptyStackException();
        int data = stack.pop(); //index의 스택에서 하나를 pop하고
        shiftLeft(index); // 다음 인덱스의 스택에 아래부분의
        return data;
    }

    // 리스트인덱스의 쉬프트
    public void shiftLeft(int index) {
        // why? size() - 1이냐 ?? 리스트의 사이즈 2일때 0번인덱스에서만 쉬프트가 가능하므로
        if(index < stacks.size() -1){
            Stack<Integer> right = stacks.get(index+1);
            Stack<Integer> left = stacks.get(index);

            // 논리상으로 구멍이 없지만 자바가 알아야하므로 예외처리함
            try{
                // 오른쪽에서 바텀의값을 팦하고 그것을 링크드노드로 반환받고
                // 또 그것을 다시 푸시
                left.push(right.popBottom());
            } catch (FullStackException e){}
            // 스택이 없다면 리스트제거
            if (right.isEmpty()){
                stacks.remove(index + 1);
            }
            // 재귀적인 방법을 통해서 쉬프트 연산을 종료 한다.
            shiftLeft(index + 1);
        }
    }
}
public class SetOfStack2 {
    public static void main(String[] args) {
        SetOfStack setOfStack = new SetOfStack(3);

        setOfStack.push(1);
        setOfStack.push(2);
        setOfStack.push(3);

        setOfStack.push(4);
        setOfStack.push(5);
        setOfStack.push(6);

        setOfStack.push(7);
        setOfStack.push(8);

        System.out.println(setOfStack.popAt(0));
        System.out.println(setOfStack.popAt(1));
    }
}