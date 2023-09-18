package com.example.fastcampusmysql.domain.follow.service;

import com.example.fastcampusmysql.domain.follow.entity.Follow;
import com.example.fastcampusmysql.domain.follow.repository.FollowRepository;
import com.example.fastcampusmysql.domain.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FollowWriteService {
    final private FollowRepository followRepository;

    public void create(MemberDto fromMember, MemberDto toMember) {
        /*
            from, to 회원 정보를 받아서 저장
            from <-> to validate (같으면 이상하니까)
         */

        // 식별자를 받으면 실제 존재하는 회원인지 검증을 여기서 해야 함
        //  -> 검증하기 위해서는 Member 도메인의 Repository나 Service를 주입 받아야 함
        //      -> 결합이 심해짐!
        //      -> 구체적인 구현을 너무 많이 알게 됨
        //  -> 그래서 식별자륾 받지 않고 계층 간의 이동에 쓰이는 MemberDto를 받을 것임!
        //  -> 서로 다른 도메인에 데이터를 주고 받아야 할 때, 서로 다른 도메인의 흐름을 제어해야 할 때 어디서 해야 하는가에 대한 고민이 필요함!
        //      -> 여러 방법론들이 있음(Hexagonal architecture, DDD, Layered Architecture...)
        //          -> 경계를 나누고 이 경계간에 통신을 하는 데에 여러가지 이론들이 있음
        //          -> 여기서는 심플한 구조로 가져가려고 함
        //          -> application layer를 하나 둘 것이고, application에 usecase라는 layer를 하나 둘 것임
        //              -> usecase layer는 여러 도메인 layer를 orchestration, 즉 흐름을 제어하는 역할을 할 것임!
        //          -> follow feature는 member와 follow 이 2개의 도메인의 orchestration이 필요 -> 그래서 usecase가 하나 만들어짐
        //              -> usecase 클래스가 동사로 써져있음
        //  -               -> 해당 기능 하나만 하는 것을 만들기 위해 명시적으로 이렇게 씀
        //              -> 앞으론 만드는 모든 usecase는 execute()라는 메서드명을 그대로 가져갈 것이고, 클래스 명에서 어떤 요구사항을 충족하는 usecase인지 명확히 이름을 지을 것임

        Assert.isTrue(!fromMember.id().equals(toMember.id()), "From, To 회원이 동일합니다.");

        var follow = Follow.builder()
                .fromMemberId(fromMember.id())
                .toMemberId(toMember.id())
                .build();

        followRepository.save(follow);
    }
}
