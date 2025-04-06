package com.centennial.eventease_backend.controllers;


import com.centennial.eventease_backend.dto.AddMemberDto;
import com.centennial.eventease_backend.dto.GetMemberDto;
import com.centennial.eventease_backend.exceptions.MemberNotFoundException;
import com.centennial.eventease_backend.exceptions.UsernameAlreadyExistsException;
import com.centennial.eventease_backend.services.contracts.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private AddMemberDto validMemberDTO;

    @BeforeEach
    void setup() {
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
    @WithMockUser(username = "member", roles={"MEMBER"})
    void saveMember_shouldReturnOk_whenMemberIsSaved() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validMemberDTO)))
                .andExpect(status().isOk());

        verify(memberService).add(validMemberDTO);
    }

    @Test
    @WithMockUser(username = "member", roles={"MEMBER"})
    void saveBook_shouldReturnConflict_whenUsernameAlreadyExists() throws Exception{
        Mockito.doThrow(new UsernameAlreadyExistsException("Username already exists in the database"))
                .when(memberService).add(Mockito.any(AddMemberDto.class));

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validMemberDTO)))
                .andExpect(status().isConflict());

        verify(memberService).add(validMemberDTO);
    }

    @Test
    @WithMockUser(username = "member", roles={"MEMBER"})
    void getMember_shouldReturnMember_whenMemberExists() throws Exception {
        // Arrange
        GetMemberDto expectedDto = new GetMemberDto(
                1,
                "John",
                "Doe",
                "6473179845",
                "doe"
        );

        when(memberService.get(1))
                .thenReturn(Optional.of(expectedDto));

        // Act & Assert
        mockMvc.perform(get("/api/members/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phone").value("6473179845"))
                .andExpect(jsonPath("$.username").value("doe"));

        verify(memberService).get(1);
    }

    @Test
    @WithMockUser(username = "member", roles={"MEMBER"})
    void getMember_shouldReturnNotFound_whenMemberDoesNotExist() throws Exception {
        // Arrange
        when(memberService.get(anyInt()))
                .thenThrow(new MemberNotFoundException("Member not found"));

        // Act & Assert
        mockMvc.perform(get("/api/members/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(memberService).get(999);
    }

    @Test
    void getMember_shouldReturnUnauthorized_whenUserNotAuthenticated() throws Exception {
        // This test doesn't use @WithMockUser
        mockMvc.perform(get("/api/members/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

}
