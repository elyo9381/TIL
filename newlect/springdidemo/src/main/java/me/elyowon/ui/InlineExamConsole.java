package main.java.me.elyowon.ui;


import main.java.me.elyowon.entity.Exam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("console")
public class InlineExamConsole implements ExamConsole {

    @Autowired
    private Exam exam;


    public InlineExamConsole(Exam exam) {
        this.exam = exam;
    }

    public InlineExamConsole() {

    }

    @Override
    public void print() {
        System.out.printf("total is %d avg is %f\n",exam.total(),exam.avg());
    }


    @Override
    public void setExam(Exam exam) {
        this.exam = exam;
    }
}