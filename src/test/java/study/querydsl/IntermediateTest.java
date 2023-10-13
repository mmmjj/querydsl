package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;


@SpringBootTest
@Transactional
public class IntermediateTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {//데이터insert
        System.out.println("==============BeforeEach==============");
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
    void simpleProjection() {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
        for(String s: result) {
            System.out.println(s);
        }
    }

    @Test
    void tupleProjection() {
        //tuple은 리포지토리안에서만 써서 모듈화하자
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for(Tuple t: result) {
            System.out.println(t.get(member.username));
            System.out.println(t.get(member.age));
        }

    }

    //프로잭션과 결과반환

    @Test
    void findDtoByJpql() {
        List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age)" +
                "from Member m", MemberDto.class)
                .getResultList();

        for(MemberDto d : result) {
            System.out.println(d);
        }
    }

    @Test
    void findDtoBDslProjection() {
        QMember memberSub = new QMember("memberSub");
        List<MemberDto> result =
                queryFactory
                        .select(
//                                Projections.bean(MemberDto.class, //setter 기본생성자필요함
//                                Projections.fields(MemberDto.class, //field
                                Projections.constructor(MemberDto.class, //field
                                        member.username,
                                        member.age,
                                        member.age.divide(10).as("ageRange"),
                                        ExpressionUtils.as(JPAExpressions
                                                .select(memberSub.age.max())
                                                .from(memberSub),"ageMax")
                                )
                        )
                        .from(member)
                        .fetch();

        for(MemberDto d : result) {
            System.out.println(d);
        }
    }

    @Test
    void findDtoByQueryProjection() {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age)) //constructor는 런타임에러때알지만 이건 컴파일시점때 알 수 있다
                .distinct()//distinct 하는법
                .from(member)
                .fetch();
        //가장 큰 단점은 @QueryProjection를 사용함으로써 querydsl에 의존성이 추가된점
        for(MemberDto d : result) {
            System.out.println(d);
        }
    }

    //동적쿼리

    @Test
    void dynamicQ_Bool() {
        String usernameParam = "member1";//
        Integer ageParam =10;//

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String username, Integer age) { //파람이 null 또는 notnull에 따라 동적으로 바뀌어야함

        //조건추출
        BooleanBuilder builder = new BooleanBuilder();
        if(username != null) {
            //파람이 널이 아니면 조건을만든다
            builder.and(member.username.eq(username));
        }

        if(age != null) {
            builder.and(member.age.eq(age));
        }

        return queryFactory
                .selectFrom(member)
                .where(
                        //조건넣기
                        builder
                )
                .fetch();
    }
    //where 다중파람사용 이게 좀더 깰꼼
    @Test
    void dynamicQ_where() {
        String usernameParam = "member1";//
        Integer ageParam = null;//

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }
    private List<Member> searchMember2(String username, Integer age) { //파람이 null 또는 notnull에 따라 동적으로 바뀌어야함

        return queryFactory
                .selectFrom(member)
                .where(allEq(username, age)) //리턴받은것중 null이면 무시된다
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return username != null ? member.username.eq(username) : null;
    }
    private BooleanExpression ageEq(Integer age) {
        return age != null ? member.age.eq(age) : null;
    }
    //두개조합가능 대신 Predicate->BooleanExpression을 사용해야함
    private BooleanExpression allEq(String username, Integer age) {
        return usernameEq(username).and(ageEq(age));
    }

    //벌크연산

    @Test
    void bulkUpdate() {
        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();
        //영속성컨택스트는 db에서 값을 가져오는최신데이터보다 우선권을 가지기 때문에 벌크연산후에는 애플리케이션내 데이터정합성을위해 clear하자
        em.flush();
        em.clear();
    }

    @Test
    void addAge() {
        long count = queryFactory
                .update(member)
                .set(member.age, member.age.multiply(1))//곱하기
//                .set(member.age, member.age.add(1))//더하기
                .execute();
        em.flush();
        em.clear();
    }

    @Test
    @Commit
    void bulkDelete() {
        long count = queryFactory
                .delete(member)
                .where(member.age.gt(20))
                .execute();
    }

    //함수호출
    @Test
    void sqlFunction() {
      List<String> result = queryFactory
              .select(
                      Expressions.stringTemplate(
                              "function('replace', {0}, {1}, {2})"
                                      , member.username, "member", "m"
                              )
              )
              .from(member)
              .fetch();
      for(String s : result) {
          System.out.println(s);
      }
    }

    @Test
    void sqlFunction2() {
        List<String> result = queryFactory
                .select(
                        /*Expressions.stringTemplate(
                                "function('upper', {0})"
                                , member.username
                        )*///jpql
                        member.username.lower()//dsl
                )
                .from(member)
                .fetch();
        for(String s : result) {
            System.out.println(s);
        }
    }
}
