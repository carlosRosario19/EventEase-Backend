package com.centennial.eventease_backend.services;


import com.centennial.eventease_backend.entities.Authority;
import com.centennial.eventease_backend.entities.AuthorityId;
import com.centennial.eventease_backend.entities.User;
import com.centennial.eventease_backend.repository.contracts.UserDao;
import com.centennial.eventease_backend.services.implementations.AppUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppUserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private AppUserService appUserService;

    private User user;

    @BeforeEach
    void setup() {
        // Initialize a User instance for testing
        Authority authority = new Authority();
        AuthorityId authorityId = new AuthorityId();
        authorityId.setUsername("testUser");
        authorityId.setAuthority("ROLE_MEMBER");
        authority.setId(authorityId);

        user = new User();
        user.setUsername("testUser");
        user.setPassword("encodedPassword");
        user.setEnabled('Y');
        user.setAuthorities(Set.of(authority));
    }

    @Test
    void loadUserByUsername_UserExists_ReturnsUserDetails() {
        // Arrange
        when(userDao.findByUsername("testUser")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = appUserService.loadUserByUsername("testUser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testUser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_MEMBER")));
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());

        verify(userDao, times(1)).findByUsername("testUser");
    }

    @Test
    void loadUserByUsername_UserDoesNotExist_ThrowsUsernameNotFoundException() {
        // Arrange
        when(userDao.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> appUserService.loadUserByUsername("nonExistentUser")
        );
        assertEquals("User not found", exception.getMessage());
        verify(userDao, times(1)).findByUsername("nonExistentUser");
    }

    @Test
    void loadUserByUsername_UserDisabled_ReturnsUserDetailsWithDisabledFlag() {
        // Arrange
        user.setEnabled('N');
        when(userDao.findByUsername("testUser")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = appUserService.loadUserByUsername("testUser");

        // Assert
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
        verify(userDao, times(1)).findByUsername("testUser");
    }
}
