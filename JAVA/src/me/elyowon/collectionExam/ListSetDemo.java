package me.elyowon.collectionExam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class ListSetDemo {

    public static void main(String[] args) {
        ArrayList<String> al  = new ArrayList<String>();

        al.add("one");
        al.add("two");
        al.add("three");
        al.add("three");
        al.add("fore");

        System.out.println("array");
        Iterator it = al.iterator();


        // foreach를 쓰려면 배열안에 들어있는 타입으로 설정해줘야한다.
        /*for(type var : arr){sout(var)}*/

        /*하지만 it는 while문에서 자주 사용된다.
        *   Iterator {
        *     boolean hasNext();
        *     Object next();
        *     void remove();}
        *   등으로 구성되어있다.
        * */
        while(it.hasNext()){
            if(it.hasNext()){
                System.out.print("불러올값이있습니다.\n 다음의값은 : ");
                System.out.println(it.next());
            } else {

            }
        }


        HashSet<String> hs = new HashSet<String>();

        hs.add("one");
        hs.add("two");
        hs.add("three");
        hs.add("one");
        hs.add("two");

        Iterator it1 = hs.iterator();

        while(it1.hasNext()){
            System.out.println(it1.next());
        }

        /*
        *   hasNext
        *       반복할 데이터가 더 있으면 true, 더이상 반복할 데이터가 없다면 false를 리턴한다.
        *
        *   next
        *       hasNext가 true라는 것은 next가 리턴할 데이터가 존재한다는 의미다.
        *
        *   set의 기능중에는 부분집합, 합집합, 차집합, 등을 표현할수있는 메소드를 제공한다.
        *   생각해보면 set은 중복을 허용하지 않으므로 이를 표현할수있을거같다.
        *
        *   a ={1,2,3} b={3,4,5} c={1,2}
        *
        *   sout(A.containsAll(b));는 b가 a의 부분집합이냐고 묻는것이다. 결론은 false이다.
        *   sout(a.containsAll(c));는 c가 a의 부분집합이냐고 묻는것이다 이는 트루이다.
        *
        *   합집합(Union)
        *   a.addAll(b);
        *
        *   교집합(intersect)
        *   a.retainAll(b);
        *
        *   차집합(difference)
        *   a.removeAll(b)
        * */



    }

}