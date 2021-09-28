package elyowon.programers.prgmStudy.week_2;

import java.util.ArrayList;
import java.util.LinkedList;


public class 다리를지나는트럭 {

    class Truck {
        int weight;
        int move;

        public Truck(int weight) {
            this.weight = weight;
            this.move = 1;
        }

        public void moving() {
            move++;
        }

        public int getWeight() {
            return weight;
        }

        public int getMove() {
            return move;
        }
    }

    public int solution(int bridge_length,int weight,int[] truck_weights) {
        int time = 0;
        LinkedList<Truck> moveQueue = new LinkedList<>();
        LinkedList<Truck> waitQueue = new LinkedList<>();


        for (int e : truck_weights) {
            waitQueue.add(new Truck(e));
        }

        int sumWeight = 0;

        while (!waitQueue.isEmpty() || !moveQueue.isEmpty()) {
            time += 1;

            if (moveQueue.isEmpty()) {
                Truck passingTruck = waitQueue.poll();
                sumWeight += passingTruck.weight;
                moveQueue.add(passingTruck);
                continue;
            }

            for (Truck truck : moveQueue) {
                truck.moving();
            }

            if (moveQueue.peek().getMove() > bridge_length) {
                Truck passedTruck = moveQueue.poll();
                sumWeight -= passedTruck.getWeight();
            }

            if (!waitQueue.isEmpty() && sumWeight + waitQueue.peek().getWeight() <= weight) {
                Truck passingTruck = waitQueue.poll();
                sumWeight += passingTruck.weight;
                moveQueue.add(passingTruck);
            }
        }

        return time;
    }
}