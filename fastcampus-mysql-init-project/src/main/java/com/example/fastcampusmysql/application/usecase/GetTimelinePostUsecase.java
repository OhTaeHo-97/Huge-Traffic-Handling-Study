package com.example.fastcampusmysql.application.usecase;

import com.example.fastcampusmysql.domain.follow.entity.Follow;
import com.example.fastcampusmysql.domain.follow.service.FollowReadService;
import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.entity.Timeline;
import com.example.fastcampusmysql.domain.post.service.PostReadService;
import com.example.fastcampusmysql.domain.post.service.TimelineReadService;
import com.example.fastcampusmysql.util.CursorRequest;
import com.example.fastcampusmysql.util.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTimelinePostUsecase {
    final private FollowReadService followReadService;
    final private PostReadService postReadService;
    final private TimelineReadService timelineReadService;

    public CursorResponse<Post> execute(Long memberId, CursorRequest cursorRequest) {
        /*
            1. memberId로 follow 정보 조회
            2. 1번의 결과로 게시물 조회
         */
        var followings = followReadService.getFollowings(memberId);
        var followingMemberIds = followings.stream()
                .map(Follow::getToMemberId)
                .toList();

        return postReadService.getPosts(followingMemberIds, cursorRequest);
    }

    public CursorResponse<Post> executeByTimeline(Long memberId, CursorRequest cursorRequest) {
        /*
            1. timeline 테이블 조회
            2. 1번에 해당하는 게시물을 조회한다
                -> join으로 가져와도 됨
         */
        var pagedTimelines = timelineReadService.getTimelines(memberId, cursorRequest);
        var postIds = pagedTimelines.contents().stream()
                .map(Timeline::getPostId)
                .toList();

        // 기존에는 회원 아이디로 조회했다면 이번에는 식별자로 조회해야 함
        //  in query를 이용해 구현
        var posts = postReadService.getPosts(postIds);

        return new CursorResponse<>(
                pagedTimelines.nextCursorRequest(),
                posts
        );
    }
}
