package main.java.me.elyowon.aop;

import main.java.me.elyowon.NewlectDIConfig;
import main.java.me.elyowon.aop.entity.Exam;
import main.java.me.elyowon.aop.entity.NewlecExam;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AopProgram {
    public static void main(String[] args) {

        ApplicationContext ctx =
                new ClassPathXmlApplicationContext("main/java/me/elyowon/aop/setting.xml");
//        ApplicationContext ctx = new AnnotationConfigApplicationContext(NewlectDIConfig.class);

        Exam exam = (Exam) ctx.getBean("exam");

        System.out.printf("total is %d\n", exam.total());
        System.out.printf("total is %f\n", exam.avg());
////                   Proxy.newProxyInstance(loader, interfaces, h)
//        Exam proxy = (Exam) Proxy.newProxyInstance(NewlecExam.class.getClassLoader()
//                , new Class[]{Exam.class}
//                , new InvocationHandler() {
//                    @Override
//                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                        long start = System.currentTimeMillis();
//
////                      method.invoke(obj,args)
//                        Object result = method.invoke(exam, args);
//
//                        try {
//                            Thread.sleep(200l);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        System.out.println(System.currentTimeMillis()-start);
//
//                        return result;
//                    }
//                });

    }
}