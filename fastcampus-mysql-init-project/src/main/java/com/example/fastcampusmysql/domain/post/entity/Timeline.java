package com.example.fastcampusmysql.domain.post.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Timeline {
    // 배달받은 회원의 id, 게시물 PK

    // 커서키가 될 것임(게시물의 키가 아니라)
    //  -> timeline에서 레코드를 조회해올 것이므로 이것이 커서키가 될 것임
    final private Long id;
    final private Long memberId;
    final private Long postId;
    final private LocalDateTime createdAt;

    @Builder
    public Timeline(Long id, Long memberId, Long postId, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = Objects.requireNonNull(memberId);
        this.postId = Objects.requireNonNull(postId);
        // 더 검증을 강화하려면 생성 시점에만 존재해야하므로 id값이 null일 때만 createdAt이 null임을 허용하도록 하는 로직을 넣으면 됨
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }
}
