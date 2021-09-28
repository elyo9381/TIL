package hellojpa;

import org.hibernate.Criteria;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.rmi.MarshalledObject;
import java.time.LocalDateTime;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {

        //애플리케이션 로딩 시점에 딱 하나만 만들어야한다.
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        // DB에 저장하는 트랜잭션 할때마다 EntityManager를 생성해줘야한다
        //DB컨넥션 하나 받았다고 생각하면 된다.
        // 쓰레드간 공유 x(사용하고 버려야한다.)
        EntityManager em = emf.createEntityManager();
        //jpa의 모든 데이터 변경은 트랜잭션 안에서 실행해야한다.(중요)
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            // 1,2번 jpa 소개, 시작
            //생성
//            Member member = new Member();
//            member.setId(2L);
//            member.setName("HelloB");
//            em.persist(member);
            //조회
//            Member findMember = em.find(Member.class, 1L);
//            System.out.println("findMember.id : " + findMember.getId());
//            System.out.println("findMember.name : " + findMember.getName());
            // 삭제
//            em.remove(findMember);

            //수정
//            findMember.setName("HElloJAP");

            // jpql >> 현업에서 개발의 고민은 : 데이터를 정말 최적화 해서 가져와야하고 통계성 쿼리도 날려야하는데
            // 이것을 jpql이 도와준다.
//            List<Member> result = em.createQuery("select m from Member as m",Member.class)
//                    .getResultList();
//
//            for(Member member : result){
//                System.out.println("member.name = "+member.getName());
//            }

            // 3번 강의 영속성관리
            // 비영속
//            Member member  = new Member();
//            member.setName("HelloJPA");
//            member.setId(101L);
//
//            //영속
//            System.out.println("===before===") ;
//            em.persist(member);
//            System.out.println("===after===") ;
//            Member findMember = em.find(Member.class,101L);
//            System.out.println("findMember.id: " +findMember.getId());

            //아래와 같이 조회시 첫 findMember1은 쿼리를 날리지만 2은 쿼리를 날리지 않고 1차캐시에서 가져온다.
//            Member findMember1 = em.find(Member.class,101L);
//            Member findMember1 = em.find(Member.class,101L);

//
//            // 바로 쿼리를 날리지 않고 영속 컨테스트 내부에 저장후 커밋이 나오면 쿼리를 날림
//            Member member1 = new Member(150L,"A");
//            Member member2 = new Member(160L,"B");
//
//            //  why? 버퍼링 쓰기위해 >> 버퍼링은 최적화를 하기 위한 개념 >> jdbc.batch를 사용하게 됨
//            // 실시간 쿼리에서는 이점이 없을수도 있지만 기본적으로 이점을 가져가기 때문에 좋다.
//            em.persist(member1);
//            em.persist(member2);
//
//            System.out.println("-==============-");

//            Member member = em.find(Member.class,150L);
//            member.setName("ZZZZZ");
//            // em.persist(member); 하지 않는 이유는 자바 컬렉션 처럼 셋네임 하면 자동으로 저장된다.
//            // 이게 어떻게 내부적으로 지원되냐? >>  변경감지(dirty checking) 기능이 존재한다. >>  영속 컨테스트 내부적으로 커밋하면
//            // 플러시가 작동하고  >> 1차캐시안에 엔티티와 스냅샷(값을 읽어온 시점)을 비교하여 다르다면 업데이트 쿼리를 쓰기지연 sql저장소에 작성한다.
//            // 이러한 매커니즘을 통해서 진행된다.

//             엔티티 삭제
//                Member memberA = em.find(Member.class,"memberA");
//                em.remove(memberA) ;

            // 플러시

            // 준영속 상태들어가는 방법


            // 4강 필드와 컬럼매핑
//            Member member = new Member();
//            member.setId(3L);
//            member.setUsername("A");
//            member.setRoleType(RoleType.USER);
//            em.persist(member);

//             5강 연관관계 매핑



//            // 데이터 기반 쿼리 코드(연관관계 설정 x)
//            Member member = new Member();
//            member.setName("member1");
//            member.setTeamId(team.getId()); // 왜래키 식별자를 직접 다룸
//            em.persist(member);

//            Team team = new Team();
//            team.setName("TeamA");
////            team.getMembers().add(member);// mappedBy 의해서 주인이 아니다.
//            em.persist(team);
//
//            // 연관관계설정
//            Member member = new Member();
//            member.setName("memeber1");
////            member.changeTeam(team); // 멤버에 팀을 추가
//            em.persist(member);
//
//            team.addMember(member); // 팀에 멤버를 추가
//            // <양방향 객체 jpa 참조할시 둘중에 한곳에서만 하는게 났다. >
//
//            // 양쪽으로 데이터를 넣어야한다. 왜? 한쪽으로 넣으면 commit 날리기 전에
//            // 멤버에 팀을 넣거나 팀에 멤버를 넣을수없으므로
//            // 그렇다면 어떻게 해야하나? 양방향 편의 메소드를 생성하라
//
////            em.flush();
////            em.clear();
//
//            // 연관관계 설정시 그래프 탐색
//            Member findMember = em.find(Member.class,member.getId());
//            List<Member> members = findMember.getTeam().getMembers();
//
//            System.out.println("================");
//            for(Member m : members){
//                System.out.println("m = " + m.getName());
////                System.out.println("team name = " + m.getTeam().getName());
//            }
//            System.out.println("================");
//            // 가장 많이 하는 양방향 실수 to.string,loombok,json 생성라이브러리


            // 고급매핑 (정규화, 단일 테이블 전략 등등)
//
//            Movie movie = new Movie();
//            movie.setDirector("aaa");
//            movie.setActor("BBB");
//            movie.setName("바람과함께사라지다.");
//            movie.setPrice(1000);
//            em.persist(movie);
//            Movie findMovie = em.find(Movie.class,movie.getId());
//            System.out.println("findMovie = "+findMovie);

            //mappedsuperclass

//            Member member = new Member();
//            member.setName("hello");
//
//            em.persist(member);
//
//            em.flush();
//            em.clear();
//            //Member findMember = em.find(Member.class,member.getId());
//            Member findMember = em.getReference(Member.class,member.getId());
//            System.out.println("findMember = " + findMember.getName());

//            Child child1 = new Child();
//            Child child2 = new Child();
//
//            Parent parent = new Parent();
//            parent.addChild(child1);
//            parent.addChild(child2);
//
//            em.persist(parent);
//            em.persist(child1);
//            em.persist(child2);

            //값타입과 불변객체(임베디드 타입 사용)
//            Address address = new Address("city","street","10000");
//            Member member = new Member();
//            member.setName("aaa");
//            member.setHomeAddress(address);
//            em.persist(member);
//
//            Member member2 = new Member();
//            member2.setName("bbb");
//            member2.setHomeAddress(address);
//            em.persist(member2);
//
//            member.getHomeAddress().setCity("newCity");

            //JPQL
//            List<Member> result = em.createQuery(
//                    "select m from Member as m where m.name like '%kim%'",
//                    Member.class
//            ).getResultList();

            // jpql - criteria (하지만 실무에서는 잘 사용치 않는다.)
//            CriteriaBuilder cb = em.getCriteriaBuilder();
//            CriteriaQuery<Member> query = cb.createQuery(Member.class);
//
//            // 루트 클래스 (조회를 시작할 클래스)
//            Root<Member> m  = query.from(Member.class);
//
//            // 쿼리생성
//            CriteriaQuery<Member> cq = query.select(m).where(cb.equal(m.get("name"),"won"));
//            List<Member> resultList = em.createQuery(cq).getResultList();



            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();

    }

}