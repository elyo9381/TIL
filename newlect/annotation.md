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





