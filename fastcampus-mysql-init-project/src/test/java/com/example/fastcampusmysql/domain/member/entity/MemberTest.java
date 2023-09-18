package com.example.fastcampusmysql.domain.member.entity;

import com.example.fastcampusmysql.util.MemberFixtureFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {
    // Live Template -> for문을 작성하기 위해서는 for() 등을 다 쳐야 하는데, fori만 치면 for문이 작성되는 그런 것
    @DisplayName("회원은 닉네임을 변경할 수 있다") // test에 대한 설명을 명시할 수 있음
    @Test
    public void testChangeNickname() {
        // 생각해볼 부분
        // -> Member.builder()를 통해 필요한 객체를 만드는데, Member 객체가 필요한 테스트 코드마다 비슷한 류의 코드가 계속 중복이 될 것임
        //  -> 그래서 이를 한 곳에 모으고 싶음

        // objectMother 패턴
        //  -> Object Mother는 테스트 할 때 사용되는 클래스 종류
        //  -> 테스트에 필요한 객체들을 만들어주는 데에 생성을 도와주는 함수
//        Member member = Member.builder().build();
//        LongStream.range(0, 10)
//                .mapToObj(MemberFixtureFactory::create)
//                .forEach(member -> {
//                    System.out.println(member.getNickname());
//                });

        // EasyRandom이라는 ObjectMother를 사용해 하나의 FixtureFactory를 만듬
        // 이를 이용해서 Member를 받아볼 것임
        var member = MemberFixtureFactory.create();
        var expected = "pnu";

        member.changeNickname(expected);

        Assertions.assertEquals(expected, member.getNickname());
    }

    @DisplayName("회원의 닉네임은 10자를 초과할 수 없다") // test에 대한 설명을 명시할 수 있음
    @Test
    public void testNicknameMaxLength() {
        var member = MemberFixtureFactory.create();
        var overMaxLengthName = "pnupnupnupnu";

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> member.changeNickname(overMaxLengthName));
    }
}