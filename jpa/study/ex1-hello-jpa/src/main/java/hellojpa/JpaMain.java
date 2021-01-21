package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
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
            List<Member> result = em.createQuery("select m from Member as m",Member.class)
                    .getResultList();

            for(Member member : result){
                System.out.println("member.name = "+member.getName());
            }


            tx.commit();
        } catch (Exception e){
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();

    }
}