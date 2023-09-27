package com.example.fastcampusmysql.domain.post.service;

import com.example.fastcampusmysql.domain.post.dto.PostCommand;
import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostWriteService {
    final private PostRepository postRepository;

    // 게시물 작성자를 팔로우하는 유저들의 목록을 조회해야 함, 이를 timeline에 delivery해줘야 함
    // 두 도메인이 필요함 -> Usecase로 올리자!
    public Long create(PostCommand command) {
        var post = Post.builder()
                .memberId(command.memberId())
                .contents(command.contents())
                .build();

        return postRepository.save(post).getId();
    }

    // 좋아요 수 증가
    @Transactional
    public void likePost(Long postId) {
        // repo에서 게시물을 조회하고 업데이트 하는 코드가 필요

        // repo에서 게시물 조회
        var post = postRepository.findById(postId, true).orElseThrow();

        // likeCount 개수 증가
        post.incrementLikeCount();

        // 업데이트
        postRepository.save(post);
    }
}
