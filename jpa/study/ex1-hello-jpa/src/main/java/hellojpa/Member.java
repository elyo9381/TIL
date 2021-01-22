package hellojpa;


import javax.persistence.Entity;
import javax.persistence.Id;

@Entity // table을 놓는다.
public class Member {

    @Id
    private Long id;
    private String name;

    //jpa는 생성자와 기본 생성자를 생성해줘야한다.
    public Member() {
    }

    public Member(Long id, String name) {
        this.id = id;
        this.name = name;
    }


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
}