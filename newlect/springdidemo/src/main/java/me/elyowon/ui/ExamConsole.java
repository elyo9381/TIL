package main.java.me.elyowon.ui;


import main.java.me.elyowon.entity.Exam;
import org.springframework.stereotype.Component;

@Component
public interface ExamConsole {
    void print();

    void setExam(Exam exam);
}
