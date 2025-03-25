package com.centennial.eventease_backend.dto;

public record EventDto(int id, String title, String description, String category, String location, int ticketsLeft, float pricePerTicket) {
}
