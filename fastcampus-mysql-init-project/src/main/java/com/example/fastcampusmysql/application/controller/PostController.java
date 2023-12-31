package com.example.fastcampusmysql.application.controller;

import com.example.fastcampusmysql.application.usecase.CreatePostLikeUsecase;
import com.example.fastcampusmysql.application.usecase.CreatePostUsecase;
import com.example.fastcampusmysql.application.usecase.GetTimelinePostUsecase;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCount;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.dto.PostCommand;
import com.example.fastcampusmysql.domain.post.dto.PostDto;
import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.service.PostReadService;
import com.example.fastcampusmysql.domain.post.service.PostWriteService;
import com.example.fastcampusmysql.util.CursorRequest;
import com.example.fastcampusmysql.util.CursorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    final private PostWriteService postWriteService;
    final private PostReadService postReadService;
    final private GetTimelinePostUsecase getTimelinePostUsecase;
    final private CreatePostUsecase createPostUsecase;
    final private CreatePostLikeUsecase createPostLikeUsecase;

    @PostMapping("")
    public Long create(PostCommand command) {
        return createPostUsecase.execute(command);
    }

    @GetMapping("/daily-post-counts")
    public List<DailyPostCount> getDailyPostCounts(DailyPostCountRequest request) {
        return postReadService.getDailyPostCounts(request);
    }

//    @GetMapping("/members/{memberId}")
//    public Page<Post> getPosts(
//            @PathVariable Long memberId,
//            @RequestParam Integer page,
//            @RequestParam Integer size
//    ) {
//        return postReadService.getPosts(memberId, PageRequest.of(page, size));
//    }

    @GetMapping("/members/{memberId}")
    public Page<PostDto> getPosts(
            @PathVariable Long memberId,
            Pageable pageable
    ) {
        return postReadService.getPosts(memberId, pageable);
    }

    @GetMapping("/members/{memberId}/by-cursor")
    public CursorResponse<Post> getPostsByCursor(
            @PathVariable Long memberId,
            CursorRequest cursorRequest
    ) {
        return postReadService.getPosts(memberId, cursorRequest);
    }

//    @GetMapping("/members/{memberId}/timeline")
//    public CursorResponse<Post> getTimeline(
//            @PathVariable Long memberId,
//            CursorRequest cursorRequest
//    ) {
//        return getTimelinePostUsecase.execute(memberId, cursorRequest);
//    }
    @GetMapping("/members/{memberId}/timeline")
    public CursorResponse<Post> getTimeline(
            @PathVariable Long memberId,
            CursorRequest cursorRequest
    ) {
        return getTimelinePostUsecase.executeByTimeline(memberId, cursorRequest);
    }

    // 비관적 락을 통한 좋아요 증가
//    @PostMapping("/{postId}/like")
//    public void likePost(@PathVariable Long postId) {
//        postWriteService.likePost(postId);
//    }

    // 낙관적 락을 통한 좋아요 증가
    @PostMapping("/{postId}/like/v1")
    public void likePost(@PathVariable Long postId) {
        postWriteService.likePostByOptimisticLock(postId);
    }

    // 좋아요를 별도 집계 테이블로 나눈 후 좋아요 증가
    @PostMapping("/{postId}/like/v2")
    public void likePostV2(@PathVariable Long postId,
                           @RequestParam Long memberId) {
        createPostLikeUsecase.execute(postId, memberId);
    }
}
