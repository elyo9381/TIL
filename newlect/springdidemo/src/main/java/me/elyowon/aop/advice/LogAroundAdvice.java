package main.java.me.elyowon.aop.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class LogAroundAdvice implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        long start = System.currentTimeMillis();

        Object result = methodInvocation.proceed();

        try {
            Thread.sleep(200l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println((System.currentTimeMillis()-start)+"m/s 시간이 걸렸습니다.");
        return result;
    }
}