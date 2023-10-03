package com.example.fastcampusmysql.application.usecase;

import com.example.fastcampusmysql.domain.member.service.MemberReadService;
import com.example.fastcampusmysql.domain.post.service.PostLikeWriteService;
import com.example.fastcampusmysql.domain.post.service.PostReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreatePostLikeUsecase {
    final private PostReadService postReadService;
    final private MemberReadService memberReadService;
    final private PostLikeWriteService postLikeWriteService;

    // 좋아요를 테이블로 분리함으로써 코드 작업은 더 들어갔지만 write의 성능이 좋아짐
    //  - post에 대한 락을 안잡아도 됨(optimistic locking을 사용하지 않더라도 lock을 잡지 않아도 된다! -> insert이므로!)
    //  - 하나의 자원에 대해서 서로 update하려고 경합하지 않음(insert만 하니까)
    public void execute(Long postId, Long memberId) {
        var post = postReadService.getPost(postId);
        var member = memberReadService.getMember(memberId);
        postLikeWriteService.create(post, member);
    }
}
