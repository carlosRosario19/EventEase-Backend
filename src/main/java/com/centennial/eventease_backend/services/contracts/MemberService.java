package com.centennial.eventease_backend.services.contracts;

import com.centennial.eventease_backend.dto.AddMemberDto;
import com.centennial.eventease_backend.dto.GetMemberDto;
import com.centennial.eventease_backend.exceptions.MemberNotFoundException;
import com.centennial.eventease_backend.exceptions.UsernameAlreadyExistsException;

import java.util.Optional;

public interface MemberService {
    void add(AddMemberDto addMemberDto) throws UsernameAlreadyExistsException;
    Optional<GetMemberDto> get(int id) throws MemberNotFoundException;
}
