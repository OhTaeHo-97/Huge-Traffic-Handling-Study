package com.example.fastcampusmysql.domain.follow.service;

import com.example.fastcampusmysql.domain.follow.entity.Follow;
import com.example.fastcampusmysql.domain.follow.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowReadService {
    final private FollowRepository followRepository;

    // MemberReadService에서는 도메인을 직접 반환하지 않았음(MemberDto를 만들어 그것으로 반환했었음)
    // Follow는 외부 API로 나가는 경우가 없을 것 같음
    //  -> Controller에서는 회원정보를 반환할 것임(MemberDto)
    //  -> 아직까지는 도메인 Entity를 직접 반환해도 외부 도메인이나 외부 서비스에 흘러가지 않아서 필요할 때 만들 생각(DTO를)
    //      -> 지금은 시간상 이렇게 진행했지만 실제 구현할 때에는 DTO를 구현하는 것이 좋다!
    public List<Follow> getFollowings(Long memberId) {
        return followRepository.findAllByFromMemberId(memberId);
    }
    public List<Follow> getFollows(Long memberId) {
        return followRepository.findAllByToMemberId(memberId);
    }
}
