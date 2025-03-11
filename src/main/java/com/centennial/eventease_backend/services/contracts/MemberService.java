package com.centennial.eventease_backend.services.contracts;

import com.centennial.eventease_backend.dto.AddMemberDTO;
import com.centennial.eventease_backend.exceptions.UsernameAlreadyExistsException;

public interface MemberService {
    void add(AddMemberDTO addMemberDTO) throws UsernameAlreadyExistsException;
}
