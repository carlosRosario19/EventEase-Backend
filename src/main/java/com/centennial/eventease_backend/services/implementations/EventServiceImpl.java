package com.centennial.eventease_backend.services.implementations;

import com.centennial.eventease_backend.dto.EventDto;
import com.centennial.eventease_backend.entities.Event;
import com.centennial.eventease_backend.exceptions.PageOutOfRangeException;
import com.centennial.eventease_backend.repository.contracts.EventDao;
import com.centennial.eventease_backend.services.contracts.EventService;
import com.centennial.eventease_backend.services.contracts.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class EventServiceImpl implements EventService {

    private final EventDao eventDao;
    private final ImageStorageService imageStorageService;
    private Function<Event, EventDto> eventDtoMapper;
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    public EventServiceImpl(@Qualifier("eventDaoImpl") EventDao eventDao,
                            @Qualifier("imageStorageServiceImpl") ImageStorageService imageStorageService){
        this.eventDao = eventDao;
        this.imageStorageService = imageStorageService;
        this.imageStorageService.init();

        this.eventDtoMapper = entity -> {
            // Handle nullable imagePath
            Resource imageResource = null;
            if (entity.getImagePath() != null) {
                try {
                    imageResource = imageStorageService.load(entity.getImagePath());
                } catch (RuntimeException e) {
                    // Log error (consider adding logger)
                    System.err.println("Failed to load image: " + entity.getImagePath() + ", error: " + e.getMessage());
                    // imageResource remains null
                }
            }

            return new EventDto(
                    entity.getId(),
                    entity.getTitle(),
                    entity.getDescription(),
                    imageResource,  // Will be null if no image or error loading
                    entity.getCategory(),
                    entity.getLocation(),
                    entity.getTotalTickets() - entity.getTicketsSold(),
                    entity.getPricePerTicket()
            );
        };
    }

    @Override
    public Page<EventDto> getAll(int page, int size) throws PageOutOfRangeException {

        if(page < 0){
            throw new PageOutOfRangeException("Page number cannot be negative");
        }
        else if(size <= 0){
            throw new PageOutOfRangeException("Page size must be greater than 0");
        }
        else if(size > MAX_PAGE_SIZE){
            throw new PageOutOfRangeException("Page size cannot exceed " + MAX_PAGE_SIZE);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventPage = eventDao.findAllOrderedByDate(pageable);
        return eventPage.map(eventDtoMapper);
    }
}
