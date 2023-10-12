package study.querydsl;


import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;


@SpringBootTest
@Transactional
@Commit
public class QTypeTest {

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

    @Test
    void search() { //검색조건쿼리
        System.out.println("====================query====================");
        Member findmember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10))) //and대신 , 사용가능
                .fetchOne(); //로그: select m1_0.member_id,m1_0.age,m1_0.team_id,m1_0.username from member m1_0 where m1_0.username='member1' and m1_0.age=10;

        assertThat(findmember.getUsername()).isEqualTo("member1");

        //검색관련
        /**
         * member.username.eq("member1") // username = 'member1'
         * member.username.ne("member1") //username != 'member1'
         * member.username.eq("member1").not() // username != 'member1'
         * member.username.isNotNull() //이름이 is not null
         * member.age.in(10, 20) // age in (10,20)
         * member.age.notIn(10, 20) // age not in (10, 20)
         * member.age.between(10,30) //between 10, 30
         * member.age.goe(30) // age >= 30
         * member.age.gt(30) // age > 30
         * member.age.loe(30) // age <= 30
         * member.age.lt(30) // age < 30
         * member.username.like("member%") //like 검색
         * member.username.contains("member") // like ‘%member%’ 검색
         * member.username.startsWith("member") //like ‘member%’ 검색
         */
    }

    @Test
    void searchWithComma() { //검색조건쿼리
        System.out.println("====================query====================");
        Member findmember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1")
                        ,member.age.eq(10)//and대신 , 사용가능, null은 무시
                )
                .fetchOne(); //로그: select m1_0.member_id,m1_0.age,m1_0.team_id,m1_0.username from member m1_0 where m1_0.username='member1' and m1_0.age=10;

        assertThat(findmember.getUsername()).isEqualTo("member1");
    }

    @Test
    void returnType() {
        System.out.println("====================query====================");
        //List
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        System.out.println("====================query====================");
        //단 건
        Member findMember1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("====================query====================");
        //처음 한 건 조회
        Member findMember2 = queryFactory
                .selectFrom(member)
                .fetchFirst();

        System.out.println("====================query====================");
        //페이징에서 사용
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults(); //쿼리 두번날림, 토탈카운트때문에

        System.out.println("====================query====================");
        //count 쿼리로 변경
        long count = queryFactory
                .selectFrom(member)
                .fetchCount();
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));
        System.out.println("====================query====================");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        System.out.println("====================query====================");
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //0부터 시작(zero index)
                .limit(2) //최대 2건 조회
                .fetch();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {//전체조회, 복잡한쿼리는 카운팅쿼리는 간단한쿼리로 호출하자
        System.out.println("====================query====================");
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }


    /**
     * JPQL
     * select
     * COUNT(m), //회원수
     * SUM(m.age), //나이 합
     * AVG(m.age), //평균 나이
     * MAX(m.age), //최대 나이
     * MIN(m.age) //최소 나이
     * from Member m
     */
    @Test
    public void aggregation() throws Exception { //그룹함수
        System.out.println("====================query====================");
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();
        Tuple tuple = result.get(0);//여러개타입이있을때 가져올수있는것
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    //그룹by
    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() throws Exception {
        System.out.println("====================query====================");
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
//                .having() group 조건절
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    //조인 join(조인 대상, 별칭으로 사용할 Q타입)
    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() throws Exception {
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        System.out.println("====================query====================");
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team) //join(조인 대상, 별칭으로 사용할 Q타입)
                .where(team.name.eq("teamA"))
                .fetch();
        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인(연관관계가 없는 필드로 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     * from절에 여러 엔티티 선택해서 가능하다
     * 외부조인은 불가능하다
     */
    @Test
    public void theta_join() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch(); // m1_0.member_id,m1_0.age,m1_0.team_id,m1_0.username from member m1_0,team t1_0 where m1_0.username=t1_0.name;
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    //join - on절 jpa 2.1부터 지원,
    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and
     t.name='teamA'
     */
    @Test
    public void join_on_filtering() throws Exception {
        System.out.println("====================query====================");
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))//member는 다가져오고 team은a만가져오는것,
                //join ( aka.innerjoin)에 on조건을 넣으면 where절에 조건을 넣는것과 같다
                .fetch();
        //jpql 실로그
        /* select
        member1,
        team
    from
        Member member1
    left join
        member1.team as team with team.name = ?1 */
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);

        }
        /**
         * tuple = [Member(id=1, username=member1, age=10), Team(id=1, name=teamA)]
         * tuple = [Member(id=2, username=member2, age=20), Team(id=1, name=teamA)]
         * tuple = [Member(id=3, username=member3, age=30), null]
         * tuple = [Member(id=4, username=member4, age=40), null]
         */
    }

    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
     */
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member) //team빼고
                .leftJoin(team)//member.team이 아니라 team
                .on(member.username.eq(team.name))//join 조건추가
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("t=" + tuple);
        }
    }

    //패치조인

    @PersistenceUnit
    EntityManagerFactory emf;//로딩된엔티티인지초기화안된엔티티인지알수있음

    //패치조인미적용
    @Test
    public void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();//패치조인은 깔끔하지않으면 제대로 된  결과보기가 어렵다

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        /*
    select
        member1
    from
        Member member1
    where
        member1.username = ?1 */
        boolean loaded =
                emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }
    //패치조인적용

    @Test
    public void fetchJoinUse() throws Exception {
        em.flush();
        em.clear();
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .fetchJoin()//패치조인적용
                .where(member.username.eq("member1"))
                .fetchOne();
    /*
    select
        member1
    from
        Member member1
    inner join
        fetch member1.team as team
    where
        member1.username = ?1 */
        boolean loaded =
                emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    //서브쿼리
    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * 나이가 평균 나이 이상인 회원
     */
    @Test
    public void subQueryGoe() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(30,40);
    }

    //dsl은 jpql빌더이고, jpql의 한계점으로 from 절에 서브쿼리가안된다, dsl도 마찬가지로안됨 --> 서브쿼리를 조인으로, 쿼리2번실행, 네이티브쿼리사용
    @Test
    public void subQueryOther() throws Exception {

        /**
         * 서브쿼리 여러 건 처리, in 사용
         */
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);

//        select 절에 subquery
        List<Tuple> fetch = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ).from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " +
                    tuple.get(JPAExpressions.select(memberSub.age.avg())
                            .from(memberSub)));
        }

        //    static import 활용
        List<Member> staticResult = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();


    }

    //케이스
    @Test
    void baseCase() {
//        단순한 조건
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        for(String s: result) {
            System.out.println("s= " + s);
        }
//        복잡한 조건
        List<String> result2 = queryFactory
                .select(new CaseBuilder() //복잡한조건일때 빌더쓴다
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for(String s: result2) {
            System.out.println("s2= " + s);
        }
    }

    //상수
    @Test
    void constant() {
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch(); //fetchFirst();
        for(Tuple tuple : result) {
            System.out.println(tuple);
        }
    }
    //enum더할떄(문자열더하기)
    @Test
    void concat() {
        List<String> result = queryFactory
                .select(
                        member.username
                                .concat("_")
                                .concat(member.age.stringValue())//stringvalue는 문자열로 바꿔준다
                )
                .from(member)
                .fetch(); //fetchFirst();
        for(String s : result) {
            System.out.println(s);
        }
    }





}
