package com.centennial.eventease_backend.dto;

import org.springframework.core.io.Resource;

import java.time.LocalDateTime;

public record GetEventDto(int id,
                          String title,
                          String description,
                          Resource image,
                          String category,
                          LocalDateTime dateTime,
                          String location,
                          int ticketsLeft,
                          float pricePerTicket) {
}
