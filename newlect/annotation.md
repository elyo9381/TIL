# 애노테이션

## @Autowired

    spring에서 애노테이션의 기준은 무엇인가?

    객체들의 안을 들어다 보라는 xml의 설정은 <context:annotation-config/> 이다.
    빈들의 객체안을 뒤져서 @Autowired를찾는다.


    @Autowired는 프로퍼티를 대신해서 DI를 수행한다. 하지만 두개가같이 있을때는 어떻게 작동할것인가/??    
    ```
    <property name="Exam" ref="exam"/>
    <property name="Exam" ref="exam"/>
    ```   
    @Autowired는 클래스를 기준으로 Autowired를 수행한다.    
    클래스가 같을경우에는 id를 기준으로 찾게 된다.    
    이럴때 id까지 같을때는 @Qualifier("exam2") 라고 설정하면 id를 가지고 찾을수있다.


    @Autowired와 @Qualifier는 접근제시자에 적어도 된다. 왜냐하면 기본 생성자의 위치를 참고하기 때문이다. 

    @Autowired와 @Qualifier의 위치를 셋터위에 놓으면 셋터를 호출한다. 

    오버로드된 기본생성자가 있다면 기본생성자 또한 무조건 있어야한다. 

    오버로드된 생성자에는 @Qualifier()를 사용할수없다. 대신에 오버로드된 생성자의 인자에 사용할수있다.

    @Autowired(required = false) 옵션이 존재한다. 어떤 DI도 받지않았을대 즉 널일때이다.


    <context:annotation-config/>를 대신 할 수 있는것은   
    <context:componentscan base-package="me.elyowon"/>가 대신할수있다.   
    이때 @Component를 사용해서 코드상에서 객체를 생성할수있다.  


    ```
    ExamConsole console = (ExamConsole) ctx.getBean("console");
    ```   
    빈을 console이름으로 찾을 때 애노테이션을 가지고 찾으려면 @Component와 <context:componentscan base~~>를 이용해야한다. 이때 @Component("console")을 통해서 빈의 이름을 등록할수있다.

## @Component

    컴포넌트는 mvc방식으로 앱을 만들때 mvc를 구성하는 업무용 로직을 컴포넌트라고 한다.    

    ```
    <property name = "Exam" value = "40"/>
    <constructor-arg name="eng" type ="int" value="10"/>
    ```
    
    위와같이 밸류에 값을 넣으려고할때는 어떻게 넣을수있을까???    
    xml에서 값을 넣지 않고 애노테이션을 사용할때는 말이다.   
 
    ```
    @Value("20")
    private int kor;
    ```   


## @Configuration

    xml설정파일을 javaConfiguration파일로 바꿀수가있다.

    ```
    <beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    </beans>
    ⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎
    @Configuration
    public class NewlectDIConfig {
    
    }
    ```   


    ```
     <bean id="exam" class="main.java.me.elyowon.entity.NewlecExam" p:kor="1" p:eng="1"/>
    ⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎
    @Bean
        public Exam exam(){
            return new NewlecExam();
            }
    ```   
    로 바꿀수있다.   
    빈의 id는 메소드이름으로 바껴야한다.   

    ```
    <context:component-scan base-package="main.java.me.elyowon"/>
    ⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎⬇︎
    @ComponentScan
    ```   
    으로 변경가능하다.    

    

    ApplicationContext context = Classpath~~   
    ApplicationContext context = AnnotationConfigureationContext~~~    

    javaConfig 파일을 사용하면 Application 사용도 바꿔줘야한다.




