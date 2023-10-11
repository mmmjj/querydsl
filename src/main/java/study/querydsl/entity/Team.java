package study.querydsl.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {

    @Id @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    private String name;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "team")이게 왜아니지..? TODO 복습..
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>(); //private List<Member> members; 이거는 null에러난댜...

    public Team(String name) {
        this.name = name;
    }

}
