package hellojpa;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

//@Entity // table을 놓는다.
//@Table(name = "MBR") // 데이터베이스 테이블명을 MBR로 나가게 된다.
// 유니크 제약조건의 이름까지 줄수있다.
// @Table(uniqueConstraints = {@UniqueConstraint( name = "NAME_AGE_UNIQUE", columnNames = {"NAME", "AGE"} )})
// DDL생성 기능은 DDL을 자동 생성할 때만 사용되고 jpa의 실행 로직에는 영향을 주지 않는다.
@Entity
public class Member extends BaseEntity {

    @Id
    @GeneratedValue()
    private Long id;

    @Column(name = "USERNAME")
    private String name;

    // 기간
    @Embedded
    private Period workPeriod;

    // 주소
    @Embedded
    private Address homeAddress;

    public Period getWorkPeriod() {
        return workPeriod;
    }

    public void setWorkPeriod(Period workPeriod) {
        this.workPeriod = workPeriod;
    }

    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

    //    데이터기반의 설계
//    @Column(name = "TEAM_ID")
//    private Long teamId;

    @ManyToOne(fetch = FetchType.LAZY) // 프록시로 가져온다.
    @JoinColumn(name = "TEAM_ID")
    private Team team;


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

//    public Long getTeamId() {
//        return teamId;
//    }
//
//    public void setTeamId(Long teamId) {
//        this.teamId = teamId;
//    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void changeTeam(Team team) {
        this.team = team;
        // 양방향 편의메소드 주인에서 팀추가
        team.getMembers().add(this);
    }

}
