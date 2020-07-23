# Spring

## DI(Dependency Injection)

composition has a
```
class A
public A(){
 private B b;
    b = new B();
}
```
일체형


Association has a
```
class A {
    private B b;
    public A(){

    }
    public void setB(B b){
        this.b = b;
    }
}

B b = new B(); // Dependency
A a = new A();

a.setB(b); // b : Injection
```
조립형

일체형보단 조립형이 많이 사용된다. (유지보수를 생각한다면)

DI의 방법은 두가지가 있다. Setter Injection, Construction Injection

Setter Injection
```
B b = new B();
A a = new A();

a.setB(b);
```

Construction Injection
```
B b = new B();
A a = new A(b);
```
위와 같은 일들을 Spring이 해주고 이것을 DI라고 한다. 속된표현으로는 부품을 조힙해주는 느낌이다.
<종속성주입> Dependency Injection
