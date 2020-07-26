# AOP

>AOP->Spring MVC->Spring Mybatis->Spring Transaction->Spring Security

## AOP(Aspect Oriented Prigramming) 이해하기

시용자의 요구가 주업무로직이라고 말할때   
개발자 혹은 운영자가 필요해 의해서 추가하는 업무로직   

각자의 측면을 고려한 프로그래밍이 AOP입니다.   


AOP는 관점에 해당하는 코드들을 반복해서 사용하지 않는다.

cross-cutting Concern, core Concern

다음과 같은 업무 로직이 존재한다. 이때 result 전후로 시간을 찍는 코드를 추가하고 싶을때   
AOP를 사용한다.

```
@Override
public int total() {
    long start = System.currentTimeMillis();

    int result= kor+eng+math+com;

    return result;
}

@Override
public float avg() {
    float total =total() / 4.0f;
    return total;
}
```


```
public class AopProgram {
    public static void main(String[] args) {
        Exam exam = new NewlecExam(1,1,1,1);
//                   Proxy.newProxyInstance(loader, interfaces, h)
        Exam proxy = (Exam) Proxy.newProxyInstance(NewlecExam.class.getClassLoader()
                , new Class[]{Exam.class}
                , new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        long start = System.currentTimeMillis();
//                      method.invoke(obj,args)
                        Object result = method.invoke(exam, args);
                        try {
                            Thread.sleep(200l);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println(System.currentTimeMillis()-start);
                        return result;
                    }
                });
        System.out.printf("total is %d\n", proxy.total());
        System.out.printf("total is %f\n", proxy.avg());
    }
}
```    
위의 코드는 AOP를 사용하기 위한 자바 코드이다.   
Proxy.newProxyInstance()를 생성하여 프록시를 날린다.   
이때 newProxyInstance()의 인자로 loader, interfaces, Handler를 받고   
loader는 클래스의 getClassLoader()를 통해서 가져올수있으며   
interfaces는 인터페이스가 여러개이므로 Class[]{Exam.class}를 동적 할당 받아서 가져올수있다.   
Handler는 InvocationHandler()를 통해서 받아온다.   

InvocationHandler()는  invokes는 업무객체를 넣어주고 호출한 메소드의 파라미터를 받아온다


Before / AfterReturnning / After throwing / Around /

Before : 앞에만 필요한 서브로직   
After returnning : 뒤에만 필요한 서브로직
After throwing : 예외를 처리하는 서브로직
Around : 앞뒤로 필요한 서브로직 



## 스프링을 이용한 AOP

  아래의 코드는 xml파일을 통해서 proxy를 구성하는 코드이다.   
  메인 로직이 동작할 빈을 등록한다. 빈이름을 (id = "target") 으로 설정한 이유는 서브로직이 수행되는곳에서 메인로직을 참조해야하기 때문이다.    
  서브로직이 동작할 빈을 등록한다. 이때 서브로직이 수행될 클래스가 존재해야한다.    
  그리고 코드상에서 동작할 빈을 등록하여 빈을 불러왔을때 수행될수있도록 빈을 등록한다.    
  property TAG를 이용하여 셋터를 통해 (name = "target"을 통해서) 빈이름이 target인것을 찾고 ref를 통해서 target을 찾는다.   
  그리고 핸들러를 등록하는데 여러개를 등록할수있으므로 list TAG를 사용한다.   
  ```
<bean id ="target" class="main.java.me.elyowon.aop.entity.NewlecExam" p:kor="1" p:eng="1" p:math="1" p:com="1"/>
<bean id ="LogAroundAdvice" class="main.java.me.elyowon.aop.advice.LogAroundAdvice"/>
<bean id = "exam" class="org.springframework.aop.framework.ProxyFactoryBean" >
    <property name="target" ref="target"/>
    <property name="interceptorNames">
        <list>
            <value>LogAroundAdvice</value>
        </list>
    </property>
</bean>
  ```

    서브로직을 사용하기 위한 클래스를 정의하였다.   
  ```
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
        System.out.println(System.currentTimeMillis()-start);
        return result;
    }
}
  ```

    빈을 불러와서 IoC작동으로 동작하는 main문의 내용이다.    
  ```
ApplicationContext ctx = 
    new ClassPathXmlApplicationContext("main/java/me/elyowon/aop/setting.xml");
//        ApplicationContext ctx = new AnnotationConfigApplicationContext(NewlectDIConfig.class);

Exam exam = (Exam) ctx.getBean("exam");

System.out.printf("total is %d\n", exam.total());
System.out.printf("total is %f\n", exam.avg());
  ```

