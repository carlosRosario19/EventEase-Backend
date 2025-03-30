package com.centennial.eventease_backend.services.contracts;

import com.centennial.eventease_backend.dto.CreateEventDto;
import com.centennial.eventease_backend.dto.EventDto;
import com.centennial.eventease_backend.dto.GetEventDto;
import com.centennial.eventease_backend.exceptions.*;
import org.springframework.data.domain.Page;

import java.util.Optional;


public interface EventService {
    Page<EventDto> getAll(int page, int size, String title, String location, String category) throws PageOutOfRangeException;
    Optional<GetEventDto> get(int id) throws EventNotFoundException;
    void save(CreateEventDto createEventDto) throws EventConflictException, InvalidDateTimeException, InvalidPriceException, MemberNotFoundException;
}
