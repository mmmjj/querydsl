package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

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

}