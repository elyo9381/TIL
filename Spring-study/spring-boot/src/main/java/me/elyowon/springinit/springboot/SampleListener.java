package me.elyowon.springinit.springboot;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

// applicationContext가 실행되고 나서 빈으로 등록한 리스너가 실행된다.
// 하지만 ApplicationStartingEvent은 applicationContext가 실행되기 이전에 먼저 실행되므로
// 이러한 리스너들은 직접 따로 명시를 해줘야한다.


@Component
//public class SampleListener implements ApplicationListener<ApplicationStartedEvent> {
public class SampleListener implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("foo:"+ args.containsOption("foo"));
        System.out.println("bar:"+ args.containsOption("bar"));
    }


//    @Override
//    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
//        System.out.println("=======================");
//        System.out.println("Application is starting");
//        System.out.println("=======================");
//    }

//    public SampleListener(ApplicationArguments arguments){
//
//        System.out.println("foo:"+ arguments.containsOption("foo"));
//        System.out.println("bar:"+ arguments.containsOption("bar"));
//    }
}