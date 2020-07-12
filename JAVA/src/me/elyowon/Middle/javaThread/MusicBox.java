package me.elyowon.Middle.javaThread;
/*
*   쓰레드와 상태제어
*   :
*
*
* */
public class MusicBox {
    int money = 0;

    public int getMoney() {
        return money;
    }

    //synchronized 모니터링 락이 되었다고 한다.
    public  void playMusicA(){
        for(int i = 0; i<10; i++){
            System.out.println("신나는음악");

            try {
                Thread.sleep((int)(Math.random()*1000));
                money +=2;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public  void playMusicB(){
        for(int i = 0; i<10; i++){
            System.out.println("sad 음악");

            try {
                Thread.sleep((int)(Math.random()*1000));
                money +=1;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public  void playMusicC(){
        for(int i = 0; i<10; i++){
            // 동기화 블럭을 통해서 메소드 전체가 아닌 원하는 부분만 동기화를 맞출수있는것을
            // 알수있다.
//            synchronized(this){
                System.out.println(" caffe 음악");

//            }

            try {
                Thread.sleep((int)(Math.random()*1000));
                money -=1;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}