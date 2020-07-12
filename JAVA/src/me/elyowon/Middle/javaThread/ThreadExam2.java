package me.elyowon.Middle.javaThread;

public class ThreadExam2 {
    public static void main(String[] args) {
        MyThread2 t1 = new MyThread2("*");
        MyThread2 t2 = new MyThread2("-");


        // Runable을 이용할때는 인스턴스에서 바로 thread strat()메소드를 사용하지 못한다.
        // 쓰레드 인스턴스(객체)를 만들어서 실행할 쓰레드를 주입한 후에
        // 쓰레드 인스턴스에서 start()메소드를 사용할수있다.

        Thread thread1  = new Thread(t1);
        Thread thread2 = new Thread(t2);

        thread1.start();
        thread2.start();
    }

}