### AOC의 전체적인 순서
 1. main문에서 applicationContext를 통해서 xml파일을 IoC컨테이너를 불러와야한다. 
 2. IoC컨테이너에서 빈을 불러와서 IoC기능을 수행해야한다. 그러므로 IoC컨테이너에 프록시를 위한 빈이 등록되어있어야한다. 
 3. IoC컨테이너에 메인로직이 수행될 빈을 등록하고 서브로직이 등록될 빈을 등록해야한다. 
 4. 그리고 코드상에서 빈을 불러왔을때 동작할수있는 빈을 설정한다. 
 5. 4번의 빈에서 동작할 property를 작성한다.
 6. 프로퍼티는 메인이 동작할 target을 셋터로 부르고 ref= target임을 확인하며 빈을 DI한다.
 7. 서브로직 또한 작동되야 하므로 프로퍼티 태그를 통해서 핸들러를 불러온다. >>interceptorNames
 8. 핸들러 interceptorNames는 복수이므로 리스트태그를 사용해서 서브로직의 빈을 DI한다.

```
<bean id ="LogAroundAdvice" class="main.java.me.elyowon.aop.advice.LogAroundAdvice"/>
<bean id ="LogBeforeAdvice" class = "main.java.me.elyowon.aop.advice.LogBeforeAdvice"/>
<bean id ="LogAfterReturningAdvice" class = "main.java.me.elyowon.aop.advice.LogAfterReturningAdvice"/>
<bean id ="LogAfterThrowingAdvice" class = "main.java.me.elyowon.aop.advice.LogAfterThrowingAdvice"/>
```

advice를 4가지 케이스를 확인해볼수잇었다.   

```
앞에서 실행될 로직입니다 target.NewlecExam{kor=23, eng=1, math=1, com=1}
앞에서 실행될 로직입니다. objects [Ljava.lang.Object;@7161d8d1
returnValue :26, methond : total
216m/s 시간이 걸렸습니다.
total is 26
```   
출력결과를 나타냅니다.   

Object[] objects <<< 이것은 오브젝트형 배열안에 존재하는 값들이 있다.   
?? 여러가지 메소드가 존재하기 때문에    


## pointcut(Weaving, joincut)

  pointcut : 특정 메소드만 조인하게 하고 싶을 때 특정 정보가 필요한데 그것을 포인트컷이라고 한다. 
  joinpoint : 조인하게 될 메소드 
  weaving :  cross-cutting이 실행되고 core concern이 실행되는것을 뜨개질하는것에 비유하였고 이것이 위빙이다. 

  프록시는 메인로직을 수행하여야하고 서브로직을 수행하여야한다. 그것을 crossConcern과 coreConcern을 수행하는것이고 수행을 순차적으로 하는것 즉 cross -> core 순으로 시작하는것을 weaving이라고 하며 
  프록시는 각각의 coreConcern을 joinpoint로 생각한다. 이때 특정한 joinpoint만 생성하려고 할때 pointcut을 설정한다. 


  ```
<bean id ="target" class="main.java.me.elyowon.aop.entity.NewlecExam" p:kor="23" p:eng="1" p:math="1" p:com="1"/>
<bean id ="LogAroundAdvice" class="main.java.me.elyowon.aop.advice.LogAroundAdvice"/>
<bean id ="LogBeforeAdvice" class = "main.java.me.elyowon.aop.advice.LogBeforeAdvice"/>
<bean id ="LogAfterReturningAdvice" class = "main.java.me.elyowon.aop.advice.LogAfterReturningAdvice"/>
<bean id ="LogAfterThrowingAdvice" class = "main.java.me.elyowon.aop.advice.LogAfterThrowingAdvice"/>

<bean id = "classicPointcut" class = "org.springframework.aop.support.NameMatchMethodPointcut">
<property name="mappedName" value="total"/>
</bean>

<!--    포인트컷과 LogBeforeAdvice 연결-->
<bean id ="classicBeforeAdvisor" class =" org.springframework.aop.support.DefaultPointcutAdvisor">
    <property name="advice" ref="LogBeforeAdvice"/>
    <property name="pointcut" ref="classicPointcut"/>
</bean>

<!--    포인트컷과 LogBeforeAdvice 연결-->
<bean id ="classicAroundAdvisor" class =" org.springframework.aop.support.DefaultPointcutAdvisor">
    <property name="advice" ref="LogAroundAdvice"/>
    <property name="pointcut" ref="classicPointcut"/>
</bean>

<bean id = "exam" class="org.springframework.aop.framework.ProxyFactoryBean" >
<property name="target" ref="target"/>
<property name="interceptorNames">
    <list>
        <value>classicAroundAdvisor</value>
        <value>classicBeforeAdvisor</value>
        <value>LogAfterReturningAdvice</value>
        <value>LogAfterThrowingAdvice</value>
    </list>
</property>
</bean>
  ```   


