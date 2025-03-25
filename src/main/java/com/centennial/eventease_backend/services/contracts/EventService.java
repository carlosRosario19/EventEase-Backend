package com.centennial.eventease_backend.services.contracts;

import com.centennial.eventease_backend.dto.EventDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface EventService {
    Page<EventDto> getAll(Pageable pageable);
}
