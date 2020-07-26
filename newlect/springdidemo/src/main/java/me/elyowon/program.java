package main.java.me.elyowon;

import main.java.me.elyowon.entity.Exam;
import main.java.me.elyowon.entity.NewlecExam;
import main.java.me.elyowon.ui.ExamConsole;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class program {

    public static void main(String[] args) {
        // exam는 인터페이스이고 이를 구현하는 부분이 NewlectExam이다.
        // 이를 주입하여서 각각의 기능을 사용할수있다.

        // 스프링에게 지시하는 방법으로 코드를 변경
//        Exam exam = new NewlecExam();
//        ExamConsole console = new InlineExamConsole(); // DI
////        ExamConsole console = new GridExamConsole(exam);
//        console.setExam(exam);

//        ApplicationContext ctx =
//                new ClassPathXmlApplicationContext("main/java/me/elyowon/setting.xml");

        ApplicationContext ctx = new AnnotationConfigApplicationContext(NewlectDIConfig.class);

        // IOC컨테이너에서 빈을 가져오는 두가지 방법
        // 아래의 방법이 선호되는 방법이다.
        ExamConsole console = (ExamConsole) ctx.getBean("console");
//        ExamConsole console = ctx.getBean(ExamConsole.class);
//        System.out.println(console.toString());
        console.print();


////        List<Exam> exams = new ArrayList<>();
//        List<Exam> exams = (List<Exam>) ctx.getBean("exams");
////        exams.add(new NewlecExam(1,1,1,1));
//
//        for(Exam e: exams){
//            System.out.println(e);
//        }
    }
}
