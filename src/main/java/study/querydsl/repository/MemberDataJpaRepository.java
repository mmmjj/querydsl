package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsl.entity.Member;

import java.util.List;

public interface MemberDataJpaRepository extends JpaRepository<Member, Long> {//c1 상속추가

    List<Member> findByUsername(String username);//c2 조회코드추가, 메서드명을 자동으로 jpql로 만들어줌
    //c3 jpql 아마 select m from Member where username = :username
    //c4 aka.이런 메서드는 정적쿼리


}
