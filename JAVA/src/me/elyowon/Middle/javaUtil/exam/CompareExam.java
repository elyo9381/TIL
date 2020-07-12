package me.elyowon.Middle.javaUtil.exam;

public class CompareExam {
    public static void exec(Compare compare){
        int k = 10;
        int m = 20;
        int value = compare.compareTo(k,m);
        System.out.println(value);
    }

    /*
    *   람다식 :
    *   ((매개변수) ->{});
    * */
    public static void main(String[] args) {
        exec((i,j) -> {
            return i -j;
        });
    }

}