package com.centennial.eventease_backend.controllers;

import com.centennial.eventease_backend.services.implementations.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthControllerTest {
    private TokenService tokenService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        tokenService = mock(TokenService.class);
        authController = new AuthController(tokenService);
    }

    //This will test if the AuthController is invoking the TokenService correctly.
    @Test
    void login_ShouldReturnToken() {
        Authentication authentication = mock(Authentication.class);
        String expectedToken = "sampleToken";

        when(tokenService.generateToken(authentication)).thenReturn(expectedToken);

        String actualToken = authController.login(authentication);

        assertEquals(expectedToken, actualToken);
        Mockito.verify(tokenService).generateToken(authentication);
    }
}
