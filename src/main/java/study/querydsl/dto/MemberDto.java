package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor//dsl에서 세터 프로잭션 사용때 필요
public class MemberDto {

    private String username;
    private int age;
    private int ageRange;
    private int ageMax;

    @QueryProjection//q파일 생성 어노테이션
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }

    public MemberDto(String username, int age, int ageRange) {
        this.username = username;
        this.age = age;
        this.ageRange = ageRange;
    }

    public MemberDto(String username, int age, int ageRange, int ageMax) {
        this.username = username;
        this.age = age;
        this.ageRange = ageRange;
        this.ageMax = ageMax;
    }
}
