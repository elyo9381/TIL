package me.elyowon.ArraysAndStrings;
import java.util.LinkedList;

/*
* 해시테이블을 구성할때 만들어야하는 기능들은
* HashTable (해시 테이블 구성 각 테이블의 인덱스는 linkedlist로 구성)
* getHashCode (입력된 문자열 혹은 숫자열들을 특정방법으로 해쉬값을 생성)
* convetToIndex (hashcode를 충돌이 잘 일어나지 않게 index로 바꾸는)
* put ( getHashcode, convertToIndex 를 이용하여 해시테이블의 해당 인덱스의 링크드 리스트를 확인하고 없으면
* 새롭게 만들게 기존에 있던거에 끝에 연결하는 구조
* searchKey (targetKey와 링크드리스트의 내부키를 비교하여 같으면 노드를 반환)
* get(  getHashcode, convertToIndex, seachKey를 확인하여 해시테이블의 링크드리스트의 키값이 같으면
* Node.value()메소드 반환
* */

class HashTable{
    class Node{
        String key;
        String value;
        public Node(String key,String value){
            this.key = key;
            this.value = value;
        }
        String value(){
            return value;
        }
        void value(String value){
            this.value = value;
        }
    }

    LinkedList<Node>[] data;

    HashTable(int size) {
        this.data = new LinkedList[size];
    }
    int getHashCode(String key){
        int hashcode = 0;
        for(char c: key.toCharArray()){
            hashcode +=c;
        }
        return hashcode;
    }
    int converToIndex(int hashcode){
        return hashcode % data.length;
    }
    Node searchKey(LinkedList<Node> list, String key){
        if(list == null) return null;
        for(Node node : list){
            if(node.key.equals(key)){
                return node;
            }
        }
        return null;
    }
    void put(String key, String value){
        int hashcode = getHashCode(key);
        int index = converToIndex(hashcode);
        LinkedList<Node> list = data[index];
        if(list == null){
            list = new LinkedList<Node>();
            data[index] = list;
        }
        Node node = searchKey(list,key);
        if(node == null){
            list.addLast(new Node(key,value));
        } else {
            node.value(value);
        }
    }

    String get(String key){
        int hashcode = getHashCode(key);
        int index =  converToIndex(hashcode);
        LinkedList<Node> list = data[index];
        Node node = searchKey(list, key);
        return node == null? "Not found" : node.value();
    }
}
public class HashFuntion {
    public static void main(String[] args) {
        HashTable h = new HashTable(3);
        h.put("sung", "she is pretty");
        h.put("jin", "she is good");
        h.put("min", "she is bad");
        h.put("hee", "she is not");
        System.out.println(h.get("sung"));
        System.out.println(h.get("jin"));
        System.out.println(h.get("min"));
        System.out.println(h.get("hee"));
    }
}
