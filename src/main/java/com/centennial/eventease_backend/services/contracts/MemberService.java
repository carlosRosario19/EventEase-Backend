package com.centennial.eventease_backend.services.contracts;

import com.centennial.eventease_backend.dto.AddMemberDto;
import com.centennial.eventease_backend.exceptions.UsernameAlreadyExistsException;

public interface MemberService {
    void add(AddMemberDto addMemberDto) throws UsernameAlreadyExistsException;
}
