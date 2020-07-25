package me.elyowon.StackAndQueue.StackWithMinExam;
/*스택을 순차적으로 탐색하려면 O(n)시간이 걸린다.

 * 왜냐고 ? 전반적으로 탐색해야하기 때문이다.
*
* 하지만 나는 push(), pop(), min()을 구현하고 min()은 스택에서의 최소값을
* 리턴해주고 싶다. 또한 min()은 O(1)의 시간으로 구현해야할때
*
* 이를 해결할수있는 방법은 스택에 최소의값을 가지고 있는것이다.
* stact(value, min)이런식으로 말이다.
*
* */

import java.util.Stack;

class NodeWithMin {
    int value;
    int min;

    NodeWithMin(int v, int min) {
        value = v;
        this.min = min;
    }
}

class StackWithMin extends Stack<NodeWithMin> {
    public int min(){
        if(this.isEmpty()){
            return Integer.MAX_VALUE;
        } else {
            return peek().min;
        }
    }

    public void push(int value) {
        int newMin = Math.min(value, min());
        super.push(new NodeWithMin(value,newMin));
    }
}

public class StackMinExam {
    public static void main(String[] args) {
        StackWithMin s = new StackWithMin();
        s.push(3);
        s.push(5);
        s.push(1);
        s.push(2);

        System.out.println("min : " + s.min());
        System.out.println(s.pop().value);
        System.out.println(s.pop().value);
    }

}