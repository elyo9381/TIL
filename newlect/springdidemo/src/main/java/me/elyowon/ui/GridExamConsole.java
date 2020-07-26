package main.java.me.elyowon.ui;


import main.java.me.elyowon.entity.Exam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GridExamConsole implements ExamConsole {

    private Exam exam;

    public GridExamConsole(Exam exam) {
        this.exam = exam;
    }

    public GridExamConsole() {

    }

    @Override
    public void print() {
        System.out.println("그리드");
    }

    @Autowired
    @Override
    public void setExam(Exam exam) {
        this.exam = exam;
    }
}