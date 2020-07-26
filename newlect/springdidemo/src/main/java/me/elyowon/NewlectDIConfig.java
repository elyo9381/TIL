package main.java.me.elyowon;

import main.java.me.elyowon.entity.Exam;
import main.java.me.elyowon.entity.NewlecExam;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan
@Configuration
public class NewlectDIConfig {
    @Bean
    public Exam exam(){
        return new NewlecExam();
    }
}