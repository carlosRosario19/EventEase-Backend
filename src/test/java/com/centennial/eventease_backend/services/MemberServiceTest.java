package com.centennial.eventease_backend.services;

import com.centennial.eventease_backend.dto.AddMemberDto;
import com.centennial.eventease_backend.dto.GetMemberDto;
import com.centennial.eventease_backend.entities.Member;
import com.centennial.eventease_backend.entities.User;
import com.centennial.eventease_backend.exceptions.MemberNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
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

    private AddMemberDto validMemberDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a sample AddMemberDTO
        validMemberDTO = new AddMemberDto(
                "John",  // firstName
                "Doe",   // lastName
                "6473179845", // phone
                "doe",    // username
                "test123", // password
                "john.doe@example.com", // email
                "123456789012", // bankAccountNumber
                "123456789",    // bankRoutingNumber
                "Royal Bank of Canada", // bankName
                "Canada"         // bankCountry
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

    @Test
    void get_shouldReturnMemberDto_whenMemberExists() throws MemberNotFoundException {
        // Arrange
        int memberId = 1;
        Member mockMember = new Member();
        mockMember.setMemberId(memberId);
        mockMember.setFirstName("John");
        mockMember.setLastName("Doe");
        mockMember.setPhone("6479878978");
        mockMember.setUsername("doe");
        mockMember.setEmail("john.doe@example.com");
        mockMember.setBankAccountNumber("123456789012");
        mockMember.setBankRoutingNumber("123456789");
        mockMember.setBankName("Royal Bank of Canada");
        mockMember.setBankCountry("Canada");

        when(memberDao.findById(memberId)).thenReturn(Optional.of(mockMember));

        // Act
        Optional<GetMemberDto> result = memberService.get(memberId);

        // Assert
        assertTrue(result.isPresent());
        GetMemberDto dto = result.get();
        assertEquals(memberId, dto.id());
        assertEquals("John", dto.firstName());
        assertEquals("Doe", dto.lastName());
        assertEquals("6479878978", dto.phone());
        assertEquals("doe", dto.username());

        verify(memberDao).findById(memberId);
    }

    @Test
    void get_shouldThrowMemberNotFoundException_whenMemberDoesNotExist() {

        int nonExistentId = 999;
        when(memberDao.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        MemberNotFoundException exception = assertThrows(
                MemberNotFoundException.class,
                () -> memberService.get(nonExistentId)
        );

        assertEquals("Member with id 999 not found", exception.getMessage());
        verify(memberDao).findById(nonExistentId);
    }
}
