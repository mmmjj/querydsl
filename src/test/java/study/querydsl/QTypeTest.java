package study.querydsl;


import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.*;


@SpringBootTest
@Transactional
@Commit
public class QTypeTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {//데이터insert

        queryFactory = new JPAQueryFactory(em);
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
        /*List<Member> members = em.createQuery("select m from Member m",
                        Member.class)
                .getResultList();
        for (Member member : members) {
            System.out.println("member=" + member);
            System.out.println("-> member.team=" + member.getTeam());
        }*/
    }

    @Test
    void start() { //결론: q타입은 static import해서 쓰면된다
        System.out.println("==============start==============");
        //member1을 찾아라
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member2"))
                .fetchOne(); //dsl은 결국 jpql이 된다

        QMember m2 = new QMember("m2");//Member 테이블 alias 지정할수있음

        assertThat(findMember.getUsername()).isEqualTo("member2");
    }
}
