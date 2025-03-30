package com.centennial.eventease_backend.repository.contracts;

import com.centennial.eventease_backend.entities.Member;

import java.util.Optional;

public interface MemberDao {
    Optional<Member> findByUsername(String username);
    void save(Member member);
    Optional<Member> findById(int id);
}
