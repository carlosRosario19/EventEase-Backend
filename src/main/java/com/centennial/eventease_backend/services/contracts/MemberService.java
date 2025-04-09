package com.centennial.eventease_backend.services.contracts;

import com.centennial.eventease_backend.dto.AddMemberDto;
import com.centennial.eventease_backend.dto.GetMemberDto;
import com.centennial.eventease_backend.dto.UpdateMemberDto;
import com.centennial.eventease_backend.exceptions.MemberNotFoundException;
import com.centennial.eventease_backend.exceptions.UsernameAlreadyExistsException;

import java.util.Optional;

public interface MemberService {
    void add(AddMemberDto addMemberDto) throws UsernameAlreadyExistsException;
    Optional<GetMemberDto> getByUsername(String username) throws MemberNotFoundException;
    void update(UpdateMemberDto updateMemberDto) throws MemberNotFoundException;
}
