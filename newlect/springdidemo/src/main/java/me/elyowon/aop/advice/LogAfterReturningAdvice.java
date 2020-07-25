package main.java.me.elyowon.aop.advice;

import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

public class LogAfterReturningAdvice implements AfterReturningAdvice {

    // Object returnValue 반환값을 준다.
    //
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        System.out.println("returnValue :" +returnValue+", " +
                         "methond : " +method.getName());
    }
}