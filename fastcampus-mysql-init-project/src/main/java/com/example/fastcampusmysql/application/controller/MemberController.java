package com.example.fastcampusmysql.application.controller;

import com.example.fastcampusmysql.domain.member.dto.MemberDto;
import com.example.fastcampusmysql.domain.member.dto.MemberNicknameHistoryDto;
import com.example.fastcampusmysql.domain.member.dto.RegisterMemberCommand;
import com.example.fastcampusmysql.domain.member.service.MemberReadService;
import com.example.fastcampusmysql.domain.member.service.MemberWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {
    final private MemberWriteService memberWriteService;
    final private MemberReadService memberReadService;

    // register(), getMember() 모두 Member라는 도메인 Entity 객체를 그대로 반환하고 있음
    //  -> 이는 고민해볼만한 포인트!
    // JPA에서 Controller까지 Entity 객체가 나오게 되면 OSIV 같은 문제가 발생할 수 있음
    // 그런 이슈뿐 아니라 Layered Architecture에서 entity는 controller, service, repository, entity 순으로 제일 깊은 곳에 있음
    //  -> 이렇게 깊은 곳에 있는 entity가 controller를 통해 presentation layer까지 나가버리면 presentation layer의 요구사항에 entity가 변경되는 상황이 생김
    //  -> 예를 들어, DTO에 내려줘야 할 데이터가 추가됐다던가, presentation layer에 내려야 할 데이터가 추가됐다던가, 소숫점 자리수가 바뀌었다던가 이런 것들의 영향을 entity가 직격으로 맞게 됨
    // 또한 불필요한 정보까지 다 내려주게 됨 -> 강한 결합을 유도!
    // DTO를 도입해야 함
    @PostMapping("/members")
    public MemberDto register(@RequestBody RegisterMemberCommand command) {
        // 객체와 객체를 매핑하는 로직은 mapper라는 layer를 하나 더 두기도 함
        // 하지만 지금 프로젝트는 사이즈가 작으니 MemberReadService에서 구현하여 public으로 열어두는 형태로 진행
        var member = memberWriteService.register(command);
        return memberReadService.toDto(member);
    }

    @GetMapping("/members/{id}")
    public MemberDto getMember(@PathVariable Long id) {
        return memberReadService.getMember(id);
    }

    @PostMapping("/members/{id}/name")
    public MemberDto changeNickname(@PathVariable Long id, @RequestBody String nickname) {
        memberWriteService.changeNickname(id, nickname);
        return memberReadService.getMember(id);
    }

    @GetMapping("/members/{id}/nickname-histories")
    public List<MemberNicknameHistoryDto> getNicknameHistories(@PathVariable Long id) {
        return memberReadService.getNicknameHistories(id);
    }
}
