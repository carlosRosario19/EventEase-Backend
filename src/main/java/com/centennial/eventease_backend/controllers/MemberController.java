package com.centennial.eventease_backend.controllers;

import com.centennial.eventease_backend.dto.AddMemberDto;
import com.centennial.eventease_backend.dto.GetMemberDto;
import com.centennial.eventease_backend.dto.UpdateMemberDto;
import com.centennial.eventease_backend.exceptions.MemberNotFoundException;
import com.centennial.eventease_backend.exceptions.UsernameAlreadyExistsException;
import com.centennial.eventease_backend.services.contracts.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("members/{id}")
    public ResponseEntity<GetMemberDto> getMember(@PathVariable int id) throws MemberNotFoundException {
        return memberService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("members")
    public void updateMember(@RequestBody UpdateMemberDto updateMemberDto) throws MemberNotFoundException  {
        memberService.update(updateMemberDto);
    }
}
