package com.example.fastcampusmysql.domain.member.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Member {
    final private Long id;
    private String nickname; // 변경이 가능하니 final로 하지 않음
    final private String email;
    final private LocalDate birthday;
    final private LocalDateTime createdAt;

    final private static Long NAME_MAX_LENGTH = 10L;

    @Builder
    public Member(Long id, String nickname, String email, LocalDate birthday, LocalDateTime createdAt) {
        // JPA 도입을 할 수 있도록 구현해볼 것임
        // JPA -> id 값이 있냐 없냐로 Insert할지, update할지를 결정
        // 그래서 id는 nullable을 열어둠
        this.id = id;
        this.email = Objects.requireNonNull(email);
        this.birthday = Objects.requireNonNull(birthday);

        // nickname, email, birthday -> null이 되면 안됨
        // 그래서 Objects.requireNonNull() 이용
        // nickname -> 10자를 넘으면 안됨
        validateNickname(nickname);
        this.nickname = Objects.requireNonNull(nickname);

        // createdAt도 null이면 지금 시간을 넣어주면 됨
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    // Member 안에 객체에 이 함수를 넣은 이유는 이렇게 하면 단위 테스트가 매우 용이해짐
    // changeNickname 함수면 보면 다른 객체의 도움이 전혀 필요없고, Member라는 객체만 만들면 됨
    // 그래서 테스트 코드 짜기가 용이해짐
    //  -> test code는 product 1번 코드와 동일 패키지 구조를 동일하게 가져가주는 것이 좋음
    public void changeNickname(String to) {
        Objects.requireNonNull(to);
        validateNickname(to);
        nickname = to;
    }

    private void validateNickname(String nickname) {
        // Spring Core에 있는 assert를 이용해볼 것임
        Assert.isTrue(nickname.length() <= NAME_MAX_LENGTH, "최대 길이를 초과하였습니다.");

        // custom Exception을 이용하는 것이 좋다!
        // 지금은 빠른 진행을 위해 Assert를 이용한 것
    }

    // entity -> 생성 시간을 모든 엔티티에 다 넣음
    // 나중에 디버깅할 때 문제 생겼을 때에 도움이 됨
}
