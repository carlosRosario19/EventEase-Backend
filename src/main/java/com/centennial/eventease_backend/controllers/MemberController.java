package com.centennial.eventease_backend.controllers;

import com.centennial.eventease_backend.dto.AddMemberDto;
import com.centennial.eventease_backend.exceptions.UsernameAlreadyExistsException;
import com.centennial.eventease_backend.services.contracts.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(@Qualifier("memberServiceImpl") MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("members")
    public void registerMember(@RequestBody AddMemberDto addMemberDto) throws UsernameAlreadyExistsException {
        memberService.add(addMemberDto);
    }
}
