package com.centennial.eventease_backend.dto;

public record GetMemberDto(
        int id,
        String firstName,
        String lastName,
        String phone,
        String username,
        String email,
        String bankAccountNumber,
        String bankRoutingNumber,
        String bankName,
        String bankCountry) {
}
