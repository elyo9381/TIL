package me.elyowon.Middle.javaUtil.exam;

public class BoxExam {
    public static void main(String[] args) {
//        Box box = new Box();
//
//        box.setObj(new Object());
//        Object obj = box.getObj();
//
//        box.setObj("반가워여");
//        String str = (String)box.getObj();
//        System.out.println(str);
//
//        box.setObj(1);
//        int i = (int)box.getObj();
//        System.out.println(i);

        Box<Object> box = new Box<>();
        box.setObj(new Object());
        Object obj = box.getObj();

        Box<String> box2= new Box<>();
        box2.setObj("new string");
        System.out.println(box2.getObj());

        Box<Integer> box3 = new Box<>();
        box3.setObj(1);
        int i = box3.getObj();
    }
}