package elyowon.ndb796;


import java.util.ArrayList;

class Node {
    private int index;
    private int distance;

    public Node(int index,int distance){
        this.index = index;
        this.distance = distance;
    }

    public void show(){
        System.out.println("("+this.index+","+this.distance+")");
    }
}

public class _14_인접리스트 {

    /**
     * 인접행렬은 2차원 리스트를 이용해 표현
     * */

    public static ArrayList<ArrayList<Node>> graph = new ArrayList<>();

    public static void main(String[] args) {

        for (int i = 0; i < 3; i++) {
            graph.add(new ArrayList<Node>());
        }

        //노드 0에 연결된 노드정보 저장(노드, 거리)
        graph.get(0).add(new Node(1,7));
        graph.get(0).add(new Node(2,5));

        graph.get(1).add(new Node(0,7));

        graph.get(2).add(new Node(0,5));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < graph.get(i).size(); j++) {
                graph.get(i).get(j).show();
            }
            System.out.println();
        }

    }

}