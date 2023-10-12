package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;//스프링프레임워크가 주입해주는 엔티티매니저는 멀티스레드에 문제없이설계가 되어있음

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {//데이터insert

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        //초기화
        em.flush();
        em.clear();
        //확인
        List<Member> members = em.createQuery("select m from Member m",
                        Member.class)
                .getResultList();
        for (Member member : members) {
            System.out.println("member=" + member);
            System.out.println("-> member.team=" + member.getTeam());
        }
    }

    @Test
    void startJPQL() {
        //member1찾기
        String qlString =
                "select m from Member m " +
                "where username = :username"; //jpql 오타는 런타임익셉션이 나야 알수있음
        Member findMember = em.createQuery(qlString,Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    void startQuerydsl() {
        //querydsl 아래부터 dsl
        //dsl을 짜려면 우선jpaqueryfactory로 시작해야한다
        queryFactory = new JPAQueryFactory(em); //첨언1이건 beforeeach에 필드레벨로 두고 beforeeach에두고 해당라인 없어져도 됨
        //그리고 컴파일하고 q엔티티파일을 사용한다
        QMember m = new QMember("m"); //q멤버 이름을 m으로 지정
        QMember q = QMember.member; //

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) //prepare statement에 파라미터파인딩 방식을 적용함
                .fetchOne();//dsl은 컴파일시점때 오타를 알수있음

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }
}
