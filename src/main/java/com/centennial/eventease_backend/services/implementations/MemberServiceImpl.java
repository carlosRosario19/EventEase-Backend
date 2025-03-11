package com.centennial.eventease_backend.services.implementations;

import com.centennial.eventease_backend.dto.AddMemberDTO;
import com.centennial.eventease_backend.entities.Authority;
import com.centennial.eventease_backend.entities.AuthorityId;
import com.centennial.eventease_backend.entities.Member;
import com.centennial.eventease_backend.entities.User;
import com.centennial.eventease_backend.exceptions.UsernameAlreadyExistsException;
import com.centennial.eventease_backend.repository.contracts.MemberDao;
import com.centennial.eventease_backend.repository.contracts.UserDao;
import com.centennial.eventease_backend.services.contracts.MemberService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.function.Function;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberDao memberDao;
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(
            @Qualifier("memberDaoImpl") MemberDao memberDao,
            @Qualifier("userDaoImpl") UserDao userDao,
            PasswordEncoder passwordEncoder
    ){
        this.memberDao = memberDao;
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public void add(AddMemberDTO dto) throws UsernameAlreadyExistsException {
        if (memberDao.findByUsername(dto.username()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username " + dto.username() + " already exists");
        }
        userDao.create(userMapper(dto));
        memberDao.save(memberMapper(dto));
    }

    private Member memberMapper(AddMemberDTO dto){
        Member member = new Member();
        member.setFirstName(dto.firstName());
        member.setLastName(dto.lastName());
        member.setPhone(dto.phone());
        member.setCreatedAt(LocalDate.now());
        member.setUsername(dto.username());
        return member;
    }

    private User userMapper(AddMemberDTO dto){
        User user = new User();
        user.setUsername(dto.username());
        String hashedPassword = passwordEncoder.encode(dto.password());
        user.setPassword(hashedPassword);
        user.setEnabled('Y');
        Set<Authority> authorities = Set.of(authorityMapper.apply(dto));
        user.setAuthorities(authorities);
        return user;
    }

    private final Function<AddMemberDTO, Authority> authorityMapper = dto -> {
        Authority authority = new Authority();
        // Initialize the id field
        AuthorityId authorityId = new AuthorityId();
        authorityId.setUsername(dto.username());
        authorityId.setAuthority("ROLE_MEMBER");

        authority.setId(authorityId); // Set the initialized id
        return authority;
    };
}
