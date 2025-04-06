package com.centennial.eventease_backend.dto;

public record UpdateMemberDto(
        int id,
        String firstName,
        String lastName,
        String phone,
        String email,
        String bankAccountNumber,
        String bankRoutingNumber,
        String bankName,
        String bankCountry
) {
}
