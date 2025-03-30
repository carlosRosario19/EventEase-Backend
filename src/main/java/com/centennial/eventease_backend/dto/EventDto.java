package com.centennial.eventease_backend.dto;

import org.springframework.core.io.Resource;

public record EventDto(int id,
                       String title,
                       String description,
                       String imagePath,
                       String category,
                       String location,
                       int ticketsLeft,
                       float pricePerTicket) {
}
