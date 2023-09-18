package com.example.fastcampusmysql.application.usecase;

import com.example.fastcampusmysql.domain.follow.service.FollowWriteService;
import com.example.fastcampusmysql.domain.member.service.MemberReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateFollowMemberUsecase {
    // read, write 서비스를 분리해서 얻는 이점이 여기서도 드러남
    //  -> 이 usecase는 회원에 대한 쓰기 권한이 전혀 없음(의존성이 분리된 덕분에)
    final private MemberReadService memberReadService;
    final private FollowWriteService followWriteService;

    public void execute(Long fromMemberId, Long toMemberId) {
        /*
            1. 입력 받은 memberId로 회원 조회
            2. followWriteService.create() 호출
         */

        var fromMember = memberReadService.getMember(fromMemberId);
        var toMember = memberReadService.getMember(toMemberId);

        followWriteService.create(fromMember, toMember);
    }
}

// usecase layer는 가능한 로직이 없어야 한다!
//  -> 각 도메인에 대한 비즈니스 로직은 각 도메인 service에 들어가있어야 함
//  -> usecase는 도메인 Service의 흐름을 제어하는 역할만 해야 한다!
