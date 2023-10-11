package study.querydsl.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)//기본생성자를 여러곳에서 호출하는것방지
@ToString(of={"id","username","age"}) //team은 무한루프돌수있으므로 
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)//n:1은 lazy로 명시
    @JoinColumn(name = "team_id")//외래키설정
    private Team team;
    
    public Member(String username) {
        this(username, 0);
    }
    
    public Member(String username, int age) {
        this(username, age, null);
    }
    
    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if(team != null) {
            changeTeam(team);
        }
    }
    
    public void changeTeam(Team team) {//객체지향, 일종의패턴, member는member안에서 처리하자
        this.team = team;
        team.getMembers().add(this);//TODO 이것도복습..
    }
}
