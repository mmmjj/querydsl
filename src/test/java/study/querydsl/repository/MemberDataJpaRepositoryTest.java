package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberDataJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberDataJpaRepository memberDataJpaRepository;

    @Test
    void basicTest() {
        Member member = new Member("member1", 10);
        memberDataJpaRepository.save(member);

        Member findMember = memberDataJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberDataJpaRepository.findAll();
        assertThat(result1).containsExactly(member);


        List<Member> result2 = memberDataJpaRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @BeforeEach
    public void before() {//데이터insert
        System.out.println("==============BeforeEach==============");
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
    public void searchTest() {

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(50);
        condition.setTeamName("teamB"); //조건하나도없으면 all

        /*List<MemberTeamDto> result = memberDataJpaRepository.searchByBuilder(condition);
        for(MemberTeamDto m : result) {
            System.out.println("========================");
            System.out.println(m);
        }*/

        List<MemberTeamDto> result2 = memberDataJpaRepository.search(condition);
        for(MemberTeamDto m : result2) {
            System.out.println("========================");
            System.out.println(m);
        }



    }

}