포인터컷을 xml에서 설정하기 위해서는 설정이 필요하다.   
우선 ! 포인트 컷을 빈으로 등록해야한다. 
< bean id = "classicPointcut" class = "org.springframework.aop.support.NameMatchMethodPointcut"/>   
이때 프로퍼티를 이용해서 mappedName이라는 셋터를 이용해서 value="total"을 설정하고 pointcut을 value만 사용한다고 명시해준다.    

다음으로는 포인트컷과 Advice를 연결해야한다.   
연결은 위해 빈을 등록한다.  Advisor를 사용한다고 말한다. 
< bean id ="classicBeforeAdvisor" class =" org.springframework.aop.support.DefaultPointcutAdvisor"/>   
그리고 advice 메소드를  LogAroundAdvice가 참조하게 하고 pointcut 포인트컷기능을 빈으로 등록한 classicPointcut을 참조하게 한다.   

위의 순서로 포인트컷과 조인포인트와 연결을 시켰고 조인포인트에 pointcut을 사용한다고 명시해주면 포인트컷 사용이 완료된다. 

```
<bean id = "exam" class="org.springframework.aop.framework.ProxyFactoryBean" >
    <property name="target" ref="target"/>
    <property name="interceptorNames">
        <list>
            <value>classicAroundAdvisor</value>
            <value>classicBeforeAdvisor</value>
            <value>LogAfterReturningAdvice</value>
            <value>LogAfterThrowingAdvice</value>
        </list>
    </property>
</bean>
```





```
<!--    <bean id = "classicPointcut" class = "org.springframework.aop.support.NameMatchMethodPointcut">-->
<!--        <property name="mappedName" value="total"/>-->
<!--    </bean>-->

<!--&lt;!&ndash;    포인트컷과 LogBeforeAdvice 연결&ndash;&gt;-->
<!--    <bean id ="classicBeforeAdvisor" class =" org.springframework.aop.support.DefaultPointcutAdvisor">-->
<!--            <property name="advice" ref="LogBeforeAdvice"/>-->
<!--            <property name="pointcut" ref="classicPointcut"/>-->
<!--    </bean>-->

<!--    <bean id ="classicBeforeAdvisor" class =" org.springframework.aop.support.NameMatchMethodPointcutAdvisor">-->
<!--        <property name="advice" ref="LogBeforeAdvice"/>-->
<!--        <property name="mappedNames" >-->
<!--            <list>-->
<!--                <value>total</value>-->
<!--                <value>avg</value>-->
<!--            </list>-->
<!--        </property>-->
<!--    </bean>-->

<bean id ="classicBeforeAdvisor" class =" org.springframework.aop.support.RegexpMethodPointcutAdvisor">
    <property name="advice" ref="LogBeforeAdvice"/>
    <property name="patterns" >
        <list>
            <value>.*to.*</value>
        </list>
    </property>
</bean>


<bean id ="classicAroundAdvisor" class =" org.springframework.aop.support.NameMatchMethodPointcutAdvisor">
        <property name="advice" ref="LogAroundAdvice"/>
        <property name="mappedName" value="total"></property>
</bean>


<bean id = "exam" class="org.springframework.aop.framework.ProxyFactoryBean" >
    <property name="target" ref="target"/>
    <property name="interceptorNames">
        <list>
            <value>classicAroundAdvisor</value>
            <value>classicBeforeAdvisor</value>
            <value>LogAfterReturningAdvice</value>
            <value>LogAfterThrowingAdvice</value>
        </list>
    </property>
</bean>

```







