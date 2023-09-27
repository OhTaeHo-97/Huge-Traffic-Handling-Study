package com.example.fastcampusmysql.domain.post.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Post {
    final private Long id;

    final private Long memberId;

    final private String contents; // 컨텐츠 수정 기능을 만들지 않을 것이라서 final

    final private LocalDate createdDate;

    private Long likeCount; // 좋아요 수

    final private LocalDateTime createdAt;

    @Builder
    public Post(Long id, Long memberId, String contents, LocalDate createdDate, Long likeCount, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = Objects.requireNonNull(memberId);
        this.contents = Objects.requireNonNull(contents);
        this.createdDate = createdDate == null ? LocalDate.now() : createdDate;
        this.likeCount = likeCount == null ? 0 : likeCount;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public void incrementLikeCount() {
        likeCount++;
    }
}
