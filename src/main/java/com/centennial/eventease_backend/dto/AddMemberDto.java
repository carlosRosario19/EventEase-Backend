package com.centennial.eventease_backend.dto;

public record AddMemberDto(
        String firstName,
        String lastName,
        String phone,
        String username,
        String password,
        String email,
        String bankAccountNumber,
        String bankRoutingNumber,
        String bankName,
        String bankCountry) {
}
