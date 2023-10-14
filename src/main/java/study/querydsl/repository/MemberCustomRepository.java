package study.querydsl.repository;

import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberCustomRepository {//d1. 이걸 datajpa 인터페이스에 같이 상속시킨다

    List<MemberTeamDto> search(MemberSearchCondition condition);

}
