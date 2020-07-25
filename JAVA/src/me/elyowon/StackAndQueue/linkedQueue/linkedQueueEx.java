package me.elyowon.StackAndQueue.linkedQueue;


import java.util.LinkedList;

enum AnimalType{
    DOG, CAT
}

// 추상 클래스는 부모 클래스를 만드는것이다. 추상클래스를 상속하면
// 그 클래스는 추상클래스의 기능을 다시 사용할수있으며 수정도 가능하고 추가도 가능하다.
// 새로운 기능을 저의하는데 있어서 바탕이 될 추상 클래스
abstract class Animal{
    AnimalType type;
    String name;
    int order;

    Animal(AnimalType type, String name){
        this.type = type;
        this.name = name;
    }

    void setOrder(int order){
        this.order = order;
    }

    int getOrder(){
        return order;
    }

    String info(){
        return order + "type: " + type + ", name : " +name;
    }
}

// Dog클래스는 Animal을 상속받고 새로운기능을 넣을수있다.
class Dog extends Animal{
    Dog(String name){
        // 상속받은 Animal클래스의 생성자
        super (AnimalType.DOG,name);
    }
}
class Cat extends Animal{
    Cat(String name){
        super(AnimalType.CAT, name);
    }
}

// 도그 클래스와 캣 클래스를 통해서 링크드리스트구현
class AnimalShelter{
    LinkedList<Dog> dogs = new LinkedList<Dog>();
    LinkedList<Cat> cats = new LinkedList<Cat>();

    int order;
    AnimalShelter(){
        order = 1;
    }

    // Dog/Cat이 Animal을 상속받았기에 queue에 넣을때 Animal 형으로 넣어야한다.
    void enqueue(Animal animal){
        animal.setOrder(order);
        order++;
        if(animal.type == AnimalType.DOG){
            dogs.addLast((Dog) animal);
        } else if(animal.type == AnimalType.CAT){
            cats.addLast((Cat) animal);
        }
    }

    // 링크드리스트의 poll()는 데이터 제거 메소드
    // Queue<T>에서도 지원한다.
    Animal dequeueDog(){
        return dogs.poll();
    }
    Animal dequeueCat(){
        return cats.poll();
    }
    Animal dequeue(){
        if(dogs.size() == 0 && cats.size() == 0){
            return null;
        } else if (dogs.size() ==0){
            return cats.poll();
        } else if (cats.size() == 0){
            return dogs.poll();
        }

        Animal dog = dogs.peek();
        Animal cat = cats.peek();
        if(cat.order < dog.order){
            return cats.poll();
        } else{
            return dogs.poll();
        }
    }
}

public class linkedQueueEx {
    public static void main(String[] args) {
        Dog d1 = new Dog("puppy");
        Dog d2 = new Dog("kangGi");
        Dog d3 = new Dog("mongmongYi");
        Cat c1 = new Cat("shasha");
        Cat c2 = new Cat("야옹이");
        Cat c3 = new Cat("mimi");

        AnimalShelter as = new AnimalShelter();
        as.enqueue(d3);
        as.enqueue(c2);
        as.enqueue(d1);
        as.enqueue(c1);
        as.enqueue(d2);
        as.enqueue(c3);

        System.out.println(as.dequeueCat().info());
        System.out.println(as.dequeueDog().info());
        System.out.println(as.dequeue().info());
        System.out.println(as.dequeue().info());
        System.out.println(as.dequeue().info());
        System.out.println(as.dequeue().info());
    }
}