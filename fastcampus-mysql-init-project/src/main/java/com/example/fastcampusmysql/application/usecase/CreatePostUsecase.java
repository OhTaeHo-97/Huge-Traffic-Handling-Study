package com.example.fastcampusmysql.application.usecase;

import com.example.fastcampusmysql.domain.follow.entity.Follow;
import com.example.fastcampusmysql.domain.follow.service.FollowReadService;
import com.example.fastcampusmysql.domain.post.dto.PostCommand;
import com.example.fastcampusmysql.domain.post.service.PostWriteService;
import com.example.fastcampusmysql.domain.post.service.TimelineWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePostUsecase {
    final private PostWriteService postWriteService;
    final private FollowReadService followReadService;
    final private TimelineWriteService timelineWriteService;

//    @Transactional
    public Long execute(PostCommand postCommand) {
        // insert 쿼리가 postWriteService.create()에서 한 번, timelineWriteService.deliveryToTimeline()에서 한 번 나가게 됨
        //  -> 트랜잭션 필요!
        //  -> 여기에 @Transactional을 걸어주면 트랜잭션이 묶이면서 연산들이 All or Nothing 연산이 된다!(Atomic 연산이 보장된다)
        // 여기에 트랜잭션을 걸 것인지는 고민해봐야 하는 문제
        //  -> 게시물 하나를 작성하는 것에 팔로워가 몇 만명 이상이라면 이 트랜잭션이 너무 길어질 것임
        //
        var postId = postWriteService.create(postCommand);

        var followerMemberIds = followReadService
                .getFollows(postCommand.memberId())
                .stream()
                .map(Follow :: getFromMemberId)
                .toList();

        timelineWriteService.deliveryToTimeline(postId, followerMemberIds);

        return postId;
    }
}
