package me.elyowon.Middle.javaThread;

public class DeamonThread implements Runnable{

    @Override
    public void run() {
        while(true){
            System.out.println("데몬쓰레드 실행중입니다.");


            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }


    public static void main(String[] args) {
        Thread thread = new Thread(new DeamonThread());
        // 데몬쓰레드로 설정된다. setDeamon();
        thread.setDaemon(true);
        thread.start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("메인이종료되었습니다.");
    }
}