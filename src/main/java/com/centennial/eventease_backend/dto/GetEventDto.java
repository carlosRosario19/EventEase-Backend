package com.centennial.eventease_backend.dto;

import org.springframework.core.io.Resource;

import java.time.LocalDateTime;

public record GetEventDto(int id,
                          String title,
                          String description,
                          String imagePath,
                          String category,
                          LocalDateTime dateTime,
                          String location,
                          int ticketsLeft,
                          float pricePerTicket) {
}
