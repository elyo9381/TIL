package me.elyowon.Middle.javaThread;

import java.security.cert.Extension;
// wait() 와 notify()메소드의 사용법
// 동기화를 시켜서 b.wait()를 진행하게 되면
// 메인 메소드는 멈추고 b.start()만 실행되게
// 쓰레드 b에서 notify()가 실행되어야 wait()가 풀리고
// 메인 쓰레드가 실행된다.
// wait() 와 notify() 사용하려면 동기화를 꼭 시켜야한다.

public class ThreadA {
    public static void main(String[] args) {
        ThreadB b = new ThreadB();
        b.start();
        synchronized (b){
            try{
                System.out.println("b가 완료될때까지 기다려라");
                b.wait();
            } catch (Exception e){
                e.printStackTrace();
            }
            System.out.println("total is : " + b.total);
        }
    }

}