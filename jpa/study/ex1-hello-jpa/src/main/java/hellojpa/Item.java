package hellojpa;

import javax.persistence.*;

//@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 단일 테이블
//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS) // 클래스별로 item의 속성이 중복되어 테이블 생성
@Entity
@Inheritance(strategy = InheritanceType.JOINED) // 정규화 시킨는 전략
@DiscriminatorColumn // table에 Dtype을 설정한다.
public abstract class Item { //table_per_class 를 설정시 상속 클래스를 추상클래스로 만들어야한다.

    @Id @GeneratedValue
    private Long id;

    private String name;
    private int price;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}