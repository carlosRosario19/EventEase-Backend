package com.centennial.eventease_backend.dto;


import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

public record CreateEventDto(String title,
                             String description,
                             MultipartFile file,
                             String category,
                             LocalDateTime dateTime,
                             String location,
                             int totalTickets,
                             float pricePerTicket,
                             int memberId) {
}
