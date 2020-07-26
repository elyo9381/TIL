# spring

## DI

```
스프링에게 지시하는 방법으로 코드를 변경 
		Exam exam = new NewlecExam();
//		ExamConsole console = new InlineExamConsole(exam); // DI
		ExamConsolse console = new GridExamConsole();
		console.setExam(exam);
		
		
		
		
		ExamConsole console = ?;
		console.print(); 
```
위와 같은 코드가 존재할때 
인스턴스 exam은 InlineExamConsole, GridExamConsole에서 파라미터로 들어간다 
인스턴스 exam에 의해서 두가지의 기능을 사용할수있는것이다. 
console 인스턴스는 exam인스턴스를 파라미터로 받은 두 new 클래스에서 출력 기능을 담당한다.
exam인스턴스는 각각의 new출력기능에 들어가므로 Dependency Injection을 허용하게 되는것이다. 


```
    <!-- Exam exam = new NewlecExam(); -->
	<bean id = "exam" class = "spring.di.entity.NewlecExam" />
	<!-- ExamConsole console = new GridExamConsole(); -->
	<bean id = "console" class = "spring.di.ui.GridExamConsole">
	<!-- console.setExam(exam);  -->
		<property name="exam" ref="exam"/>
	</bean>

```
Dependency Injection 지시서를 작성하는 xml의 일부분이다. 
bean에 등록할땐 id  = "인스턴스명" class ="패키지명을 포함한 레퍼클래스명"이다.
또한 DI를 통해서 작동한 레퍼클래스와 인스턴스는 id ="인스턴스 ", class = "DI를 통해서 작동한 레퍼클래스"을 등록하고
DI를 포함한 인스턴스는 게터같은 실행메소드가 존재한다. 이에 해당하는 DI지시자(bean)등록방법은
property 태그에 name에 해당하는곳에 name = "메소드의 이름" ref = "인스턴스명" value="인스턴스명"
ref, value둘중에서 인스턴스가 레퍼클래스를 통해서 만들어졌다면 레퍼, 밸류로 만들어졌다면 value를 등록한다.

인스턴스를 구성하는 new ~~클래스 interface를 가지는 클래스에서 실행을 위한 케터세터,투스트링이 존재해야한다. 