package com.centennial.eventease_backend.services;

import com.centennial.eventease_backend.dto.AddMemberDTO;
import com.centennial.eventease_backend.entities.Member;
import com.centennial.eventease_backend.entities.User;
import com.centennial.eventease_backend.exceptions.UsernameAlreadyExistsException;
import com.centennial.eventease_backend.repository.contracts.MemberDao;
import com.centennial.eventease_backend.repository.contracts.UserDao;
import com.centennial.eventease_backend.services.implementations.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

public class MemberServiceTest {

    @Mock
    private MemberDao memberDao;

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    private AddMemberDTO validMemberDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a sample AddMemberDTO
        validMemberDTO = new AddMemberDTO(
                "Doe",
                "John",
                "6479878978",
                "doe",
                "test123"
        );
    }

    @Test
    void add_Success() throws UsernameAlreadyExistsException {
        // Mock the DAO responses

        when(memberDao.findByUsername(validMemberDTO.username())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validMemberDTO.password())).thenReturn("hashedPassword");

        // Execute the method
        memberService.add(validMemberDTO);

        // Verify the interactions

        verify(memberDao).findByUsername(validMemberDTO.username());

        verify(userDao).create(any(User.class));
        verify(memberDao).save(any(Member.class));
    }

    @Test
    void add_ThrowsUsernameAlreadyExistsException() {
        when(memberDao.findByUsername(validMemberDTO.username())).thenReturn(Optional.of(new Member()));

        assertThrows(UsernameAlreadyExistsException.class, () -> memberService.add(validMemberDTO));

        verify(memberDao).findByUsername(validMemberDTO.username());
        verify(userDao, never()).create(any());
        verify(memberDao, never()).save(any());
    }
}
