package com.centennial.eventease_backend.controllers;

import com.centennial.eventease_backend.dto.CreateEventDto;
import com.centennial.eventease_backend.dto.EventDto;
import com.centennial.eventease_backend.dto.GetEventDto;
import com.centennial.eventease_backend.exceptions.*;
import com.centennial.eventease_backend.services.contracts.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(@Qualifier("eventServiceImpl") EventService eventService){
        this.eventService = eventService;
    }

    @GetMapping("/events")
    public Page<EventDto> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category
    ) throws PageOutOfRangeException {
        return eventService.getAll(page, size, title, location, category);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<GetEventDto> getEvent(@PathVariable int id) throws EventNotFoundException {
        return eventService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/events", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void saveEvent(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam("dateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam("location") String location,
            @RequestParam("totalTickets") int totalTickets,
            @RequestParam("pricePerTicket") float pricePerTicket,
            @RequestParam("memberId") int memberId
    ) throws EventConflictException, InvalidDateTimeException, InvalidPriceException, MemberNotFoundException {

        CreateEventDto dto = new CreateEventDto(
                title, description, file, category,
                dateTime, location, totalTickets,
                pricePerTicket, memberId
        );

        eventService.save(dto);
    }

    @GetMapping("/events/member/{username}")
    public Page<EventDto> getAllEventsByUsername(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws PageOutOfRangeException, MemberNotFoundException {
        return eventService.getAllByUsername(username, page, size);
    }

}
