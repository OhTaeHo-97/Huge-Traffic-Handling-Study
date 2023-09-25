package com.example.fastcampusmysql.domain.member.service;

import com.example.fastcampusmysql.domain.member.dto.RegisterMemberCommand;
import com.example.fastcampusmysql.domain.member.entity.Member;
import com.example.fastcampusmysql.domain.member.entity.MemberNicknameHistory;
import com.example.fastcampusmysql.domain.member.repository.MemberNicknameHistoryRepository;
import com.example.fastcampusmysql.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@RequiredArgsConstructor
@Service
public class MemberWriteService {
    final private MemberRepository memberRepository;
    final private MemberNicknameHistoryRepository memberNicknameHistoryRepository;

    @Transactional
    public Member register(RegisterMemberCommand command) {
        /*
            목표 - 회원정보(이메일, 닉네임, 생년월일)를 등록한다
                - 닉네임은 10자를 넘길 수 없다
            파라미터 - memberRegisterCommand

            var member = Member.of(memberRegisterCommand)
            memberRepository.save(member)
         */
        var member = Member.builder()
                .nickname(command.nickname())
                .email(command.email())
                .birthday(command.birthday())
                .build();

        // 별도의 트랜잭션을 설정하지 않았기 때문에 SQL문 하나하나가 트랜잭션이라고 생각해도 무방하다!
        //  -> 결국 두 트랜잭션이 별개다!
        // 회원을 저장하고 이름 변경 내역을 저장하기 직전에 예외를 던진다면 트랜잭션이 적용되지 않아 회원만 저장되고 이름이 저장되지 않는다!
        // JdbcTemplate을 이용해 트랜잭션을 제어하는 방법은 크게 2가지가 있음
        //  1. @Transactional 어노테이션을 이용해 선언적으로 트랜잭션을 제어하는 방법
        //      - 내가 트랜잭션을 보장하고 싶은 메서드 위에다가 @Transactional 어노테이션을 붙이면 SQL은 해당 메서드에서 트랜잭션을 시작하고 해당 메서드가 끝이 날 때 트랜잭션을 commit한다
        //  2. TransactionTemplate을 사용하여 직접 트랜잭션을 제어하는 방법
        // Spring을 사용할 때는 선언적 어노테이션을 선호한다
        //  - 비즈니스 로직에 DB와 종속된 코드들을 숨길 수 있기 때문!
        // JdbcTemplate을 사용하고 있는데 TransactionTemplate 도움 없이 트랜잭션을 구현하면 코드가 복잡해지고 처리해야 할 것들이 많음
        //  -> 그래서 대부분 TransactionTemplate이나 @Transactional 어노테이션을 많이 사용!
        //  -> TransactionTemplate도 많이 사용하지 않음

        // execute()를 통해 트랜잭션 범위를 정해줄 수 있음
        //  - execute() 안에 콜백 함수로 받음
        //  - execute() 안에 받은 인자의 범위가 트랜잭션의 범위가 된다
        TransactionTemplate transactionTemplate = new TransactionTemplate();

        var savedMember = memberRepository.save(member); // 회원을 저장하는 insert 쿼리가 하나 발생할 것임

        saveMemberNicknameHistory(savedMember); // 회원의 이름 변경 내역 저장하는 쿼리가 발생할 것임
        return savedMember;
    }

    @Transactional
    public void changeNickname(Long memberId, String nickname) {
        /*
            1. 회원의 이름을 변경
            2. 변경 내역을 저장한다
         */

        var member = memberRepository.findById(memberId).orElseThrow();
        member.changeNickname(nickname);
        memberRepository.save(member);

        saveMemberNicknameHistory(member);
        // TODO : 변경내역 히스토리를 저장한다.
    }

    private void saveMemberNicknameHistory(Member member) {
        var history = MemberNicknameHistory.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .build();
        memberNicknameHistoryRepository.save(history);
    }

}
