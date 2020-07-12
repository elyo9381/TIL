package me.elyowon.Middle.javaThread;

public class MusicBoxExam1 {
    public static void main(String[] args) throws InterruptedException {
        MusicBox box = new MusicBox();

        System.out.println("시작");

        MusicPlayer m1 = new MusicPlayer(1,box);
        MusicPlayer m2 = new MusicPlayer(2,box);
        MusicPlayer m3 = new MusicPlayer(3,box);

        m1.start();
        m2.start();
        m3.start();

        // 조인을 통해서 메인 메소드를 기다리라고 할수있다.
        // m1이 멈출때까지 join을 사용한 쓰레드가 기다리는것이다.
        try {
            m1.join();
            m2.join();
            m3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(m1.musicBox.getMoney());

        System.out.println("종료");
    }

}